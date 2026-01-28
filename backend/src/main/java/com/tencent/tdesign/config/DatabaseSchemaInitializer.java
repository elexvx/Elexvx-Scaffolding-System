package com.tencent.tdesign.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.lang.NonNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSchemaInitializer implements SmartLifecycle {
  private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaInitializer.class);

  private final JdbcTemplate jdbc;
  private final DataSource dataSource;
  private DatabaseDialect dialect;
  private volatile boolean running = false;

  @Value("${tdesign.db.schema-init.enabled:true}")
  private boolean enabled;

  @Value("${tdesign.db.dialect:}")
  private String configuredDialect;

  public DatabaseSchemaInitializer(JdbcTemplate jdbc, DataSource dataSource) {
    this.jdbc = jdbc;
    this.dataSource = dataSource;
  }

  @Override
  public void start() {
    if (running) return;
    running = true;
    if (!enabled) return;

    this.dialect = DatabaseDialect.resolve(configuredDialect, dataSource);
    log.info("数据库方言识别为: {}", dialect.getId());

    try {
      ensureUsersTable();
      ensureOrgUnitsTable();
      ensureOrgUnitLeadersTable();
      ensureUserOrgUnitsTable();
      ensureUserDepartmentsTable();
      ensureRolesTables();
      ensureMenuItemsTable();
      ensureRoleMenusTable();
      ensureUiBrandSettingsTable();
      ensureUiLayoutSettingsTable();
      ensureUiThemeSettingsTable();
      ensureUiFooterSettingsTable();
      ensureUiLoginSettingsTable();
      ensureUiLegalSettingsTable();
      ensureUiSystemSettingsTable();
      ensureVerificationSmsSettingsTable();
      ensureVerificationEmailSettingsTable();
      ensureSecurityTokenSettingsTable();
      ensureSecurityCaptchaSettingsTable();
      ensureSecurityPasswordPolicyTable();
      ensureOperationLogsTable();
      ensureUserParametersTable();
      ensureStorageSettingsTable();
      ensureAnnouncementTable();
      ensureNotificationTable();
      ensureAiProviderSettingsTable();
      ensureSensitiveWordsTable();
      ensureSensitivePageSettingsTable();
      ensureSensitiveSettingsTable();

    } catch (Exception e) {
      log.warn("数据库schema初始化失败（可能是权限不足或数据库不可用），将继续启动: {}", e.getMessage());
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
    return true;
  }

  @Override
  public int getPhase() {
    return Integer.MIN_VALUE;
  }

  private void ensureUsersTable() {
    if (!tableExists("users")) {
      log.info("创建表 users");
      jdbc.execute(
        "CREATE TABLE users (" +
          dialect.identityPrimaryKey("id") + ", " +
          column("account", dialect.varchar(64), "NOT NULL UNIQUE") + ", " +
          column("guid", dialect.varchar(36), "NOT NULL UNIQUE") + ", " +
          column("name", dialect.varchar(64), "NOT NULL") + ", " +
          column("password_hash", dialect.varchar(100), "NOT NULL") + ", " +
          column("mobile", dialect.varchar(20), null) + ", " +
          column("phone", dialect.varchar(20), null) + ", " +
          column("email", dialect.varchar(100), null) + ", " +
          column("id_card", dialect.varchar(32), null) + ", " +
          column("seat", dialect.varchar(50), null) + ", " +
          column("entity", dialect.varchar(100), null) + ", " +
          column("leader", dialect.varchar(64), null) + ", " +
          column("position", dialect.varchar(64), null) + ", " +
          column("join_day", dialect.dateType(), null) + ", " +
          column("team", dialect.varchar(255), null) + ", " +
          column("status", dialect.tinyIntType(), "NOT NULL DEFAULT 1") + ", " +
          timestampColumn("created_at", true, true, false) + ", " +
          timestampColumn("updated_at", true, true, true) +
          ")"
      );
      return;
    }

    ensureColumn("users", "guid", nullable(dialect.varchar(36)));
    ensureColumn("users", "mobile", nul
