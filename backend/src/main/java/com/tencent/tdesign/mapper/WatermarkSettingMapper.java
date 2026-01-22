package com.tencent.tdesign.mapper;

import com.tencent.tdesign.entity.WatermarkSetting;

public interface WatermarkSettingMapper {
  WatermarkSetting selectTop();
  int insert(WatermarkSetting setting);
  int update(WatermarkSetting setting);
}
