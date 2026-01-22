package com.tencent.tdesign.mapper;

import com.tencent.tdesign.entity.UiLayoutSetting;

public interface UiLayoutSettingMapper {
  UiLayoutSetting selectTop();
  int insert(UiLayoutSetting setting);
  int update(UiLayoutSetting setting);
}
