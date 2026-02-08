package com.tencent.tdesign.module;

import java.util.List;

public class ModulePackageManifest {
  private String key;
  private String name;
  private String version;
  private Boolean enabledByDefault;
  private List<String> requiredTables;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Boolean getEnabledByDefault() {
    return enabledByDefault;
  }

  public void setEnabledByDefault(Boolean enabledByDefault) {
    this.enabledByDefault = enabledByDefault;
  }

  public List<String> getRequiredTables() {
    return requiredTables;
  }

  public void setRequiredTables(List<String> requiredTables) {
    this.requiredTables = requiredTables;
  }
}
