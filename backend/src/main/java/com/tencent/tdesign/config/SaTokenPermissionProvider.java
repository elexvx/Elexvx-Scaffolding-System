package com.tencent.tdesign.config;

import cn.dev33.satoken.stp.StpInterface;
import com.tencent.tdesign.service.PermissionFacade;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SaTokenPermissionProvider implements StpInterface {
  private final PermissionFacade permissionFacade;

  public SaTokenPermissionProvider(PermissionFacade permissionFacade) {
    this.permissionFacade = permissionFacade;
  }

  @Override
  public List<String> getPermissionList(Object loginId, String loginType) {
    long userId = Long.parseLong(String.valueOf(loginId));
    return permissionFacade.getEffectivePermissions(userId);
  }

  @Override
  public List<String> getRoleList(Object loginId, String loginType) {
    long userId = Long.parseLong(String.valueOf(loginId));
    return permissionFacade.getEffectiveRoles(userId);
  }
}
