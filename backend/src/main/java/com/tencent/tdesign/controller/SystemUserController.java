package com.tencent.tdesign.controller;

import com.tencent.tdesign.annotation.RepeatSubmit;
import com.tencent.tdesign.dto.UserCreateRequest;
import com.tencent.tdesign.dto.UserUpdateRequest;
import com.tencent.tdesign.security.AuthContext;
import com.tencent.tdesign.service.UserAdminService;
import com.tencent.tdesign.util.PermissionUtil;
import com.tencent.tdesign.vo.ApiResponse;
import com.tencent.tdesign.vo.PageResult;
import com.tencent.tdesign.vo.UserListItem;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/user")
public class SystemUserController {
  private final UserAdminService userAdminService;
  private final AuthContext authContext;

  public SystemUserController(UserAdminService userAdminService, AuthContext authContext) {
    this.userAdminService = userAdminService;
    this.authContext = authContext;
  }

  @GetMapping("/page")
  public ApiResponse<PageResult<UserListItem>> page(
    @RequestParam(required = false) String keyword,
    @RequestParam(required = false) String mobile,
    @RequestParam(required = false) Long orgUnitId,
    @RequestParam(required = false) Long departmentId,
    @RequestParam(required = false) Integer status,
    @RequestParam(required = false) String startTime,
    @RequestParam(required = false) String endTime,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) {
    PermissionUtil.check("system:SystemUser:query");
    java.time.LocalDateTime start = parseDateTime(startTime);
    java.time.LocalDateTime end = parseDateTime(endTime);
    return ApiResponse.success(userAdminService.page(keyword, mobile, orgUnitId, departmentId, status, start, end, page, size));
  }

  @GetMapping("/{id}")
  public ApiResponse<UserListItem> get(@PathVariable long id) {
    PermissionUtil.check("system:SystemUser:query");
    return ApiResponse.success(userAdminService.get(id));
  }

  @PostMapping
  @RepeatSubmit
  public ApiResponse<UserListItem> create(@RequestBody @Valid UserCreateRequest req) {
    PermissionUtil.check("system:SystemUser:create");
    return ApiResponse.success(userAdminService.create(req));
  }

  @PutMapping("/{id}")
  @RepeatSubmit
  public ApiResponse<UserListItem> update(@PathVariable long id, @RequestBody UserUpdateRequest req) {
    PermissionUtil.check("system:SystemUser:update");
    return ApiResponse.success(userAdminService.update(id, req));
  }

  @DeleteMapping("/{id}")
  @RepeatSubmit
  public ApiResponse<Boolean> delete(@PathVariable long id) {
    PermissionUtil.check("system:SystemUser:delete");
    long self = authContext.requireUserId();
    if (self == id) throw new IllegalArgumentException("不允许删除当前登录用户");
    return ApiResponse.success(userAdminService.delete(id));
  }

  @PostMapping("/{id}/reset-password")
  @RepeatSubmit
  public ApiResponse<Boolean> resetPassword(@PathVariable long id, @RequestParam(required = false) String password) {
    PermissionUtil.check("system:SystemUser:update");
    return ApiResponse.success(userAdminService.resetPassword(id, password));
  }

  private java.time.LocalDateTime parseDateTime(String value) {
    if (value == null || value.isBlank()) return null;
    try {
      return java.time.LocalDateTime.parse(value);
    } catch (Exception ignored) {
      try {
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return java.time.LocalDateTime.parse(value, formatter);
      } catch (Exception ignored2) {
        return null;
      }
    }
  }
}
