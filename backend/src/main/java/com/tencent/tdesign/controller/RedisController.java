package com.tencent.tdesign.controller;

import com.tencent.tdesign.service.RedisService;
import com.tencent.tdesign.util.PermissionUtil;
import com.tencent.tdesign.vo.ApiResponse;
import com.tencent.tdesign.vo.RedisInfoVO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/monitor")
@ConditionalOnProperty(name = "tdesign.redis.enabled", havingValue = "true")
public class RedisController {

  private final RedisService redisService;

  public RedisController(RedisService redisService) {
    this.redisService = redisService;
  }

  @GetMapping("/redis")
  public ApiResponse<RedisInfoVO> getRedisInfo() {
    PermissionUtil.checkAdmin();
    return ApiResponse.success(redisService.getRedisInfo());
  }
}
