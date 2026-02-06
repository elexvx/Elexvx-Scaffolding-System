package com.tencent.tdesign.module;

import com.tencent.tdesign.service.ModuleRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.lang.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ModuleAutoInstaller implements SmartLifecycle {
  private static final Logger log = LoggerFactory.getLogger(ModuleAutoInstaller.class);

  private final ModuleRegistryService moduleRegistryService;
  private final boolean autoInstallEnabled;
  private volatile boolean running;

  public ModuleAutoInstaller(
    ModuleRegistryService moduleRegistryService,
    @Value("${tdesign.modules.auto-install:false}") boolean autoInstallEnabled
  ) {
    this.moduleRegistryService = moduleRegistryService;
    this.autoInstallEnabled = autoInstallEnabled;
  }

  @Override
  public void start() {
    if (running) return;
    running = true;
    try {
      moduleRegistryService.autoInstallModules();
    } catch (Exception ex) {
      log.warn("模块自动安装失败: {}", ex.getMessage());
    }
  }

  @Override
  public void stop() {
    running = false;
  }

  @Override
  public void stop(@NonNull Runnable callback) {
    stop();
    callback.run();
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public boolean isAutoStartup() {
    return autoInstallEnabled;
  }

  @Override
  public int getPhase() {
    return Integer.MIN_VALUE + 100;
  }
}
