package com.tencent.tdesign.mapper;

import com.tencent.tdesign.entity.SecurityCaptchaSetting;

public interface SecurityCaptchaSettingMapper {
  SecurityCaptchaSetting selectTop();
  int insert(SecurityCaptchaSetting setting);
  int update(SecurityCaptchaSetting setting);
}
