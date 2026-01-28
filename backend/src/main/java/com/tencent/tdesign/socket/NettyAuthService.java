package com.tencent.tdesign.socket;

import cn.dev33.satoken.stp.StpUtil;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class NettyAuthService {
  public Optional<Long> authenticate(String token) {
    if (token == null || token.isBlank()) {
      return Optional.empty();
    }
    try {
      Object loginId = StpUtil.getLoginIdByToken(token);
      if (loginId != null) {
        return Optional.of(Long.parseLong(String.valueOf(loginId)));
      }
    } catch (Exception ignored) {
      return Optional.empty();
    }
    return Optional.empty();
  }
}
