package com.tencent.tdesign.controller;

import com.tencent.tdesign.annotation.RepeatSubmit;
import com.tencent.tdesign.dto.OrgUnitReorderRequest;
import com.tencent.tdesign.dto.OrgUnitUpsertRequest;
import com.tencent.tdesign.service.OrgUnitService;
import com.tencent.tdesign.util.PermissionUtil;
import com.tencent.tdesign.vo.ApiResponse;
import com.tencent.tdesign.vo.OrgUnitNode;
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
@RequestMapping("/system/org")
public class OrgUnitController {
  private final OrgUnitService orgUnitService;

  public OrgUnitController(OrgUnitService orgUnitService) {
    this.orgUnitService = orgUnitService;
  }

  @GetMapping("/tree")
  public ApiResponse<List<OrgUnitNode>> tree() {
    PermissionUtil.check("system:SystemOrg:query");
    return ApiResponse.success(orgUnitService.tree());
  }

  @GetMapping("/{id}")
  public ApiResponse<OrgUnitNode> get(@PathVariable long id) {
    PermissionUtil.check("system:SystemOrg:query");
    return ApiResponse.success(orgUnitService.get(id));
  }

  @PostMapping
  @RepeatSubmit
  public ApiResponse<OrgUnitNode> create(@RequestBody @Valid OrgUnitUpsertRequest req) {
    PermissionUtil.check("system:SystemOrg:create");
    return ApiResponse.success(orgUnitService.create(req));
  }

  @PutMapping("/{id}")
  @RepeatSubmit
  public ApiResponse<OrgUnitNode> update(@PathVariable long id, @RequestBody @Valid OrgUnitUpsertRequest req) {
    PermissionUtil.check("system:SystemOrg:update");
    return ApiResponse.success(orgUnitService.update(id, req));
  }

  @DeleteMapping("/{id}")
  @RepeatSubmit
  public ApiResponse<Boolean> delete(@PathVariable long id) {
    PermissionUtil.check("system:SystemOrg:delete");
    return ApiResponse.success(orgUnitService.delete(id));
  }

  @PutMapping("/reorder")
  @RepeatSubmit
  public ApiResponse<Boolean> reorder(@RequestBody @Valid OrgUnitReorderRequest req) {
    PermissionUtil.check("system:SystemOrg:update");
    return ApiResponse.success(orgUnitService.reorder(req));
  }
}
