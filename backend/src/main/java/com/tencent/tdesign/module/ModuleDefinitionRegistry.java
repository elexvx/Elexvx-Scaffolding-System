package com.tencent.tdesign.module;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
/**
 * 模块定义注册表。
 *
 * <p>收集系统内声明的 {@link ModuleDefinition}（通常由各模块以 Bean 形式提供），并按 {@code key} 归一化
 *（trim + lower-case）后注册，供运行时查询模块元信息。
 */
public class ModuleDefinitionRegistry {
  private final Map<String, ModuleDefinition> definitions;

  public ModuleDefinitionRegistry(List<ModuleDefinition> definitions) {
    Map<String, ModuleDefinition> map = new LinkedHashMap<>();
    if (definitions != null) {
      for (ModuleDefinition definition : definitions) {
        if (definition == null || definition.getKey() == null) {
          continue;
        }
        String key = definition.getKey().trim().toLowerCase();
        map.put(key, definition);
      }
    }
    this.definitions = Collections.unmodifiableMap(map);
  }

  public Map<String, ModuleDefinition> getDefinitions() {
    return definitions;
  }

  public ModuleDefinition getDefinition(String key) {
    if (key == null) return null;
    return definitions.get(key.trim().toLowerCase());
  }
}
