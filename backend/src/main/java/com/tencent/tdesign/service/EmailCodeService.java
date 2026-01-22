package com.tencent.tdesign.service;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class EmailCodeService {
  private static class Entry {
    String code;
    long expiresAt;
  }

  private static final int CODE_EXPIRES_SECONDS = 300;
  private final Map<String, Entry> store = new ConcurrentHashMap<>();
  private final Random random = new Random();

  public int getExpiresInSeconds() {
    return CODE_EXPIRES_SECONDS;
  }

  public String generateCode(String email) {
    String code = String.format("%06d", random.nextInt(1000000));
    Entry entry = new Entry();
    entry.code = code;
    entry.expiresAt = Instant.now().plusSeconds(CODE_EXPIRES_SECONDS).toEpochMilli();
    store.put(email, entry);
    return code;
  }

  public boolean verify(String email, String code) {
    Entry entry = store.get(email);
    if (entry == null) return false;
    if (Instant.now().toEpochMilli() > entry.expiresAt) {
      store.remove(email);
      return false;
    }
    boolean isValid = entry.code.equals(code == null ? "" : code.trim());
    if (isValid) {
      store.remove(email);
    }
    return isValid;
  }

  public void invalidate(String email) {
    if (email == null || email.isBlank()) return;
    store.remove(email);
  }
}
