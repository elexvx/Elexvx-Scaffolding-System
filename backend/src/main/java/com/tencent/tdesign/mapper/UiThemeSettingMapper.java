package com.tencent.tdesign.mapper;

import com.tencent.tdesign.entity.UiThemeSetting;

public interface UiThemeSettingMapper {
  UiThemeSetting selectTop();
  int insert(UiThemeSetting setting);
  int update(UiThemeSetting setting);
}
