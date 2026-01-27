package com.tencent.tdesign.vo;

import java.util.ArrayList;
import java.util.List;

public class DatabaseMetadataResponse {
  private String name;
  private List<TableMetadataResponse> tables = new ArrayList<>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<TableMetadataResponse> getTables() {
    return tables;
  }

  public void setTables(List<TableMetadataResponse> tables) {
    this.tables = tables;
  }
}
