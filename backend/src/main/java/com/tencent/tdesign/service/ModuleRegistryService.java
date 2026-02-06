package com.tencent.tdesign.service;

import com.tencent.tdesign.entity.ModuleRegistry;
import com.tencent.tdesign.mapper.ModuleRegistryMapper;
import com.tencent.tdesign.module.ModuleDefinition;
import com.tencent.tdesign.module.ModuleDefinitionRegistry;
import com.tencent.tdesign.module.ModuleInstallationContext;
import com.tencent.tdesign.vo.ModuleDescriptor;
import com.tencent.tdesign.vo.ModuleRegistryResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModuleRegistryService {
  private static final String LICENSE_APACHE = "Apache-2.0";
  private static final String STATE_PENDING = "PENDING";
  private static final String STATE_INSTALLED = "INSTALLED";
  private static final String STATE_FAILED = "FAILED";
  private static final String STATE_UNINSTALLED = "UNINSTALLED";

  private final Environment environment;
  private final ModuleRegistryMapper registryMapper;
  private final ModuleDefinitionRegistry definitionRegistry;
  private final DataSource dataSource;
  private final ResourceLoader resourceLoader;

  public ModuleRegistryService(
    Environment environment,
    ModuleRegistryMapper registryMapper,
    ModuleDefinitionRegistry definitionRegistry,
    DataSource dataSource,
    ResourceLoader resourceLoader
  ) {
    this.environment = environment;
    this.registryMapper = registryMapper;
    this.definitionRegistry = definitionRegistry;
    this.dataSource = dataSource;
    this.resourceLoader = resourceLoader;
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

  public List<ModuleRegistryResponse> listRegistries() {
    List<ModuleRegistry> registries = registryMapper.selectAll();
    Map<String, ModuleRegistry> map = new LinkedHashMap<>();
    if (registries != null) {
      for (ModuleRegistry registry : registries) {
        if (registry == null || registry.getModuleKey() == null) continue;
        map.put(normalizeKey(registry.getModuleKey()), registry);
      }
    }
    for (ModuleDefinition definition : definitionRegistry.getDefinitions().values()) {
      if (definition == null || definition.getKey() == null) continue;
      String key = normalizeKey(definition.getKey());
      if (!map.containsKey(key)) {
        ModuleRegistry registry = buildRegistry(definition);
        map.put(key, registry);
      }
    }
    return map.values().stream()
        .sorted(Comparator.comparing(ModuleRegistry::getModuleKey, Comparator.nullsLast(String::compareToIgnoreCase)))
        .map(ModuleRegistryResponse::from)
        .toList();
  }

  public void assertModuleAvailable(String moduleKey) {
    ModuleRegistry registry = registryMapper.selectByKey(normalizeKey(moduleKey));
    if (registry == null) {
      throw new IllegalArgumentException("模块未安装: " + moduleKey);
    }
    if (!STATE_INSTALLED.equalsIgnoreCase(normalizeState(registry.getInstallState()))) {
      throw new IllegalArgumentException("模块未安装: " + moduleKey);
    }
    if (!Boolean.TRUE.equals(registry.getEnabled())) {
      throw new IllegalArgumentException("模块未启用: " + moduleKey);
    }
  }

  public boolean isModuleAvailable(String moduleKey) {
    ModuleRegistry registry = registryMapper.selectByKey(normalizeKey(moduleKey));
    if (registry == null) return false;
    if (!STATE_INSTALLED.equalsIgnoreCase(normalizeState(registry.getInstallState()))) return false;
    return Boolean.TRUE.equals(registry.getEnabled());
  }

  @Transactional
  public ModuleRegistryResponse enableModule(String moduleKey, boolean enabled) {
    ModuleRegistry registry = requireRegistry(moduleKey);
    if (!STATE_INSTALLED.equalsIgnoreCase(normalizeState(registry.getInstallState()))) {
      throw new IllegalArgumentException("模块未安装: " + moduleKey);
    }
    registry.setEnabled(enabled);
    registryMapper.update(registry);
    return ModuleRegistryResponse.from(registry);
  }

  @Transactional
  public ModuleRegistryResponse installModule(String moduleKey) {
    ModuleDefinition definition = requireDefinition(moduleKey);
    ModuleRegistry registry = getOrCreateRegistry(definition);
    if (STATE_INSTALLED.equalsIgnoreCase(normalizeState(registry.getInstallState()))) {
      return ModuleRegistryResponse.from(registry);
    }
    ModuleInstallationContext context = new ModuleInstallationContext(dataSource, resourceLoader);
    try {
      definition.initialize(context);
      registry.setInstallState(STATE_INSTALLED);
      registry.setInstalledAt(LocalDateTime.now());
      if (registry.getEnabled() == null) {
        registry.setEnabled(true);
      }
      registryMapper.update(registry);
      return ModuleRegistryResponse.from(registry);
    } catch (Exception ex) {
      registry.setInstallState(STATE_FAILED);
      registryMapper.update(registry);
      throw new IllegalArgumentException("模块安装失败: " + ex.getMessage());
    }
  }

  @Transactional
  public ModuleRegistryResponse uninstallModule(String moduleKey) {
    ModuleDefinition definition = requireDefinition(moduleKey);
    ModuleRegistry registry = getOrCreateRegistry(definition);
    ModuleInstallationContext context = new ModuleInstallationContext(dataSource, resourceLoader);
    try {
      definition.uninstall(context);
      registry.setInstallState(STATE_UNINSTALLED);
      registry.setEnabled(false);
      registry.setInstalledAt(null);
      registryMapper.update(registry);
      return ModuleRegistryResponse.from(registry);
    } catch (Exception ex) {
      registry.setInstallState(STATE_FAILED);
      registryMapper.update(registry);
      throw new IllegalArgumentException("模块卸载失败: " + ex.getMessage());
    }
  }

  @Transactional
  public void autoInstallModules() {
    for (ModuleDefinition definition : definitionRegistry.getDefinitions().values()) {
      if (definition == null || definition.getKey() == null) continue;
      ModuleRegistry registry = getOrCreateRegistry(definition);
      String state = normalizeState(registry.getInstallState());
      if (STATE_INSTALLED.equalsIgnoreCase(state)) continue;
      if (Boolean.FALSE.equals(registry.getEnabled())) continue;
      if (!definition.isEnabledByDefault() && (state == null || STATE_PENDING.equalsIgnoreCase(state))) {
        continue;
      }
      try {
        ModuleInstallationContext context = new ModuleInstallationContext(dataSource, resourceLoader);
        definition.initialize(context);
        registry.setInstallState(STATE_INSTALLED);
        if (registry.getEnabled() == null) registry.setEnabled(true);
        registry.setInstalledAt(LocalDateTime.now());
        registryMapper.update(registry);
      } catch (Exception ex) {
        registry.setInstallState(STATE_FAILED);
        registryMapper.update(registry);
      }
    }
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

  private ModuleDefinition requireDefinition(String moduleKey) {
    ModuleDefinition definition = definitionRegistry.getDefinition(moduleKey);
    if (definition == null) {
      throw new IllegalArgumentException("模块不存在: " + moduleKey);
    }
    return definition;
  }

  private ModuleRegistry requireRegistry(String moduleKey) {
    ModuleRegistry registry = registryMapper.selectByKey(normalizeKey(moduleKey));
    if (registry == null) {
      throw new IllegalArgumentException("模块未安装: " + moduleKey);
    }
    return registry;
  }

  private ModuleRegistry getOrCreateRegistry(ModuleDefinition definition) {
    String key = normalizeKey(definition.getKey());
    ModuleRegistry registry = registryMapper.selectByKey(key);
    if (registry != null) return registry;
    registry = buildRegistry(definition);
    registryMapper.insert(registry);
    return registry;
  }

  private ModuleRegistry buildRegistry(ModuleDefinition definition) {
    ModuleRegistry registry = new ModuleRegistry();
    registry.setModuleKey(normalizeKey(definition.getKey()));
    registry.setName(definition.getName());
    registry.setVersion(definition.getVersion());
    registry.setEnabled(definition.isEnabledByDefault());
    registry.setInstallState(STATE_PENDING);
    registry.setInstalledAt(null);
    return registry;
  }

  private boolean getFlag(String key, boolean fallback) {
    String raw = environment.getProperty(key);
    if (raw == null) return fallback;
    return Boolean.parseBoolean(raw);
  }

  private String normalizeKey(String moduleKey) {
    if (moduleKey == null) return "";
    return moduleKey.trim().toLowerCase();
  }

  private String normalizeState(String state) {
    if (state == null) return null;
    return state.trim().toUpperCase();
  }

  private boolean isClassPresent(String className) {
    return ClassUtils.isPresent(className, getClass().getClassLoader());
  }
}
