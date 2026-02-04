package com.tencent.tdesign.mapper;

import com.tencent.tdesign.entity.ModuleRegistry;
import java.util.List;

public interface ModuleRegistryMapper {
  List<ModuleRegistry> selectAll();
  ModuleRegistry selectByKey(String moduleKey);
  int insert(ModuleRegistry registry);
  int update(ModuleRegistry registry);
}
