package com.tencent.tdesign.dto;

import jakarta.validation.constraints.NotBlank;

public class FieldColumnRequest {
  @NotBlank(message = "数据库标识不能为空")
  private String database;

  @NotBlank(message = "表标识不能为空")
  private String table;

  @NotBlank(message = "字段名不能为空")
  private String name;

  @NotBlank(message = "字段类型不能为空")
  private String type;

  private String length;
  private String defaultValue;
  private String description;
  private Boolean required;
  private Boolean primaryKey;

  public String getDatabase() {
    return database;
  }

  public void setDatabase(String database) {
    this.database = database;
  }

  public String getTable() {
    return table;
  }

  public void setTable(String table) {
    this.table = table;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getLength() {
    return length;
  }

  public void setLength(String length) {
    this.length = length;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Boolean getRequired() {
    return required;
  }

  public void setRequired(Boolean required) {
    this.required = required;
  }

  public Boolean getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(Boolean primaryKey) {
    this.primaryKey = primaryKey;
  }
}
