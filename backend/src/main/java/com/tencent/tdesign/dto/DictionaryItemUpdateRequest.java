package com.tencent.tdesign.dto;

public class DictionaryItemUpdateRequest {
  private String label;
  private String value;
  private String valueType;
  private Integer status;
  private Integer sort;
  private String tagColor;

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getValueType() {
    return valueType;
  }

  public void setValueType(String valueType) {
    this.valueType = valueType;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public Integer getSort() {
    return sort;
  }

  public void setSort(Integer sort) {
    this.sort = sort;
  }

  public String getTagColor() {
    return tagColor;
  }

  public void setTagColor(String tagColor) {
    this.tagColor = tagColor;
  }
}
