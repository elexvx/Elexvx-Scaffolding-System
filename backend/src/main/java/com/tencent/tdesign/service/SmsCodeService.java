package com.tencent.tdesign.service;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class SmsCodeService {
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

  public String generateCode(String phone) {
    String code = String.format("%06d", random.nextInt(1000000));
    Entry entry = new Entry();
    entry.code = code;
    entry.expiresAt = Instant.now().plusSeconds(CODE_EXPIRES_SECONDS).toEpochMilli();
    store.put(phone, entry);
    return code;
  }

  public boolean verify(String phone, String code) {
    Entry entry = store.get(phone);
    if (entry == null) return false;
    store.remove(phone);
    if (Instant.now().toEpochMilli() > entry.expiresAt) return false;
    return entry.code.equals(code == null ? "" : code.trim());
  }

  public void invalidate(String phone) {
    if (phone == null || phone.isBlank()) return;
    store.remove(phone);
  }
}
