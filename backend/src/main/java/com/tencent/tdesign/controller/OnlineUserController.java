package com.tencent.tdesign.controller;

import com.tencent.tdesign.service.OnlineUserService;
import com.tencent.tdesign.service.OperationLogService;
import com.tencent.tdesign.security.AccessControlService;
import com.tencent.tdesign.vo.ApiResponse;
import com.tencent.tdesign.vo.OnlineUserVO;
import com.tencent.tdesign.vo.PageResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/monitor")
public class OnlineUserController {
  private final OnlineUserService onlineUserService;
  private final OperationLogService operationLogService;
  private final AccessControlService accessControlService;

  public OnlineUserController(
    OnlineUserService onlineUserService,
    OperationLogService operationLogService,
    AccessControlService accessControlService
  ) {
    this.onlineUserService = onlineUserService;
    this.operationLogService = operationLogService;
    this.accessControlService = accessControlService;
  }

  @GetMapping("/online")
  public ApiResponse<PageResult<OnlineUserVO>> getOnlineUsers(
    @RequestParam(required = false) String loginAddress,
    @RequestParam(required = false) String userName,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) {
    accessControlService.checkRole("admin");
    return ApiResponse.success(onlineUserService.getOnlineUsers(loginAddress, userName, page, size));
  }

  @DeleteMapping("/online/{sessionId}")
  public ApiResponse<Boolean> forceLogout(@PathVariable String sessionId) {
    accessControlService.checkRole("admin");
    OnlineUserVO target = onlineUserService.getOnlineUser(sessionId);
    boolean ok = onlineUserService.forceLogout(sessionId);
    if (ok) {
      String detail = target != null ? "强制下线: " + target.getLoginName() : "强制下线会话: " + sessionId;
      operationLogService.log("DELETE", "在线用户", detail);
    }
    return ApiResponse.success(ok);
  }
}
