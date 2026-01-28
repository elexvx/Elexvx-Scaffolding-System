package com.tencent.tdesign.controller;

import com.tencent.tdesign.annotation.RepeatSubmit;
import com.tencent.tdesign.dto.FileResourceRequest;
import com.tencent.tdesign.service.FileDownloadService;
import com.tencent.tdesign.vo.ApiResponse;
import com.tencent.tdesign.vo.FileResourceResponse;
import com.tencent.tdesign.vo.PageResult;
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
@RequestMapping("/system/file/resource")
public class DownloadController {
  private final FileDownloadService fileDownloadService;

  public DownloadController(FileDownloadService fileDownloadService) {
    this.fileDownloadService = fileDownloadService;
  }

  @GetMapping
  public ApiResponse<PageResult<FileResourceResponse>> list(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) {
    return ApiResponse.success(fileDownloadService.page(page, size));
  }

  @PostMapping
  @RepeatSubmit
  public ApiResponse<FileResourceResponse> create(@RequestBody @Valid FileResourceRequest req) {
    return ApiResponse.success(fileDownloadService.create(req));
  }

  @PutMapping("/{id}")
  @RepeatSubmit
  public ApiResponse<FileResourceResponse> update(@PathVariable Long id, @RequestBody @Valid FileResourceRequest req) {
    return ApiResponse.success(fileDownloadService.update(id, req));
  }

  @DeleteMapping("/{id}")
  @RepeatSubmit
  public ApiResponse<Boolean> delete(@PathVariable Long id) {
    return ApiResponse.success(fileDownloadService.delete(id));
  }
}
