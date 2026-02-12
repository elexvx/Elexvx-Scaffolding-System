package com.tencent.tdesign.print;

import java.util.Map;

public class PrintJobRequest {
  private String definitionId;
  private String businessRef;
  private String mode;
  private Map<String, Object> params;

  public String getDefinitionId() { return definitionId; }
  public void setDefinitionId(String definitionId) { this.definitionId = definitionId; }
  public String getBusinessRef() { return businessRef; }
  public void setBusinessRef(String businessRef) { this.businessRef = businessRef; }
  public String getMode() { return mode; }
  public void setMode(String mode) { this.mode = mode; }
  public Map<String, Object> getParams() { return params; }
  public void setParams(Map<String, Object> params) { this.params = params; }
}
