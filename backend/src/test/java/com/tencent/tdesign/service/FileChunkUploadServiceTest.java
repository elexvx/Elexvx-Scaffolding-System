package com.tencent.tdesign.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tdesign.security.AuthContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class FileChunkUploadServiceTest {
  @Test
  void shouldRejectTraversalLikeUploadId() {
    FileChunkUploadService service = new FileChunkUploadService(
      mock(ObjectStorageService.class),
      new ObjectMapper(),
      mock(AuthContext.class),
      "abcdefghijklmnopqrstuvwxyz123456"
    );

    assertThrows(IllegalArgumentException.class, () -> service.getStatus("../../etc/passwd"));
  }
}
