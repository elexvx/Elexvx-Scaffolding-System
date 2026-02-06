package com.tencent.tdesign.controller;

import com.tencent.tdesign.service.ModuleRegistryService;
import com.tencent.tdesign.vo.ApiResponse;
import com.tencent.tdesign.vo.ModuleDescriptor;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/modules/descriptor")
public class SystemModuleController {
  private final ModuleRegistryService moduleRegistryService;

  public SystemModuleController(ModuleRegistryService moduleRegistryService) {
    this.moduleRegistryService = moduleRegistryService;
  }

  @GetMapping
  public ApiResponse<List<ModuleDescriptor>> list() {
    return ApiResponse.success(moduleRegistryService.listModules());
  }
}
