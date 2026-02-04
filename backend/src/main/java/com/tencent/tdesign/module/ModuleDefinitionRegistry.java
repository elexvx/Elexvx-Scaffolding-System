package com.tencent.tdesign.module;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
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
