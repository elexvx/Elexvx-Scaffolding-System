package com.tencent.tdesign.controller;

import com.tencent.tdesign.service.MenuItemService;
import com.tencent.tdesign.vo.ApiResponse;
import com.tencent.tdesign.vo.MenuListResult;
import java.util.ArrayList;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MenuController {
  private final MenuItemService menuItemService;

  public MenuController(MenuItemService menuItemService) {
    this.menuItemService = menuItemService;
  }

  @GetMapping("/get-menu-list-i18n")
  public ApiResponse<MenuListResult> getMenuList() {
    try {
      if (menuItemService.isConfigured()) {
        return ApiResponse.success(new MenuListResult(menuItemService.getMenuRoutesForCurrentUser()));
      }
    } catch (Exception ignored) {
    }
    return ApiResponse.success(new MenuListResult(new ArrayList<>()));
  }
}
