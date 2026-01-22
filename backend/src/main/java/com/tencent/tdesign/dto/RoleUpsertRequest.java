package com.tencent.tdesign.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class RoleUpsertRequest {
  @NotBlank(message = "角色名不能为空")
  private String name;

  private String description;
  private List<String> permissions;
  private List<Long> menuIds;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<String> getPermissions() {
    return permissions;
  }

  public void setPermissions(List<String> permissions) {
    this.permissions = permissions;
  }

  public List<Long> getMenuIds() {
    return menuIds;
  }

  public void setMenuIds(List<Long> menuIds) {
    this.menuIds = menuIds;
  }
}

