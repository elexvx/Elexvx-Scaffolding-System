package com.tencent.tdesign.print;

public class PrintDefinition {
  private String definitionId;
  private String name;
  private TemplateType templateType;
  private String permission;

  public String getDefinitionId() { return definitionId; }
  public void setDefinitionId(String definitionId) { this.definitionId = definitionId; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public TemplateType getTemplateType() { return templateType; }
  public void setTemplateType(TemplateType templateType) { this.templateType = templateType; }
  public String getPermission() { return permission; }
  public void setPermission(String permission) { this.permission = permission; }
}
