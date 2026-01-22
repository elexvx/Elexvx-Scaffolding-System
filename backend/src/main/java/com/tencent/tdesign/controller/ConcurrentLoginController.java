package com.tencent.tdesign.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.tencent.tdesign.annotation.RepeatSubmit;
import com.tencent.tdesign.dto.ConcurrentLoginDecisionRequest;
import com.tencent.tdesign.service.ConcurrentLoginService;
import com.tencent.tdesign.vo.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/auth")
public class ConcurrentLoginController {
  private final ConcurrentLoginService concurrentLoginService;

  public ConcurrentLoginController(ConcurrentLoginService concurrentLoginService) {
    this.concurrentLoginService = concurrentLoginService;
  }

  @GetMapping(value = "/concurrent/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribeConcurrentLogin() {
    StpUtil.checkLogin();
    long userId = StpUtil.getLoginIdAsLong();
    return concurrentLoginService.subscribeLoginNotice(userId);
  }

  @PostMapping("/concurrent/decision")
  @RepeatSubmit
  public ApiResponse<Boolean> decide(@RequestBody @Valid ConcurrentLoginDecisionRequest req) {
    StpUtil.checkLogin();
    long userId = StpUtil.getLoginIdAsLong();
    String action = req.getAction();
    boolean approve;
    if ("approve".equalsIgnoreCase(action)) approve = true;
    else if ("reject".equalsIgnoreCase(action)) approve = false;
    else throw new IllegalArgumentException("action仅支持 approve 或 reject");

    if (approve) {
      StpUtil.replaced(userId, null);
    }
    concurrentLoginService.decide(userId, req.getRequestId(), approve);
    return ApiResponse.success(true);
  }

  @GetMapping(value = "/login/pending/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribePendingDecision(
    @RequestParam String requestId,
    @RequestParam String requestKey
  ) {
    return concurrentLoginService.subscribeDecision(requestId, requestKey);
  }
}
