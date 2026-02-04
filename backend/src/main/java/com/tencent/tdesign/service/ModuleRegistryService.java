package com.tencent.tdesign.service;

import com.tencent.tdesign.entity.ModuleRegistry;
import com.tencent.tdesign.mapper.ModuleRegistryMapper;
import com.tencent.tdesign.module.ModuleDefinition;
import com.tencent.tdesign.module.ModuleDefinitionRegistry;
import com.tencent.tdesign.module.ModuleInstallState;
import com.tencent.tdesign.module.ModuleInstallationContext;
import com.tencent.tdesign.vo.ModuleRegistryResponse;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;

@Service
public class ModuleRegistryService {
  private static final Logger log = LoggerFactory.getLogger(ModuleRegistryService.class);

  private final ModuleRegistryMapper mapper;
  private final ModuleDefinitionRegistry definitionRegistry;
  private final DataSource dataSource;
  private final ResourceLoader resourceLoader;

  public ModuleRegistryService(
    ModuleRegistryMapper mapper,
    ModuleDefinitionRegistry definitionRegistry,
    DataSource dataSource,
    ResourceLoader resourceLoader
  ) {
    this.mapper = mapper;
    this.definitionRegistry = definitionRegistry;
    this.dataSource = dataSource;
    this.resourceLoader = resourceLoader;
  }

  public List<ModuleRegistryResponse> listModules() {
    syncDefinitions();
    List<ModuleRegistry> rows = mapper.selectAll();
    List<ModuleRegistryResponse> out = new ArrayList<>();
    for (ModuleRegistry row : rows) {
      ModuleRegistryResponse response = ModuleRegistryResponse.from(row);
      ModuleDefinition definition = definitionRegistry.getDefinition(row.getModuleKey());
      if (definition != null) {
        response.setName(definition.getName());
        response.setVersion(definition.getVersion());
      }
      out.add(response);
    }
    return out;
  }

  public ModuleRegistryResponse enableModule(String moduleKey, boolean enabled) {
    ModuleRegistry registry = getOrCreateRegistry(moduleKey);
    registry.setEnabled(enabled);
    mapper.update(registry);
    return ModuleRegistryResponse.from(registry);
  }

  public ModuleRegistryResponse installModule(String moduleKey) {
    ModuleDefinition definition = requireDefinition(moduleKey);
    ModuleRegistry registry = ensureRegistered(definition);
    ModuleInstallationContext context = new ModuleInstallationContext(dataSource, resourceLoader);
    try {
      verifyDependencies(definition, context);
      runMigrations(definition, context);
      definition.initialize(context);
      registry.setInstallState(ModuleInstallState.INSTALLED.name());
      registry.setInstalledAt(LocalDateTime.now());
      mapper.update(registry);
      return ModuleRegistryResponse.from(registry);
    } catch (Exception ex) {
      registry.setInstallState(ModuleInstallState.FAILED.name());
      mapper.update(registry);
      throw ex;
    }
  }

  public ModuleRegistryResponse uninstallModule(String moduleKey) {
    ModuleDefinition definition = requireDefinition(moduleKey);
    ModuleRegistry registry = getOrCreateRegistry(moduleKey);
    ModuleInstallationContext context = new ModuleInstallationContext(dataSource, resourceLoader);
    definition.uninstall(context);
    registry.setEnabled(false);
    registry.setInstallState(ModuleInstallState.UNINSTALLED.name());
    registry.setInstalledAt(null);
    mapper.update(registry);
    return ModuleRegistryResponse.from(registry);
  }

  public void assertModuleAvailable(String moduleKey) {
    ModuleRegistry registry = getOrCreateRegistry(moduleKey);
    String state = normalizeState(registry.getInstallState());
    if (!ModuleInstallState.INSTALLED.name().equals(state)) {
      throw new IllegalArgumentException("模块未安装: " + moduleKey);
    }
    if (!Boolean.TRUE.equals(registry.getEnabled())) {
      throw new IllegalArgumentException("模块已禁用: " + moduleKey);
    }
  }

  public void autoInstallModules() {
    syncDefinitions();
    for (Map.Entry<String, ModuleDefinition> entry : definitionRegistry.getDefinitions().entrySet()) {
      ModuleDefinition definition = entry.getValue();
      ModuleRegistry registry = getOrCreateRegistry(definition.getKey());
      String state = normalizeState(registry.getInstallState());
      if (!ModuleInstallState.INSTALLED.name().equals(state)) {
        try {
          installModule(definition.getKey());
          log.info("Module {} installed.", definition.getKey());
        } catch (Exception ex) {
          log.warn("Module {} install failed: {}", definition.getKey(), ex.getMessage());
        }
      }
    }
  }

