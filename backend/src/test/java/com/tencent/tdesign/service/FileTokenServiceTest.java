package com.tencent.tdesign.service;

import com.tencent.tdesign.entity.StorageSetting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FileTokenServiceTest {
  @Test
  void shouldRejectExpiredToken() throws InterruptedException {
    FileTokenService service = new FileTokenService("super-secure-secret-32-bytes-minimum!!", "/api", 1);
    String url = service.buildAccessUrl(StorageSetting.Provider.LOCAL, "business/2026/a.pdf");
    String token = service.extractToken(url);
    Thread.sleep(1100);
    assertThrows(IllegalArgumentException.class, () -> service.decrypt(token));
  }
}
