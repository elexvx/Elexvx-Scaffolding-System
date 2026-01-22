package com.tencent.tdesign.mapper;

import com.tencent.tdesign.entity.VerificationSmsSetting;

public interface VerificationSmsSettingMapper {
  VerificationSmsSetting selectTop();
  int insert(VerificationSmsSetting setting);
  int update(VerificationSmsSetting setting);
}
