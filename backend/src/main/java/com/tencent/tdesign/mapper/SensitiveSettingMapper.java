package com.tencent.tdesign.mapper;

import com.tencent.tdesign.entity.SensitiveSetting;

public interface SensitiveSettingMapper {
  SensitiveSetting selectTop();
  int insert(SensitiveSetting setting);
  int update(SensitiveSetting setting);
}
