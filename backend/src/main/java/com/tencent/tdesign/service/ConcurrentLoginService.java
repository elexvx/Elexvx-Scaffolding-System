package com.tencent.tdesign.service;

import com.tencent.tdesign.vo.ConcurrentLoginDecisionEvent;
import com.tencent.tdesign.vo.ConcurrentLoginEvent;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class ConcurrentLoginService {
  private static final long PENDING_TTL_MS = 2 * 60 * 1000L;
  private static final long LOGIN_HEARTBEAT_INTERVAL_SECONDS = 15L;
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final Map<Long, CopyOnWriteArrayList<SseEmitter>> loginEmitters = new ConcurrentHashMap<>();
  private final Map<SseEmitter, ScheduledFuture<?>> loginHeartbeatTasks = new ConcurrentHashMap<>();
  private final Map<String, PendingLogin> pendingLogins = new ConcurrentHashMap<>();
  private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
    Thread thread = new Thread(r, "concurrent-login-sse-heartbeat");
    thread.setDaemon(true);
    return thread;
  });

  @PreDestroy
  public void shutdownHeartbeatExecutor() {
    heartbeatExecutor.shutdownNow();
  }

  public SseEmitter subscribeLoginNotice(long loginId) {
    SseEmitter emitter = new SseEmitter(0L);
    loginEmitters.computeIfAbsent(loginId, key -> new CopyOnWriteArrayList<>()).add(emitter);
    emitter.onCompletion(() -> removeLoginEmitter(loginId, emitter));
    emitter.onTimeout(() -> removeLoginEmitter(loginId, emitter));
    emitter.onError((ex) -> removeLoginEmitter(loginId, emitter));
    startLoginHeartbeat(loginId, emitter);
    sendConnectedEvent(loginId, emitter);
    return emitter;
  }

  public boolean hasActiveSubscriber(long loginId) {
    List<SseEmitter> emitters = loginEmitters.get(loginId);
    return emitters != null && !emitters.isEmpty();
  }

  public void publishForceLogout(long loginId, String message) {
    List<SseEmitter> emitters = loginEmitters.get(loginId);
    if (emitters == null || emitters.isEmpty()) return;
    String text = (message == null || message.isBlank()) ? "当前登录状态失效，请重新登录" : message;
    for (SseEmitter emitter : emitters) {
      try {
        emitter.send(SseEmitter.event().name("force-logout").data(Map.of("message", text)));
        removeLoginEmitter(loginId, emitter);
        emitter.complete();
      } catch (Exception ex) {
        removeLoginEmitter(loginId, emitter);
      }
    }
  }

  public PendingLogin createPending(
    long loginId,
    String deviceModel,
    String os,
    String browser,
    String deviceInfo,
    String ipAddress,
    String loginLocation
  ) {
    String requestId = UUID.randomUUID().toString();
    String requestKey = UUID.randomUUID().toString().replace("-", "");
    PendingLogin pending = new PendingLogin(
      requestId,
      requestKey,
      loginId,
      deviceModel,
      os,
      browser,
      deviceInfo,
      ipAddress,
      loginLocation,
      System.currentTimeMillis()
    );
    pendingLogins.put(requestId, pending);
    try {
      publishLoginNotice(loginId, pending);
    } catch (Exception ex) {
      // SSE client may disconnect; ignore to avoid blocking login flow
    }
    return pending;
  }

  public SseEmitter subscribeDecision(String requestId, String requestKey) {
    PendingLogin pending = getPending(requestId);
    if (pending == null || !pending.requestKey.equals(requestKey)) {
      throw new IllegalArgumentException("等待确认已失效，请重新登录");
    }
    if (pending.isExpired()) {
      pendingLogins.remove(requestId);
      throw new IllegalArgumentException("等待确认已过期，请重新登录");
    }

    SseEmitter emitter = new SseEmitter(PENDING_TTL_MS);
    pending.decisionEmitters.add(emitter);
    emitter.onCompletion(() -> pending.decisionEmitters.remove(emitter));
    emitter.onTimeout(() -> pending.decisionEmitters.remove(emitter));
    emitter.onError((ex) -> pending.decisionEmitters.remove(emitter));

    if (pending.decision != Decision.PENDING) {
      sendDecisionEvent(emitter, pending.decision);
    }
    return emitter;
  }

  public PendingLogin decide(long loginId, String requestId, boolean approve) {
    PendingLogin pending = getPending(requestId);
    if (pending == null) {
      throw new IllegalArgumentException("等待确认已失效，请重新登录");
    }
    if (pending.loginId != loginId) {
      throw new IllegalArgumentException("无权处理该登录请求");
    }
    if (pending.isExpired()) {
      pendingLogins.remove(requestId);
      throw new IllegalArgumentException("等待确认已过期，请重新登录");
    }
    if (pending.decision != Decision.PENDING) {
      return pending;
    }
    pending.decision = approve ? Decision.APPROVED : Decision.REJECTED;
    notifyDecision(pending);
    return pending;
  }

  public PendingLogin consumeApproved(String requestId, String requestKey) {
    PendingLogin pending = getPending(requestId);
    if (pending == null || !pending.requestKey.equals(requestKey)) {
      throw new IllegalArgumentException("等待确认已失效，请重新登录");
    }
    if (pending.isExpired()) {
      pendingLogins.remove(requestId);
      throw new IllegalArgumentException("等待确认已过期，请重新登录");
    }
    if (pending.decision == Decision.REJECTED) {
      throw new IllegalArgumentException("该次登录已被拒绝");
    }
    if (pending.decision != Decision.APPROVED) {
      throw new IllegalArgumentException("等待对方确认中");
    }
    pendingLogins.remove(requestId);
    return pending;
  }

  private void publishLoginNotice(long loginId, PendingLogin pending) {
    List<SseEmitter> emitters = loginEmitters.get(loginId);
    if (emitters == null || emitters.isEmpty()) return;
    ConcurrentLoginEvent event = new ConcurrentLoginEvent(
      pending.requestId,
      pending.deviceInfo,
      pending.ipAddress,
      pending.loginLocation,
      LocalDateTime.now().format(TIME_FORMAT)
    );
    for (SseEmitter emitter : emitters) {
      try {
        emitter.send(SseEmitter.event().name("concurrent-login").data(event));
      } catch (Exception ex) {
        removeLoginEmitter(loginId, emitter);
      }
    }
  }

  private void notifyDecision(PendingLogin pending) {
    if (pending.decisionEmitters.isEmpty()) return;
    for (SseEmitter emitter : pending.decisionEmitters) {
      sendDecisionEvent(emitter, pending.decision);
    }
    pending.decisionEmitters.clear();
  }

  private void sendDecisionEvent(SseEmitter emitter, Decision decision) {
    try {
      String status = decision == Decision.APPROVED ? "approved" : "rejected";
      emitter.send(SseEmitter.event().name("decision").data(new ConcurrentLoginDecisionEvent(status)));
      emitter.complete();
    } catch (Exception ignored) {}
  }

  private void removeLoginEmitter(long loginId, SseEmitter emitter) {
    cancelLoginHeartbeat(emitter);
    CopyOnWriteArrayList<SseEmitter> list = loginEmitters.get(loginId);
    if (list == null) return;
    list.remove(emitter);
    if (list.isEmpty()) {
      loginEmitters.remove(loginId);
    }
  }

  private void sendConnectedEvent(long loginId, SseEmitter emitter) {
    try {
      emitter.send(SseEmitter.event().name("connected").data(Map.of("ts", System.currentTimeMillis())));
    } catch (Exception ex) {
      removeLoginEmitter(loginId, emitter);
    }
  }

  private void startLoginHeartbeat(long loginId, SseEmitter emitter) {
    cancelLoginHeartbeat(emitter);
    ScheduledFuture<?> task = heartbeatExecutor.scheduleAtFixedRate(
      () -> {
        try {
          emitter.send(SseEmitter.event().name("ping").data(Map.of("ts", System.currentTimeMillis())));
        } catch (Exception ex) {
          removeLoginEmitter(loginId, emitter);
        }
      },
      LOGIN_HEARTBEAT_INTERVAL_SECONDS,
      LOGIN_HEARTBEAT_INTERVAL_SECONDS,
      TimeUnit.SECONDS
    );
    loginHeartbeatTasks.put(emitter, task);
  }

  private void cancelLoginHeartbeat(SseEmitter emitter) {
    ScheduledFuture<?> task = loginHeartbeatTasks.remove(emitter);
    if (task != null) {
      task.cancel(true);
    }
  }

  private PendingLogin getPending(String requestId) {
    return pendingLogins.get(requestId);
  }

  public static class PendingLogin {
    private final String requestId;
    private final String requestKey;
    private final long loginId;
    private final String deviceModel;
    private final String os;
    private final String browser;
    private final String deviceInfo;
    private final String ipAddress;
    private final String loginLocation;
    private final long createdAt;
    private volatile Decision decision = Decision.PENDING;
    private final CopyOnWriteArrayList<SseEmitter> decisionEmitters = new CopyOnWriteArrayList<>();

    public PendingLogin(
      String requestId,
      String requestKey,
      long loginId,
      String deviceModel,
      String os,
      String browser,
      String deviceInfo,
      String ipAddress,
      String loginLocation,
      long createdAt
    ) {
      this.requestId = requestId;
      this.requestKey = requestKey;
      this.loginId = loginId;
      this.deviceModel = deviceModel;
      this.os = os;
      this.browser = browser;
      this.deviceInfo = deviceInfo;
      this.ipAddress = ipAddress;
      this.loginLocation = loginLocation;
      this.createdAt = createdAt;
    }

    public String getRequestId() {
      return requestId;
    }

    public String getRequestKey() {
      return requestKey;
    }

    public long getLoginId() {
      return loginId;
    }

    public String getDeviceModel() {
      return deviceModel;
    }

    public String getOs() {
      return os;
    }

    public String getBrowser() {
      return browser;
    }

    public String getDeviceInfo() {
      return deviceInfo;
    }

    public String getIpAddress() {
      return ipAddress;
    }

    public String getLoginLocation() {
      return loginLocation;
    }

    public boolean isExpired() {
      return System.currentTimeMillis() - createdAt > PENDING_TTL_MS;
    }
  }

  private enum Decision {
    PENDING,
    APPROVED,
    REJECTED
  }
}
