package com.tencent.tdesign.plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.flywaydb.core.Flyway;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "tdesign.plugins", name = "enabled", havingValue = "true")
public class PluginLifecycleService {
  private final JdbcTemplate jdbcTemplate;
  private final PluginPackageService packageService;
  private final Map<String, PluginLifecycleState> states = new ConcurrentHashMap<>();
  private final DefaultPluginManager pluginManager = new DefaultPluginManager();

  public PluginLifecycleService(JdbcTemplate jdbcTemplate, PluginPackageService packageService) {
    this.jdbcTemplate = jdbcTemplate;
    this.packageService = packageService;
  }

  public PluginStatusResponse installAndEnable(PluginPackageService.PluginInstallArtifact artifact) {
    String id = artifact.manifest().getId();
    states.put(id, PluginLifecycleState.VERIFIED);
    String schema = "plug_" + id.replace('-', '_');
    jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + schema);
    Flyway flyway = Flyway.configure()
      .dataSource(jdbcTemplate.getDataSource())
      .schemas(schema)
      .table("flyway_plugin_history")
      .locations("filesystem:" + artifact.packageFile().getParent().resolve("database/migrations"))
      .load();
    flyway.migrate();
    states.put(id, PluginLifecycleState.INSTALLED);

    String loadedPluginId = pluginManager.loadPlugin(artifact.packageFile());
    PluginWrapper wrapper = pluginManager.getPlugin(loadedPluginId);
    pluginManager.startPlugin(loadedPluginId);
    states.put(id, PluginLifecycleState.ENABLED);
    return new PluginStatusResponse(id, artifact.manifest().getVersion(), states.get(id).name(), wrapper.getDescriptor().getPluginDescription());
  }

  public PluginStatusResponse disable(String pluginId) {
    var plugin = pluginManager.getPlugin(pluginId);
    if (plugin != null) {
      pluginManager.stopPlugin(pluginId);
    }
    states.put(pluginId, PluginLifecycleState.DISABLED);
    return new PluginStatusResponse(pluginId, null, PluginLifecycleState.DISABLED.name(), "disabled");
  }

  public record PluginStatusResponse(String pluginId, String version, String state, String detail) {}
}
