package com.tencent.tdesign.controller;

import com.tencent.tdesign.dto.FieldColumnRequest;
import com.tencent.tdesign.service.FieldMetadataService;
import com.tencent.tdesign.service.SystemUpdateService;
import com.tencent.tdesign.vo.ApiResponse;
import com.tencent.tdesign.vo.DatabaseMetadataResponse;
import com.tencent.tdesign.vo.UpdateCheckResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system")
public class FieldMetadataController {
  private final FieldMetadataService fieldMetadataService;
  private final SystemUpdateService systemUpdateService;

  public FieldMetadataController(FieldMetadataService fieldMetadataService, SystemUpdateService systemUpdateService) {
    this.fieldMetadataService = fieldMetadataService;
    this.systemUpdateService = systemUpdateService;
  }

  @GetMapping("/fields/metadata")
  public ApiResponse<DatabaseMetadataResponse> getMetadata() {
    return ApiResponse.success(fieldMetadataService.loadMetadata());
  }

  @PostMapping("/fields/columns/update")
  public ApiResponse<Void> updateColumn(@Valid @RequestBody FieldColumnRequest request) {
    fieldMetadataService.updateColumn(request);
    return ApiResponse.success(null);
  }

  @GetMapping("/update/check")
  public ApiResponse<UpdateCheckResponse> checkUpdate() {
    return ApiResponse.success(systemUpdateService.checkForUpdates());
  }
}
