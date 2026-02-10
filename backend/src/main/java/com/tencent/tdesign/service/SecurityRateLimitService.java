package com.tencent.tdesign.service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class SecurityRateLimitService {
  private final RedisTemplate<String, Object> redisTemplate;
  private final HttpServletRequest request;
  private final int loginPerMinute;
  private final int loginFailThreshold;
  private final int smsPerMinute;
  private final int smsPerDay;

  public SecurityRateLimitService(
    RedisTemplate<String, Object> redisTemplate,
    HttpServletRequest request,
    @Value("${tdesign.security.rate-limit.login-per-minute:10}") int loginPerMinute,
    @Value("${tdesign.security.rate-limit.login-fail-threshold:5}") int loginFailThreshold,
    @Value("${tdesign.security.rate-limit.sms-email-per-minute:3}") int smsPerMinute,
    @Value("${tdesign.security.rate-limit.sms-email-per-day:20}") int smsPerDay
  ) {
    this.redisTemplate = redisTemplate;
    this.request = request;
    this.loginPerMinute = loginPerMinute;
    this.loginFailThreshold = loginFailThreshold;
    this.smsPerMinute = smsPerMinute;
    this.smsPerDay = smsPerDay;
  }

  public void checkLoginAttempt(String account) {
    String ip = clientIp();
    requireQuota("sec:login:ip:" + ip, loginPerMinute, Duration.ofMinutes(1), "登录过于频繁，请稍后重试");
    requireQuota("sec:login:acct:" + normalize(account), loginPerMinute, Duration.ofMinutes(1), "登录过于频繁，请稍后重试");

    long failCount = getCount("sec:login:fail:" + ip + ":" + normalize(account));
    if (failCount >= loginFailThreshold) {
      throw new IllegalArgumentException("账号或密码错误");
    }
  }

  public void recordLoginFailure(String account) {
    String key = "sec:login:fail:" + clientIp() + ":" + normalize(account);
    long v = increment(key, Duration.ofMinutes(15));
    if (v > loginFailThreshold) {
      long lockSeconds = Math.min(900, (long) Math.pow(2, (v - loginFailThreshold)) * 5L);
      redisTemplate.expire(key, Duration.ofSeconds(lockSeconds));
    }
  }

  public void clearLoginFailures(String account) {
    redisTemplate.delete("sec:login:fail:" + clientIp() + ":" + normalize(account));
  }

  public void checkVerificationSendQuota(String principal, String channel) {
    String ip = clientIp();
    String p = normalize(principal);
    String day = LocalDate.now().toString();
    requireQuota("sec:send:" + channel + ":ip:min:" + ip, smsPerMinute, Duration.ofMinutes(1), "请求过于频繁，请稍后重试");
    requireQuota("sec:send:" + channel + ":principal:min:" + p, smsPerMinute, Duration.ofMinutes(1), "请求过于频繁，请稍后重试");
    requireQuota("sec:send:" + channel + ":ip:day:" + ip + ":" + day, smsPerDay, Duration.ofDays(2), "今日请求次数已达上限");
    requireQuota("sec:send:" + channel + ":principal:day:" + p + ":" + day, smsPerDay, Duration.ofDays(2), "今日请求次数已达上限");
  }

  private void requireQuota(String key, int limit, Duration window, String message) {
    long count = increment(key, window);
    if (count > limit) {
      throw new IllegalArgumentException(message);
    }
  }

  private long increment(String key, Duration ttl) {
    Long value = redisTemplate.opsForValue().increment(key);
    if (value != null && value == 1L) {
      redisTemplate.expire(key, ttl);
    }
    return value == null ? 0L : value;
  }

  private long getCount(String key) {
    Object v = redisTemplate.opsForValue().get(key);
    if (v == null) return 0L;
    try {
      return Long.parseLong(String.valueOf(v));
    } catch (Exception ignore) {
      return 0L;
    }
  }

  private String clientIp() {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    String realIp = request.getHeader("X-Real-IP");
    if (realIp != null && !realIp.isBlank()) return realIp.trim();
    return request.getRemoteAddr();
  }

  private String normalize(String value) {
    return value == null ? "" : value.trim().toLowerCase();
  }
}
