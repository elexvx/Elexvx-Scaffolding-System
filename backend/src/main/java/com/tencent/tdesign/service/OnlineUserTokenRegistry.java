package com.tencent.tdesign.service;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class OnlineUserTokenRegistry {

  private final Set<String> tokens = ConcurrentHashMap.newKeySet();

  public void addToken(String token) {
    if (token == null || token.isBlank()) return;
    tokens.add(token);
  }

  public void removeToken(String token) {
    if (token == null || token.isBlank()) return;
    tokens.remove(token);
  }

  public Set<String> snapshotTokens() {
    return new LinkedHashSet<>(tokens);
  }
}

