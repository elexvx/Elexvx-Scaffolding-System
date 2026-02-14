package com.tencent.tdesign.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccessControlServiceTest {
  @Test
  void adminShouldPassRoleAndPermissionChecksWithoutSetLookup() {
    PermissionCache cache = mock(PermissionCache.class);
    when(cache.isAdmin()).thenReturn(true);

    AccessControlService service = new AccessControlService(cache);

    assertTrue(service.hasRole("any"));
    assertTrue(service.hasPermission("perm:x"));
    verify(cache).isAdmin();
  }

  @Test
  void nonAdminShouldUseCacheContains() {
    PermissionCache cache = mock(PermissionCache.class);
    when(cache.isAdmin()).thenReturn(false);
    when(cache.hasRole("manager")).thenReturn(true);
    when(cache.hasPermission("perm:a")).thenReturn(false);

    AccessControlService service = new AccessControlService(cache);

    assertTrue(service.hasRole("manager"));
    assertFalse(service.hasPermission("perm:a"));
    verify(cache).hasRole("manager");
    verify(cache).hasPermission("perm:a");
  }
}
