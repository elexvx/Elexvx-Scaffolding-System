package com.tencent.tdesign.controller;

import com.tencent.tdesign.service.AreaService;
import com.tencent.tdesign.vo.ApiResponse;
import com.tencent.tdesign.vo.AreaNodeResponse;
import com.tencent.tdesign.vo.AreaPathNode;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/area")
public class AreaController {
  private final AreaService areaService;

  public AreaController(AreaService areaService) {
    this.areaService = areaService;
  }

  @GetMapping("/children")
  public ApiResponse<List<AreaNodeResponse>> listChildren(@RequestParam(defaultValue = "0") Integer parentId) {
    return ApiResponse.success(areaService.listChildren(parentId));
  }

  @GetMapping("/path")
  public ApiResponse<List<AreaPathNode>> getPath(@RequestParam Integer areaId) {
    return ApiResponse.success(areaService.getPath(areaId));
  }

  @GetMapping("/resolve")
  public ApiResponse<List<AreaPathNode>> resolve(
    @RequestParam(required = false) String province,
    @RequestParam(required = false) String city,
    @RequestParam(required = false) String district,
    @RequestParam(required = false) String town,
    @RequestParam(required = false) String street
  ) {
    return ApiResponse.success(areaService.resolvePath(province, city, district, town, street));
  }
}
