package com.tencent.tdesign.util;

public final class PermissionUtil {
  private PermissionUtil() {}

  public static void check(String permission) {
    // CRUD 级权限控制已移除，仅保留页面访问权限（菜单）控制。
  }

  public static void checkAny(String... permissions) {
    // CRUD 级权限控制已移除，仅保留页面访问权限（菜单）控制。
  }

  public static void checkAdmin() {
    // 仅保留页面访问权限（菜单）控制，不再做额外角色校验。
  }
}
