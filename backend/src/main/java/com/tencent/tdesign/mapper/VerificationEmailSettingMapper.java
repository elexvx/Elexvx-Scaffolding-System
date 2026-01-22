package com.tencent.tdesign.mapper;

import com.tencent.tdesign.entity.VerificationEmailSetting;

public interface VerificationEmailSettingMapper {
  VerificationEmailSetting selectTop();
  int insert(VerificationEmailSetting setting);
  int update(VerificationEmailSetting setting);
}
