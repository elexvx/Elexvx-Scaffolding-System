package com.tencent.tdesign.mapper;

import com.tencent.tdesign.entity.UiFooterSetting;

public interface UiFooterSettingMapper {
  UiFooterSetting selectTop();
  int insert(UiFooterSetting setting);
  int update(UiFooterSetting setting);
}
