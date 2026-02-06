package com.tencent.tdesign.module;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import javax.sql.DataSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;

public class ModuleInstallationContext {
  private final DataSource dataSource;
  private final ResourceLoader resourceLoader;
  private final JdbcTemplate jdbcTemplate;

  public ModuleInstallationContext(DataSource dataSource, ResourceLoader resourceLoader) {
    this.dataSource = dataSource;
    this.resourceLoader = resourceLoader;
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public ResourceLoader getResourceLoader() {
    return resourceLoader;
  }

  public JdbcTemplate getJdbcTemplate() {
    return jdbcTemplate;
  }

  public String resolveDatabaseKey() {
    try (Connection connection = dataSource.getConnection()) {
      String productName = connection.getMetaData().getDatabaseProductName();
      return normalizeDatabaseKey(productName);
    } catch (SQLException ex) {
      return "mysql";
    }
  }

  public boolean tableExists(String tableName) {
    if (tableName == null || tableName.isBlank()) return false;
    String target = tableName.trim().toLowerCase(Locale.ROOT);
    try (Connection connection = dataSource.getConnection()) {
      DatabaseMetaData metaData = connection.getMetaData();
      try (ResultSet tables = metaData.getTables(connection.getCatalog(), null, "%", new String[] {"TABLE"})) {
        while (tables.next()) {
          String name = tables.getString("TABLE_NAME");
          if (name != null && name.trim().toLowerCase(Locale.ROOT).equals(target)) {
            return true;
          }
        }
      }
    } catch (SQLException ex) {
      return false;
    }
    return false;
  }

  public void executeSqlResource(String resourcePath) {
    Resource resource = resourceLoader.getResource(resourcePath);
    if (!resource.exists()) {
      throw new IllegalArgumentException("安装脚本不存在: " + resourcePath);
    }
    try (Connection connection = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(connection, resource);
    } catch (SQLException ex) {
      throw new IllegalArgumentException("执行模块脚本失败: " + ex.getMessage());
    }
  }

  public void executeSqlResourceIfExists(String resourcePath) {
    Resource resource = resourceLoader.getResource(resourcePath);
    if (!resource.exists()) return;
    try (Connection connection = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(connection, resource);
    } catch (SQLException ex) {
      throw new IllegalArgumentException("执行模块脚本失败: " + ex.getMessage());
    }
  }

  public String resolveModuleResource(String moduleKey, String action) {
    String databaseKey = resolveDatabaseKey();
    return String.format("classpath:modules/%s/%s/%s.sql", moduleKey, databaseKey, action);
  }

  private String normalizeDatabaseKey(String productName) {
    if (productName == null) return "mysql";
    String normalized = productName.toLowerCase(Locale.ROOT);
    if (normalized.contains("postgres")) return "postgresql";
    if (normalized.contains("oracle")) return "oracle";
    if (normalized.contains("sql server")) return "sqlserver";
    if (normalized.contains("microsoft")) return "sqlserver";
    if (normalized.contains("mariadb")) return "mysql";
    return "mysql";
  }
}
