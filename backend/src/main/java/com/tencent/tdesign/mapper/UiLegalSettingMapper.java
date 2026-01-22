package com.tencent.tdesign.mapper;

import com.tencent.tdesign.entity.UiLegalSetting;

public interface UiLegalSettingMapper {
  UiLegalSetting selectTop();
  int insert(UiLegalSetting setting);
  int update(UiLegalSetting setting);
}
