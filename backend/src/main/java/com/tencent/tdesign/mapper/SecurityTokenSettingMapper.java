package com.tencent.tdesign.mapper;

import com.tencent.tdesign.entity.SecurityTokenSetting;

public interface SecurityTokenSettingMapper {
  SecurityTokenSetting selectTop();
  int insert(SecurityTokenSetting setting);
  int update(SecurityTokenSetting setting);
}
