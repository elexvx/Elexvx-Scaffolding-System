package com.tencent.tdesign.module;

import javax.sql.DataSource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;

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
}
