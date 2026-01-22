package com.tencent.tdesign.mapper;

import com.tencent.tdesign.entity.SecurityPasswordPolicy;

public interface SecurityPasswordPolicyMapper {
  SecurityPasswordPolicy selectTop();
  int insert(SecurityPasswordPolicy setting);
  int update(SecurityPasswordPolicy setting);
}
