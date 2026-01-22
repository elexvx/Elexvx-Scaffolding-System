package com.tencent.tdesign.controller;

import com.tencent.tdesign.dto.WatermarkSettingRequest;
import com.tencent.tdesign.entity.WatermarkSetting;
import com.tencent.tdesign.mapper.WatermarkSettingMapper;
import com.tencent.tdesign.annotation.RepeatSubmit;
import com.tencent.tdesign.service.OperationLogService;
import com.tencent.tdesign.util.PermissionUtil;
import com.tencent.tdesign.vo.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/watermark")
public class WatermarkController {
  private final WatermarkSettingMapper mapper;
  private final OperationLogService operationLogService;

  public WatermarkController(WatermarkSettingMapper mapper, OperationLogService operationLogService) {
    this.mapper = mapper;
    this.operationLogService = operationLogService;
  }

  @GetMapping
  public ApiResponse<WatermarkSetting> get() {
    // 允许所有登录用户获取水印配置
    WatermarkSetting setting = mapper.selectTop();
    return ApiResponse.success(setting == null ? new WatermarkSetting() : setting);
  }

  @PostMapping
  @RepeatSubmit
  public ApiResponse<WatermarkSetting> save(@RequestBody @Valid WatermarkSettingRequest req) {
    PermissionUtil.checkAdmin();
    WatermarkSetting s = mapper.selectTop();
    if (s == null) s = new WatermarkSetting();
    s.setType(req.getType());
    s.setContent(req.getContent());
    s.setImageUrl(req.getImageUrl());
    s.setOpacity(req.getOpacity());
    s.setSize(req.getSize());
    s.setGapX(req.getGapX());
    s.setGapY(req.getGapY());
    s.setRotate(req.getRotate());
    s.setEnabled(req.getEnabled());
    if (s.getId() == null) mapper.insert(s);
    else mapper.update(s);
    operationLogService.log("UPDATE", "水印设置", "更新水印配置");
    return ApiResponse.success(s);
  }
}
