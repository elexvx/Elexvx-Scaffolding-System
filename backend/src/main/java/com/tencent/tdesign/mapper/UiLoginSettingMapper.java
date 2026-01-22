package com.tencent.tdesign.mapper;

import com.tencent.tdesign.entity.UiLoginSetting;

public interface UiLoginSettingMapper {
  UiLoginSetting selectTop();
  int insert(UiLoginSetting setting);
  int update(UiLoginSetting setting);
}
