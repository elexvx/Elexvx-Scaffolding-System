package com.tencent.tdesign.verification;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class VerificationProviderRegistry {
  private final Map<String, VerificationProvider> providers;

  public VerificationProviderRegistry(List<VerificationProvider> providers) {
    Map<String, VerificationProvider> map = new LinkedHashMap<>();
    if (providers != null) {
      for (VerificationProvider provider : providers) {
        if (provider == null || provider.getType() == null) continue;
        map.put(provider.getType().trim().toLowerCase(), provider);
      }
    }
    this.providers = Collections.unmodifiableMap(map);
  }

  public VerificationProvider require(String type) {
    if (type == null || type.isBlank()) {
      throw new IllegalArgumentException("验证码类型不能为空");
    }
    VerificationProvider provider = providers.get(type.trim().toLowerCase());
    if (provider == null) {
      throw new IllegalArgumentException("验证码模块未配置: " + type);
    }
    return provider;
  }

  public Map<String, VerificationProvider> getProviders() {
    return providers;
  }
}
