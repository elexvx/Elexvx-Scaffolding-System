package com.tencent.tdesign.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class SecurityBaselineValidator {
  @Value("${tdesign.web.cors.allowed-origin-patterns:}")
  private String allowedOrigins;

  @PostConstruct
  public void validateProdSecurityBaseline() {
    String v = allowedOrigins == null ? "" : allowedOrigins.trim();
    if (v.isEmpty() || "*".equals(v) || v.contains("*")) {
      throw new IllegalStateException("生产环境必须显式配置 tdesign.web.cors.allowed-origin-patterns，且禁止使用通配符");
    }
  }
}
