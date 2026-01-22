package com.tencent.tdesign.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserAdminServiceTest {

  @Test
  void rootAdminCannotBeManagedEvenBySelf() {
    assertThrows(
      IllegalArgumentException.class,
      () -> UserAdminService.validateManageableTarget(1L, 1L, "admin", true, true)
    );
  }

  @Test
  void rootAdminCannotBeManagedByOtherAdmin() {
    assertThrows(
      IllegalArgumentException.class,
      () -> UserAdminService.validateManageableTarget(2L, 1L, "ADMIN", true, true)
    );
  }

  @Test
  void adminRoleUserCanBeManagedByAdmin() {
    assertDoesNotThrow(() -> UserAdminService.validateManageableTarget(2L, 1L, "alice", true, true));
  }

  @Test
  void adminRoleUserCannotBeManagedByNonAdmin() {
    assertThrows(
      IllegalArgumentException.class,
      () -> UserAdminService.validateManageableTarget(2L, 1L, "alice", true, false)
    );
  }

  @Test
  void selfManageAllowedWhenNotRootAdmin() {
    assertDoesNotThrow(() -> UserAdminService.validateManageableTarget(1L, 1L, "alice", true, false));
  }
}

