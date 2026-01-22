package com.tencent.tdesign.dto;

import java.util.List;

public class RoleSwitchRequest {
  private List<String> roles;

  public List<String> getRoles() {
    return roles;
  }

  public void setRoles(List<String> roles) {
    this.roles = roles;
  }
}