  private ModuleRegistry getOrCreateRegistry(String moduleKey) {
    if (moduleKey == null || moduleKey.isBlank()) {
      throw new IllegalArgumentException("模块标识不能为空");
    }
    ModuleRegistry registry = mapper.selectByKey(moduleKey);
    if (registry != null) return registry;
    ModuleDefinition definition = definitionRegistry.getDefinition(moduleKey);
    if (definition != null) return ensureRegistered(definition);

    ModuleRegistry fallback = new ModuleRegistry();
    fallback.setModuleKey(moduleKey);
    fallback.setName(moduleKey);
    fallback.setVersion("1.0.0");
    fallback.setEnabled(false);
    fallback.setInstallState(ModuleInstallState.PENDING.name());
    mapper.insert(fallback);
    return fallback;
  }

  private void syncDefinitions() {
    for (ModuleDefinition definition : definitionRegistry.getDefinitions().values()) {
      ensureRegistered(definition);
    }
  }

  private ModuleRegistry ensureRegistered(ModuleDefinition definition) {
    ModuleRegistry existing = mapper.selectByKey(definition.getKey());
    if (existing == null) {
      ModuleRegistry registry = new ModuleRegistry();
      registry.setModuleKey(definition.getKey());
      registry.setName(definition.getName());
      registry.setVersion(definition.getVersion());
      registry.setEnabled(definition.isEnabledByDefault());
      registry.setInstallState(ModuleInstallState.PENDING.name());
      mapper.insert(registry);
      return registry;
    }
    boolean changed = false;
    if (definition.getName() != null && !definition.getName().equals(existing.getName())) {
      existing.setName(definition.getName());
      changed = true;
    }
    if (definition.getVersion() != null && !definition.getVersion().equals(existing.getVersion())) {
      existing.setVersion(definition.getVersion());
      changed = true;
    }
    if (existing.getInstallState() == null || existing.getInstallState().isBlank()) {
      existing.setInstallState(ModuleInstallState.PENDING.name());
      changed = true;
    }
    if (changed) {
      mapper.update(existing);
    }
    return existing;
  }

  private ModuleDefinition requireDefinition(String moduleKey) {
    ModuleDefinition definition = definitionRegistry.getDefinition(moduleKey);
    if (definition == null) {
      throw new IllegalArgumentException("未找到模块定义: " + moduleKey);
    }
    return definition;
  }

  private void verifyDependencies(ModuleDefinition definition, ModuleInstallationContext context) {
    List<String> requiredTables = definition.getRequiredTables();
    if (requiredTables == null || requiredTables.isEmpty()) return;
    try (Connection connection = context.getDataSource().getConnection()) {
      DatabaseMetaData metaData = connection.getMetaData();
      for (String table : requiredTables) {
        if (!tableExists(metaData, table)) {
          throw new IllegalStateException("依赖表不存在: " + table);
        }
      }
    } catch (SQLException ex) {
      throw new IllegalStateException("模块依赖检查失败: " + ex.getMessage(), ex);
    }
  }

  private void runMigrations(ModuleDefinition definition, ModuleInstallationContext context) {
    List<String> resources = definition.getMigrationResources();
    if (resources == null || resources.isEmpty()) return;
    for (String location : resources) {
      if (location == null || location.isBlank()) continue;
      Resource resource = context.getResourceLoader().getResource(location);
      try (Connection connection = context.getDataSource().getConnection()) {
        ScriptUtils.executeSqlScript(connection, resource);
      } catch (Exception ex) {
        throw new IllegalStateException("模块迁移执行失败: " + location + ": " + ex.getMessage(), ex);
      }
    }
  }

  private boolean tableExists(DatabaseMetaData metaData, String table) throws SQLException {
    if (table == null || table.isBlank()) return false;
    String[] candidates = new String[] { table, table.toLowerCase(), table.toUpperCase() };
    for (String candidate : candidates) {
      try (ResultSet rs = metaData.getTables(null, null, candidate, new String[] { "TABLE" })) {
        if (rs.next()) return true;
      }
    }
    return false;
  }

  private String normalizeState(String value) {
    if (value == null) return ModuleInstallState.PENDING.name();
    return value.trim().toUpperCase();
  }
}
