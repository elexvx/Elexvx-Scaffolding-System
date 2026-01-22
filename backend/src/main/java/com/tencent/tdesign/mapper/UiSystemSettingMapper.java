package com.tencent.tdesign.mapper;

import com.tencent.tdesign.entity.UiSystemSetting;

public interface UiSystemSettingMapper {
  UiSystemSetting selectTop();
  int insert(UiSystemSetting setting);
  int update(UiSystemSetting setting);
}
