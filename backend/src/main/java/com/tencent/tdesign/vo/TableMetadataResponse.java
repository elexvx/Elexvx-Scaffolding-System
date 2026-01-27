package com.tencent.tdesign.vo;

import java.util.ArrayList;
import java.util.List;

public class TableMetadataResponse {
  private String name;
  private String comment;
  private List<FieldMetadataResponse> columns = new ArrayList<>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public List<FieldMetadataResponse> getColumns() {
    return columns;
  }

  public void setColumns(List<FieldMetadataResponse> columns) {
    this.columns = columns;
  }
}
