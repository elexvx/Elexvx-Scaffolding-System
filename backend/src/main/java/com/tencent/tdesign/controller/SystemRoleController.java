package com.tencent.tdesign.controller;

import com.tencent.tdesign.dto.RoleUpsertRequest;
import com.tencent.tdesign.annotation.RepeatSubmit;
import com.tencent.tdesign.service.RoleAdminService;
import com.tencent.tdesign.util.PermissionUtil;
import com.tencent.tdesign.vo.ApiResponse;
import com.tencent.tdesign.vo.RoleResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/role")
public class SystemRoleController {
  private final RoleAdminService roleAdminService;

  public SystemRoleController(RoleAdminService roleAdminService) {
    this.roleAdminService = roleAdminService;
  }

  @GetMapping("/list")
  public ApiResponse<List<RoleResponse>> list() {
    PermissionUtil.check("system:SystemRole:query");
    return ApiResponse.success(roleAdminService.list());
  }

  @GetMapping("/{id}")
  public ApiResponse<RoleResponse> get(@PathVariable long id) {
    PermissionUtil.check("system:SystemRole:query");
    return ApiResponse.success(roleAdminService.get(id));
  }

  @PostMapping
  @RepeatSubmit
  public ApiResponse<RoleResponse> create(@RequestBody @Valid RoleUpsertRequest req) {
    PermissionUtil.check("system:SystemRole:create");
    return ApiResponse.success(roleAdminService.create(req));
  }

  @PutMapping("/{id}")
  @RepeatSubmit
  public ApiResponse<RoleResponse> update(@PathVariable long id, @RequestBody RoleUpsertRequest req) {
    PermissionUtil.check("system:SystemRole:update");
    return ApiResponse.success(roleAdminService.update(id, req));
  }

  @DeleteMapping("/{id}")
  @RepeatSubmit
  public ApiResponse<Boolean> delete(@PathVariable long id) {
    PermissionUtil.check("system:SystemRole:delete");
    return ApiResponse.success(roleAdminService.delete(id));
  }
}
