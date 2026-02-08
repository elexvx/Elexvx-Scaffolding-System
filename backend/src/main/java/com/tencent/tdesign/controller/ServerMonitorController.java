package com.tencent.tdesign.controller;

import com.tencent.tdesign.service.ServerMonitorService;
import com.tencent.tdesign.util.PermissionUtil;
import com.tencent.tdesign.vo.ApiResponse;
import com.tencent.tdesign.vo.ServerInfoVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/monitor")
public class ServerMonitorController {

  private final ServerMonitorService serverMonitorService;

  public ServerMonitorController(ServerMonitorService serverMonitorService) {
    this.serverMonitorService = serverMonitorService;
  }

  @GetMapping("/server")
  public ApiResponse<ServerInfoVO> getServerInfo() {
    PermissionUtil.checkAdmin();
    return ApiResponse.success(serverMonitorService.getServerInfo());
  }
}
