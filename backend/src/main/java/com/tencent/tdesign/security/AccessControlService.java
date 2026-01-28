package com.tencent.tdesign.security;

import com.tencent.tdesign.service.PermissionFacade;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class AccessControlService {
  private final AuthContext authContext;
  private final PermissionFacade permissionFacade;

  public AccessControlService(AuthContext authContext, PermissionFacade permissionFacade) {
    this.authContext = authContext;
    this.permissionFacade = permissionFacade;
  }

  public boolean hasRole(String role) {
    if (role == null || role.isBlank()) return false;
    long userId = authContext.requireUserId();
    List<String> roles = permissionFacade.getEffectiveRoles(userId);
    return roles.stream().anyMatch(r -> r.equalsIgnoreCase(role));
  }

  public boolean hasPermission(String permission) {
    if (permission == null || permission.isBlank()) return false;
    long userId = authContext.requireUserId();
    List<String> permissions = permissionFacade.getEffectivePermissions(userId);
    return permissions.contains(permission);
  }

  public void checkRole(String role) {
    if (!hasRole(role)) {
      throw new AccessDeniedException("权限不足，请联系管理员开通");
    }
  }

  public void checkPermission(String permission) {
    if (!hasPermission(permission)) {
      throw new AccessDeniedException("权限不足，请联系管理员开通");
    }
  }
}
