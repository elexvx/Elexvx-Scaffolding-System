package com.tencent.tdesign.service;

import com.tencent.tdesign.vo.ModuleDescriptor;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

@Service
public class ModuleRegistryService {
  private static final String LICENSE_APACHE = "Apache-2.0";

  private final Environment environment;

  public ModuleRegistryService(Environment environment) {
    this.environment = environment;
  }

  public List<ModuleDescriptor> listModules() {
    List<ModuleDescriptor> modules = new ArrayList<>();
    modules.add(buildModule(
      "sms-aliyun",
      "短信模块（阿里云）",
      "Aliyun Dysmsapi SDK",
      LICENSE_APACHE,
      "2.2.1",
      isSmsEnabled() && isClassPresent("com.aliyuncs.DefaultAcsClient")
    ));
    modules.add(buildModule(
      "sms-tencent",
      "短信模块（腾讯云）",
      "TencentCloud SMS SDK",
      LICENSE_APACHE,
      "3.1.1100",
      isSmsEnabled() && isClassPresent("com.tencentcloudapi.sms.v20210111.SmsClient")
    ));
    modules.add(buildModule(
      "email-smtp",
      "邮箱模块（SMTP）",
      "Spring Boot Mail Starter",
      LICENSE_APACHE,
      "3.3.5",
      isEmailEnabled() && isClassPresent("org.springframework.mail.javamail.JavaMailSenderImpl")
    ));
    modules.add(buildModule(
      "captcha-aj",
      "验证码模块（AJ Captcha）",
      "AJ Captcha",
      LICENSE_APACHE,
      "1.4.0",
      isCaptchaEnabled() && isClassPresent("com.anji.captcha.service.CaptchaService")
    ));
    return modules;
  }

  private ModuleDescriptor buildModule(
    String key,
    String name,
    String source,
    String license,
    String version,
    boolean enabled
  ) {
    ModuleDescriptor descriptor = new ModuleDescriptor();
    descriptor.setKey(key);
    descriptor.setName(name);
    descriptor.setSource(source);
    descriptor.setLicense(license);
    descriptor.setVersion(version);
    descriptor.setEnabled(enabled);
    return descriptor;
  }

  private boolean isSmsEnabled() {
    return getFlag("tdesign.modules.sms.enabled", true);
  }

  private boolean isEmailEnabled() {
    return getFlag("tdesign.modules.email.enabled", true);
  }

  private boolean isCaptchaEnabled() {
    return getFlag("tdesign.modules.captcha.enabled", true);
  }

  private boolean getFlag(String key, boolean fallback) {
    String raw = environment.getProperty(key);
    if (raw == null) return fallback;
    return Boolean.parseBoolean(raw);
  }

  private boolean isClassPresent(String className) {
    return ClassUtils.isPresent(className, getClass().getClassLoader());
  }
}
