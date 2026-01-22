package com.tencent.tdesign.mapper;

import com.tencent.tdesign.entity.StorageSetting;

public interface StorageSettingMapper {
  StorageSetting selectTop();
  int insert(StorageSetting setting);
  int update(StorageSetting setting);
}
