package com.tencent.tdesign.module;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EmailVerificationModuleDefinition implements ModuleDefinition {
  @Override
  public String getKey() {
    return "email";
  }

  @Override
  public String getName() {
    return "邮箱验证";
  }

  @Override
  public String getVersion() {
    return "1.0.0";
  }

  @Override
  public List<String> getRequiredTables() {
    return List.of("verification_email_settings");
  }
}
