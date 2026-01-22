package com.tencent.tdesign.mapper;

import com.tencent.tdesign.entity.UiBrandSetting;

public interface UiBrandSettingMapper {
  UiBrandSetting selectTop();
  int insert(UiBrandSetting setting);
  int update(UiBrandSetting setting);
}
