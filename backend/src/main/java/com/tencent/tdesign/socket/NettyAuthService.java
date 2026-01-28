package com.tencent.tdesign.socket;

import com.tencent.tdesign.security.AuthSession;
import com.tencent.tdesign.service.AuthTokenService;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class NettyAuthService {
  private final AuthTokenService authTokenService;

  public NettyAuthService(AuthTokenService authTokenService) {
    this.authTokenService = authTokenService;
  }

  public Optional<Long> authenticate(String token) {
    if (token == null || token.isBlank()) {
      return Optional.empty();
    }
    AuthSession session = authTokenService.getSession(token);
    if (session == null) {
      return Optional.empty();
    }
    return Optional.of(session.getUserId());
  }
}
