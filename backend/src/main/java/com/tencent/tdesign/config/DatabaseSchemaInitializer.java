package com.tencent.tdesign.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.lang.NonNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class DatabaseSchemaInitializer implements SmartLifecycle {
  private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaInitializer.class);

  private final JdbcTemplate jdbc;
  private volatile boolean running = false;
  private volatile DatabaseDialect databaseDialect;

  @Value("${tdesign.db.schema-init.enabled:true}")
  private boolean enabled;

  public DatabaseSchemaInitializer(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public void start() {
    if (running) return;
    running = true;
    if (!enabled) return;

    try {
      ensureUsersTable();
      ensureOrgUnitsTable();
      ensureOrgUnitLeadersTable();
      ensureUserOrgUnitsTable();
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
      String createSql =
        "CREATE TABLE IF NOT EXISTS users (" +
          "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
          "account VARCHAR(64) NOT NULL UNIQUE, " +
          "guid VARCHAR(36) NOT NULL UNIQUE, " +
          "name VARCHAR(64) NOT NULL, " +
          "password_hash VARCHAR(100) NOT NULL, " +
          "mobile VARCHAR(20), " +
          "phone VARCHAR(20), " +
          "email VARCHAR(100), " +
          "id_card VARCHAR(32), " +
          "seat VARCHAR(50), " +
          "entity VARCHAR(100), " +
          "leader VARCHAR(64), " +
          "position VARCHAR(64), " +
          "join_day DATE, " +
          "team VARCHAR(255), " +
          "status TINYINT NOT NULL DEFAULT 1, " +
          "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
          "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
          ")";
      createTableWithComments("users", createSql);
      return;
    }

    ensureColumn("users", "guid", "VARCHAR(36) NULL");
    ensureColumn("users", "mobile", "VARCHAR(20) NULL");
    ensureColumn("users", "phone", "VARCHAR(20) NULL");
    ensureColumn("users", "email", "VARCHAR(100) NULL");
    ensureColumn("users", "id_card", "VARCHAR(32) NULL");
    ensureColumn("users", "seat", "VARCHAR(50) NULL");
    ensureColumn("users", "entity", "VARCHAR(100) NULL");
    ensureColumn("users", "leader", "VARCHAR(64) NULL");
    ensureColumn("users", "position", "VARCHAR(64) NULL");
    ensureColumn("users", "join_day", "DATE NULL");
    ensureColumn("users", "team", "VARCHAR(255) NULL");
    ensureColumn("users", "status", "TINYINT NULL");
    ensureColumn("users", "created_at", "DATETIME NULL");
    ensureColumn("users", "updated_at", "DATETIME NULL");
  }

  private void ensureOrgUnitsTable() {
    if (!tableExists("org_units")) {
      log.info("创建表 org_units");
      String createSql =
        "CREATE TABLE IF NOT EXISTS org_units (" +
          "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
          "parent_id BIGINT NULL, " +
          "name VARCHAR(128) NOT NULL, " +
          "short_name VARCHAR(64), " +
          "type VARCHAR(32) NOT NULL, " +
          "sort_order INT NOT NULL DEFAULT 0, " +
          "status TINYINT NOT NULL DEFAULT 1, " +
          "phone VARCHAR(32), " +
          "email VARCHAR(128), " +
          "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
          "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
          ")";
      createTableWithComments("org_units", createSql);
      return;
    }
    ensureColumn("org_units", "parent_id", "BIGINT NULL");
    ensureColumn("org_units", "short_name", "VARCHAR(64) NULL");
    ensureColumn("org_units", "type", "VARCHAR(32) NULL");
    ensureColumn("org_units", "sort_order", "INT NULL");
    ensureColumn("org_units", "status", "TINYINT NULL");
    ensureColumn("org_units", "phone", "VARCHAR(32) NULL");
    ensureColumn("org_units", "email", "VARCHAR(128) NULL");
    ensureColumn("org_units", "created_at", "DATETIME NULL");
    ensureColumn("org_units", "updated_at", "DATETIME NULL");
  }

  private void ensureOrgUnitLeadersTable() {
    if (!tableExists("org_unit_leaders")) {
      log.info("创建表 org_unit_leaders");
      String createSql =
        "CREATE TABLE IF NOT EXISTS org_unit_leaders (" +
          "org_unit_id BIGINT NOT NULL, " +
          "user_id BIGINT NOT NULL, " +
          "PRIMARY KEY (org_unit_id, user_id)" +
          ")";
      createTableWithComments("org_unit_leaders", createSql);
    }
  }

  private void ensureUserOrgUnitsTable() {
    if (!tableExists("user_org_units")) {
      log.info("创建表 user_org_units");
      String createSql =
        "CREATE TABLE IF NOT EXISTS user_org_units (" +
          "user_id BIGINT NOT NULL, " +
          "org_unit_id BIGINT NOT NULL, " +
          "PRIMARY KEY (user_id, org_unit_id)" +
          ")";
      createTableWithComments("user_org_units", createSql);
    }
  }

  private void ensureRolesTables() {
    if (!tableExists("roles")) {
      log.info("创建表 roles");
      String createSql =
        "CREATE TABLE IF NOT EXISTS roles (" +
          "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
          "name VARCHAR(64) UNIQUE NOT NULL, " +
          "description VARCHAR(255)" +
          ")";
      createTableWithComments("roles", createSql);
    } else {
      ensureColumn("roles", "description", "VARCHAR(255) NULL");
    }

    if (!tableExists("user_roles")) {
      log.info("创建表 user_roles");
      String createSql =
        "CREATE TABLE IF NOT EXISTS user_roles (" +
          "user_id BIGINT NOT NULL, " +
          "role VARCHAR(64) NOT NULL, " +
          "PRIMARY KEY (user_id, role)" +
          ")";
      createTableWithComments("user_roles", createSql);
    } else {
      ensureColumn("user_roles", "role", "VARCHAR(64) NULL");
    }

    if (!tableExists("role_permissions")) {
      log.info("创建表 role_permissions");
      String createSql =
        "CREATE TABLE IF NOT EXISTS role_permissions (" +
          "role_id BIGINT NOT NULL, " +
          "permission VARCHAR(128) NOT NULL, " +
          "PRIMARY KEY (role_id, permission)" +
          ")";
      createTableWithComments("role_permissions", createSql);
    }
  }

  private void ensureMenuItemsTable() {
    if (!tableExists("sys_menu_items")) {
      log.info("create table sys_menu_items");
      String createSql =
        "CREATE TABLE IF NOT EXISTS sys_menu_items (" +
          "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
          "parent_id BIGINT NULL, " +
          "node_type VARCHAR(16) NOT NULL, " +
          "path VARCHAR(255) NOT NULL, " +
          "route_name VARCHAR(128) NOT NULL, " +
          "component VARCHAR(255), " +
          "redirect VARCHAR(255), " +
          "title_zh_cn VARCHAR(64) NOT NULL, " +
          "title_en_us VARCHAR(64), " +
          "icon VARCHAR(64), " +
          "hidden TINYINT NOT NULL DEFAULT 0, " +
          "frame_src VARCHAR(512), " +
          "frame_blank TINYINT NOT NULL DEFAULT 0, " +
          "enabled TINYINT NOT NULL DEFAULT 1, " +
          "order_no INT NOT NULL DEFAULT 0, " +
          "actions VARCHAR(128), " +
          "version INT NOT NULL DEFAULT 0, " +
          "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
          "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
          "UNIQUE KEY uk_sys_menu_items_route_name (route_name)" +
          ")";
      createTableWithComments("sys_menu_items", createSql);
      return;
    }

    ensureColumn("sys_menu_items", "parent_id", "BIGINT NULL");
    ensureColumn("sys_menu_items", "node_type", "VARCHAR(16) NULL");
    ensureColumn("sys_menu_items", "path", "VARCHAR(255) NULL");
    ensureColumn("sys_menu_items", "route_name", "VARCHAR(128) NULL");
    ensureColumn("sys_menu_items", "component", "VARCHAR(255) NULL");
    ensureColumn("sys_menu_items", "redirect", "VARCHAR(255) NULL");
    ensureColumn("sys_menu_items", "title_zh_cn", "VARCHAR(64) NULL");
    ensureColumn("sys_menu_items", "title_en_us", "VARCHAR(64) NULL");
    ensureColumn("sys_menu_items", "icon", "VARCHAR(64) NULL");
    ensureColumn("sys_menu_items", "hidden", "TINYINT NULL");
    ensureColumn("sys_menu_items", "frame_src", "VARCHAR(512) NULL");
    ensureColumn("sys_menu_items", "frame_blank", "TINYINT NULL");
    ensureColumn("sys_menu_items", "enabled", "TINYINT NULL");
    ensureColumn("sys_menu_items", "order_no", "INT NULL");
    ensureColumn("sys_menu_items", "actions", "VARCHAR(128) NULL");
    ensureColumn("sys_menu_items", "version", "INT NULL");
    ensureColumn("sys_menu_items", "created_at", "DATETIME NULL");
    ensureColumn("sys_menu_items", "updated_at", "DATETIME NULL");
  }

  private void ensureRoleMenusTable() {
    if (!tableExists("role_menus")) {
      log.info("创建表 role_menus");
      String createSql =
        "CREATE TABLE IF NOT EXISTS role_menus (" +
          "role_id BIGINT NOT NULL, " +
          "menu_id BIGINT NOT NULL, " +
          "PRIMARY KEY (role_id, menu_id)" +
          ")";
      createTableWithComments("role_menus", createSql);
    }
  }


  private void ensureUiBrandSettingsTable() {
    if (!tableExists("ui_brand_settings")) {
      log.info("create table ui_brand_settings");
      String createSql =
        "CREATE TABLE IF NOT EXISTS ui_brand_settings (" +
          "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
          "website_name VARCHAR(100), " +
          "app_version VARCHAR(50), " +
          "logo_expanded_url VARCHAR(255), " +
          "logo_collapsed_url VARCHAR(255), " +
          "favicon_url VARCHAR(255), " +
          "qr_code_url VARCHAR(255)" +
          ")";
      createTableWithComments("ui_brand_settings", createSql);
      return;
    }
    ensureColumn("ui_brand_settings", "website_name", "VARCHAR(100) NULL");
    ensureColumn("ui_brand_settings", "app_version", "VARCHAR(50) NULL");
    ensureColumn("ui_brand_settings", "logo_expanded_url", "VARCHAR(255) NULL");
    ensureColumn("ui_brand_settings", "logo_collapsed_url", "VARCHAR(255) NULL");
    ensureColumn("ui_brand_settings", "favicon_url", "VARCHAR(255) NULL");
    ensureColumn("ui_brand_settings", "qr_code_url", "VARCHAR(255) NULL");
  }

  private void ensureUiLayoutSettingsTable() {
    if (!tableExists("ui_layout_settings")) {
      log.info("create table ui_layout_settings");
      String createSql =
        "CREATE TABLE IF NOT EXISTS ui_layout_settings (" +
          "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
          "default_home VARCHAR(255), " +
          "show_footer TINYINT, " +
          "is_sidebar_compact TINYINT, " +
          "show_breadcrumb TINYINT, " +
          "menu_auto_collapsed TINYINT, " +
          "layout VARCHAR(20), " +
          "split_menu TINYINT, " +
          "side_mode VARCHAR(20), " +
          "is_footer_aside TINYINT, " +
          "is_sidebar_fixed TINYINT, " +
          "is_header_fixed TINYINT, " +
          "is_use_tabs_router TINYINT, " +
          "show_header TINYINT" +
          ")";
      createTableWithComments("ui_layout_settings", createSql);
      return;
    }
    ensureColumn("ui_layout_settings", "default_home", "VARCHAR(255) NULL");
    ensureColumn("ui_layout_settings", "show_footer", "TINYINT NULL");
    ensureColumn("ui_layout_settings", "is_sidebar_compact", "TINYINT NULL");
    ensureColumn("ui_layout_settings", "show_breadcrumb", "TINYINT NULL");
    ensureColumn("ui_layout_settings", "menu_auto_collapsed", "TINYINT NULL");
    ensureColumn("ui_layout_settings", "layout", "VARCHAR(20) NULL");
    ensureColumn("ui_layout_settings", "split_menu", "TINYINT NULL");
    ensureColumn("ui_layout_settings", "side_mode", "VARCHAR(20) NULL");
    ensureColumn("ui_layout_settings", "is_footer_aside", "TINYINT NULL");
    ensureColumn("ui_layout_settings", "is_sidebar_fixed", "TINYINT NULL");
    ensureColumn("ui_layout_settings", "is_header_fixed", "TINYINT NULL");
    ensureColumn("ui_layout_settings", "is_use_tabs_router", "TINYINT NULL");
    ensureColumn("ui_layout_settings", "show_header", "TINYINT NULL");
  }

  private void ensureUiThemeSettingsTable() {
    if (!tableExists("ui_theme_settings")) {
      log.info("create table ui_theme_settings");
      String createSql =
        "CREATE TABLE IF NOT EXISTS ui_theme_settings (" +
          "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
          "auto_theme TINYINT, " +
          "light_start_time VARCHAR(10), " +
          "dark_start_time VARCHAR(10), " +
          "mode VARCHAR(20), " +
          "brand_theme VARCHAR(20)" +
          ")";
      createTableWithComments("ui_theme_settings", createSql);
      return;
    }
    ensureColumn("ui_theme_settings", "auto_theme", "TINYINT NULL");
    ensureColumn("ui_theme_settings", "light_start_time", "VARCHAR(10) NULL");
    ensureColumn("ui_theme_settings", "dark_start_time", "VARCHAR(10) NULL");
    ensureColumn("ui_theme_settings", "mode", "VARCHAR(20) NULL");
    ensureColumn("ui_theme_settings", "brand_theme", "VARCHAR(20) NULL");
  }

  private void ensureUiFooterSettingsTable() {
    if (!tableExists("ui_footer_settings")) {
      log.info("create table ui_footer_settings");
      String createSql =
        "CREATE TABLE IF NOT EXISTS ui_footer_settings (" +
          "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
          "footer_company VARCHAR(100), " +
          "footer_icp VARCHAR(100), " +
          "copyright_start_year VARCHAR(10)" +
          ")";
      createTableWithComments("ui_footer_settings", createSql);
      return;
    }
    ensureColumn("ui_footer_settings", "footer_company", "VARCHAR(100) NULL");
    ensureColumn("ui_footer_settings", "footer_icp", "VARCHAR(100) NULL");
    ensureColumn("ui_footer_settings", "copyright_start_year", "VARCHAR(10) NULL");
  }

  private void ensureUiLoginSettingsTable() {
    if (!tableExists("ui_login_settings")) {
      log.info("create table ui_login_settings");
      String createSql =
        "CREATE TABLE IF NOT EXISTS ui_login_settings (" +
          "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
          "login_bg_url VARCHAR(255), " +
          "allow_multi_device_login TINYINT" +
          ")";
      createTableWithComments("ui_login_settings", createSql);
      return;
    }
    ensureColumn("ui_login_settings", "login_bg_url", "VARCHAR(255) NULL");
    ensureColumn("ui_login_settings", "allow_multi_device_login", "TINYINT NULL");
  }

  private void ensureUiLegalSettingsTable() {
    if (!tableExists("ui_legal_settings")) {
      log.info("create table ui_legal_settings");
      String createSql =
        "CREATE TABLE IF NOT EXISTS ui_legal_settings (" +
          "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
          "user_agreement TEXT, " +
          "privacy_agreement TEXT" +
          ")";
      createTableWithComments("ui_legal_settings", createSql);
      return;
    }
    ensureColumn("ui_legal_settings", "user_agreement", "TEXT NULL");
    ensureColumn("ui_legal_settings", "privacy_agreement", "TEXT NULL");
  }

  private void ensureUiSystemSettingsTable() {
    if (!tableExists("ui_system_settings")) {
      log.info("create table ui_system_settings");
      String createSql =
        "CREATE TABLE IF NOT EXISTS ui_system_settings (" +
          "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
          "log_retention_days INT, " +
          "ai_assistant_enabled TINYINT" +
          ")";
      createTableWithComments("ui_system_settings", createSql);
      return;
    }
    ensureColumn("ui_system_settings", "log_retention_days", "INT NULL");
    ensureColumn("ui_system_settings", "ai_assistant_enabled", "TINYINT NULL");
  }

  private void ensureVerificationSmsSettingsTable() {
    if (!tableExists("verification_sms_settings")) {
      log.info("create table verification_sms_settings");
      String createSql =
        "CREATE TABLE IF NOT EXISTS verification_sms_settings (" +
          "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
          "sms_enabled TINYINT, " +
          "sms_provider VARCHAR(32), " +
          "sms_aliyun_enabled TINYINT, " +
          "sms_aliyun_access_key_id VARCHAR(256), " +
          "sms_aliyun_access_key_secret VARCHAR(256), " +
          "sms_aliyun_sign_name VARCHAR(128), " +
          "sms_aliyun_template_code VARCHAR(64), " +
          "sms_aliyun_region_id VARCHAR(64), " +
          "sms_aliyun_endpoint VARCHAR(255), " +
          "sms_tencent_enabled TINYINT, " +
          "sms_tencent_secret_id VARCHAR(256), " +
          "sms_tencent_secret_key VARCHAR(256), " +
          "sms_tencent_sign_name VARCHAR(128), " +
          "sms_tencent_template_id VARCHAR(64), " +
          "sms_tencent_region VARCHAR(64), " +
          "sms_tencent_endpoint VARCHAR(255), " +
          "sms_sdk_app_id VARCHAR(64)" +
          ")";
      createTableWithComments("verification_sms_settings", createSql);
      return;
    }
    ensureColumn("verification_sms_settings", "sms_enabled", "TINYINT NULL");
    ensureColumn("verification_sms_settings", "sms_provider", "VARCHAR(32) NULL");
    ensureColumn("verification_sms_settings", "sms_aliyun_enabled", "TINYINT NULL");
    ensureColumn("verification_sms_settings", "sms_aliyun_access_key_id", "VARCHAR(256) NULL");
    ensureColumn("verification_sms_settings", "sms_aliyun_access_key_secret", "VARCHAR(256) NULL");
    ensureColumn("verification_sms_settings", "sms_aliyun_sign_name", "VARCHAR(128) NULL");
    ensureColumn("verification_sms_settings", "sms_aliyun_template_code", "VARCHAR(64) NULL");
    ensureColumn("verification_sms_settings", "sms_aliyun_region_id", "VARCHAR(64) NULL");
    ensureColumn("verification_sms_settings", "sms_aliyun_endpoint", "VARCHAR(255) NULL");
    ensureColumn("verification_sms_settings", "sms_tencent_enabled", "TINYINT NULL");
    ensureColumn("verification_sms_settings", "sms_tencent_secret_id", "VARCHAR(256) NULL");
    ensureColumn("verification_sms_settings", "sms_tencent_secret_key", "VARCHAR(256) NULL");
    ensureColumn("verification_sms_settings", "sms_tencent_sign_name", "VARCHAR(128) NULL");
    ensureColumn("verification_sms_settings", "sms_tencent_template_id", "VARCHAR(64) NULL");
    ensureColumn("verification_sms_settings", "sms_tencent_region", "VARCHAR(64) NULL");
    ensureColumn("verification_sms_settings", "sms_tencent_endpoint", "VARCHAR(255) NULL");
    ensureColumn("verification_sms_settings", "sms_sdk_app_id", "VARCHAR(64) NULL");
  }

  private void ensureVerificationEmailSettingsTable() {
    if (!tableExists("verification_email_settings")) {
      log.info("create table verification_email_settings");
      String createSql =
        "CREATE TABLE IF NOT EXISTS verification_email_settings (" +
          "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
          "email_enabled TINYINT, " +
          "email_host VARCHAR(255), " +
          "email_port INT, " +
          "email_username VARCHAR(128), " +
          "email_password VARCHAR(256), " +
          "email_from VARCHAR(128), " +
          "email_ssl TINYINT" +
          ")";
      createTableWithComments("verification_email_settings", createSql);
      return;
    }
    ensureColumn("verification_email_settings", "email_enabled", "TINYINT NULL");
    ensureColumn("verification_email_settings", "email_host", "VARCHAR(255) NULL");
    ensureColumn("verification_email_settings", "email_port", "INT NULL");
    ensureColumn("verification_email_settings", "email_username", "VARCHAR(128) NULL");
    ensureColumn("verification_email_settings", "email_password", "VARCHAR(256) NULL");
    ensureColumn("verification_email_settings", "email_from", "VARCHAR(128) NULL");
    ensureColumn("verification_email_settings", "email_ssl", "TINYINT NULL");
  }

  private void ensureSecurityTokenSettingsTable() {
    if (!tableExists("security_token_settings")) {
      log.info("create table security_token_settings");
      String createSql =
        "CREATE TABLE IF NOT EXISTS security_token_settings (" +
          "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
          "session_timeout_minutes INT, " +
          "token_timeout_minutes INT, " +
          "token_refresh_grace_minutes INT" +
          ")";
      createTableWithComments("security_token_settings", createSql);
      return;
    }
    ensureColumn("security_token_settings", "session_timeout_minutes", "INT NULL");
    ensureColumn("security_token_settings", "token_timeout_minutes", "INT NULL");
    ensureColumn("security_token_settings", "token_refresh_grace_minutes", "INT NULL");
  }

  private void ensureSecurityCaptchaSettingsTable() {
    if (!tableExists("security_captcha_settings")) {
      log.info("create table security_captcha_settings");
      String createSql =
        "CREATE TABLE IF NOT EXISTS security_captcha_settings (" +
          "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
          "captcha_enabled TINYINT, " +
          "captcha_type VARCHAR(20), " +
          "drag_captcha_width INT, " +
          "drag_captcha_height INT, " +
          "drag_captcha_threshold INT, " +
          "image_captcha_length INT, " +
          "image_captcha_noise_lines INT" +
          ")";
      createTableWithComments("security_captcha_settings", createSql);
      return;
    }
    ensureColumn("security_captcha_settings", "captcha_enabled", "TINYINT NULL");
    ensureColumn("security_captcha_settings", "captcha_type", "VARCHAR(20) NULL");
    ensureColumn("security_captcha_settings", "drag_captcha_width", "INT NULL");
    ensureColumn("security_captcha_settings", "drag_captcha_height", "INT NULL");
    ensureColumn("security_captcha_settings", "drag_captcha_threshold", "INT NULL");
    ensureColumn("security_captcha_settings", "image_captcha_length", "INT NULL");
    ensureColumn("security_captcha_settings", "image_captcha_noise_lines", "INT NULL");
  }

  private void ensureSecurityPasswordPolicyTable() {
    if (!tableExists("security_password_policy")) {
      log.info("create table security_password_policy");
      String createSql =
        "CREATE TABLE IF NOT EXISTS security_password_policy (" +
          "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
          "password_min_length INT, " +
          "password_require_uppercase TINYINT, " +
          "password_require_lowercase TINYINT, " +
          "password_require_special TINYINT, " +
          "password_allow_sequential TINYINT" +
          ")";
      createTableWithComments("security_password_policy", createSql);
      return;
    }
    ensureColumn("security_password_policy", "password_min_length", "INT NULL");
    ensureColumn("security_password_policy", "password_require_uppercase", "TINYINT NULL");
    ensureColumn("security_password_policy", "password_require_lowercase", "TINYINT NULL");
    ensureColumn("security_password_policy", "password_require_special", "TINYINT NULL");
    ensureColumn("security_password_policy", "password_allow_sequential", "TINYINT NULL");
  }

  private void ensureOperationLogsTable() {
    if (!tableExists("operation_logs")) {
      log.info("创建表 operation_logs");
      String createSql =
        "CREATE TABLE IF NOT EXISTS operation_logs (" +
          "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
          "action VARCHAR(32) NOT NULL, " +
          "module VARCHAR(64), " +
          "detail VARCHAR(512), " +
          "user_id BIGINT, " +
          "user_guid VARCHAR(36), " +
          "account VARCHAR(64), " +
          "ip_address VARCHAR(64), " +
          "device_model VARCHAR(128), " +
          "os VARCHAR(64), " +
          "browser VARCHAR(64), " +
          "created_at DATETIME" +
          ")";
      createTableWithComments("operation_logs", createSql);
      return;
    }
    ensureColumn("operation_logs", "action", "VARCHAR(32) NOT NULL");
    ensureColumn("operation_logs", "module", "VARCHAR(64) NULL");
    ensureColumn("operation_logs", "detail", "VARCHAR(512) NULL");
    ensureColumn("operation_logs", "user_id", "BIGINT NULL");
    ensureColumn("operation_logs", "user_guid", "VARCHAR(36) NULL");
    ensureColumn("operation_logs", "account", "VARCHAR(64) NULL");
    ensureColumn("operation_logs", "ip_address", "VARCHAR(64) NULL");
    ensureColumn("operation_logs", "device_model", "VARCHAR(128) NULL");
    ensureColumn("operation_logs", "os", "VARCHAR(64) NULL");
    ensureColumn("operation_logs", "browser", "VARCHAR(64) NULL");
    ensureColumn("operation_logs", "created_at", "DATETIME NULL");
  }

  private void ensureUserParametersTable() {
    if (!tableExists("user_parameters")) {
      log.info("创建表 user_parameters");
      String createSql =
        "CREATE TABLE IF NOT EXISTS user_parameters (" +
          "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
          "user_id BIGINT NOT NULL, " +
          "param_key VARCHAR(100) NOT NULL, " +
          "param_value TEXT NOT NULL, " +
          "description VARCHAR(255), " +
          "created_at DATETIME, " +
          "updated_at DATETIME" +
          ")";
      createTableWithComments("user_parameters", createSql);
    }
  }

  private void ensureStorageSettingsTable() {
    if (!tableExists("storage_settings")) {
      log.info("创建表 storage_settings");
      String createSql =
        "CREATE TABLE IF NOT EXISTS storage_settings (" +
          "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
          "provider VARCHAR(20) NOT NULL, " +
          "bucket VARCHAR(128), " +
          "region VARCHAR(128), " +
          "endpoint VARCHAR(255), " +
          "access_key VARCHAR(128), " +
          "secret_key VARCHAR(128), " +
          "custom_domain VARCHAR(255), " +
          "path_prefix VARCHAR(128), " +
          "public_read TINYINT" +
          ")";
      createTableWithComments("storage_settings", createSql);
      return;
    }
    ensureColumn("storage_settings", "provider", "VARCHAR(20) NOT NULL");
    ensureColumn("storage_settings", "bucket", "VARCHAR(128) NULL");
    ensureColumn("storage_settings", "region", "VARCHAR(128) NULL");
    ensureColumn("storage_settings", "endpoint", "VARCHAR(255) NULL");
    ensureColumn("storage_settings", "access_key", "VARCHAR(128) NULL");
    ensureColumn("storage_settings", "secret_key", "VARCHAR(128) NULL");
    ensureColumn("storage_settings", "custom_domain", "VARCHAR(255) NULL");
    ensureColumn("storage_settings", "path_prefix", "VARCHAR(128) NULL");
    ensureColumn("storage_settings", "public_read", "TINYINT NULL");
  }

  private void ensureAnnouncementTable() {
    if (!tableExists("announcements")) {
      log.info("创建表 announcements");
      String createSql =
        "CREATE TABLE IF NOT EXISTS announcements (" +
          "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
          "title VARCHAR(200) NOT NULL, " +
          "summary VARCHAR(200), " +
          "content TEXT NOT NULL, " +
          "type VARCHAR(32) NOT NULL, " +
          "priority VARCHAR(16) NOT NULL, " +
          "status VARCHAR(16) NOT NULL, " +
          "cover_url VARCHAR(255), " +
          "attachment_url VARCHAR(255), " +
          "attachment_name VARCHAR(255), " +
          "publish_at DATETIME, " +
          "created_at DATETIME, " +
          "updated_at DATETIME, " +
          "created_by_id BIGINT, " +
          "created_by_name VARCHAR(64), " +
          "is_broadcasted TINYINT NOT NULL DEFAULT 0" +
          ")";
      createTableWithComments("announcements", createSql);
    } else {
      ensureColumn("announcements", "summary", "VARCHAR(200) NULL");
      ensureColumn("announcements", "cover_url", "VARCHAR(255) NULL");
      ensureColumn("announcements", "attachment_url", "VARCHAR(255) NULL");
      ensureColumn("announcements", "attachment_name", "VARCHAR(255) NULL");
      ensureColumn("announcements", "publish_at", "DATETIME NULL");
      ensureColumn("announcements", "created_by_id", "BIGINT NULL");
      ensureColumn("announcements", "created_by_name", "VARCHAR(64) NULL");
      ensureColumn("announcements", "is_broadcasted", "TINYINT NOT NULL DEFAULT 0");
    }
    seedAnnouncementData();
  }

  private void ensureNotificationTable() {
    if (!tableExists("notifications")) {
      log.info("创建表 notifications");
      String createSql =
        "CREATE TABLE IF NOT EXISTS notifications (" +
          "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
          "title VARCHAR(200) NOT NULL, " +
          "summary VARCHAR(200), " +
          "content TEXT NOT NULL, " +
          "priority VARCHAR(16) NOT NULL, " +
          "status VARCHAR(16) NOT NULL, " +
          "type VARCHAR(32), " +
          "cover_url VARCHAR(512), " +
          "attachment_url VARCHAR(512), " +
          "attachment_name VARCHAR(255), " +
          "publish_at DATETIME, " +
          "created_at DATETIME, " +
          "updated_at DATETIME, " +
          "created_by_id BIGINT, " +
          "created_by_name VARCHAR(64)" +
          ")";
      createTableWithComments("notifications", createSql);
      return;
    }
    ensureColumn("notifications", "summary", "VARCHAR(200) NULL");
    ensureColumn("notifications", "type", "VARCHAR(32) NULL");
    ensureColumn("notifications", "cover_url", "VARCHAR(512) NULL");
    ensureColumn("notifications", "attachment_url", "VARCHAR(512) NULL");
    ensureColumn("notifications", "attachment_name", "VARCHAR(255) NULL");
    ensureColumn("notifications", "publish_at", "DATETIME NULL");
    ensureColumn("notifications", "created_by_id", "BIGINT NULL");
    ensureColumn("notifications", "created_by_name", "VARCHAR(64) NULL");
  }

  private void ensureAiProviderSettingsTable() {
    if (!tableExists("ai_provider_settings")) {
      log.info("创建表 ai_provider_settings");
      String createSql =
        "CREATE TABLE IF NOT EXISTS ai_provider_settings (" +
          "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
          "name VARCHAR(64) NOT NULL, " +
          "vendor VARCHAR(32) NOT NULL, " +
          "base_url VARCHAR(255) NOT NULL, " +
          "endpoint_path VARCHAR(255), " +
          "model VARCHAR(128), " +
          "api_key VARCHAR(512), " +
          "api_version VARCHAR(64), " +
          "temperature DOUBLE, " +
          "max_tokens INT, " +
          "is_default TINYINT, " +
          "enabled TINYINT, " +
          "extra_headers VARCHAR(2000), " +
          "remark VARCHAR(512), " +
          "last_test_status VARCHAR(32), " +
          "last_test_message VARCHAR(512), " +
          "last_tested_at DATETIME, " +
          "created_at DATETIME, " +
          "updated_at DATETIME" +
          ")";
      createTableWithComments("ai_provider_settings", createSql);
      return;
    }
    ensureColumn("ai_provider_settings", "endpoint_path", "VARCHAR(255) NULL");
    ensureColumn("ai_provider_settings", "api_version", "VARCHAR(64) NULL");
    ensureColumn("ai_provider_settings", "temperature", "DOUBLE NULL");
    ensureColumn("ai_provider_settings", "max_tokens", "INT NULL");
    ensureColumn("ai_provider_settings", "is_default", "TINYINT NULL");
    ensureColumn("ai_provider_settings", "enabled", "TINYINT NULL");
    ensureColumn("ai_provider_settings", "extra_headers", "VARCHAR(2000) NULL");
    ensureColumn("ai_provider_settings", "remark", "VARCHAR(512) NULL");
    ensureColumn("ai_provider_settings", "last_test_status", "VARCHAR(32) NULL");
    ensureColumn("ai_provider_settings", "last_test_message", "VARCHAR(512) NULL");
    ensureColumn("ai_provider_settings", "last_tested_at", "DATETIME NULL");
    ensureColumn("ai_provider_settings", "created_at", "DATETIME NULL");
    ensureColumn("ai_provider_settings", "updated_at", "DATETIME NULL");
  }

  private void ensureSensitiveWordsTable() {
    if (!tableExists("sensitive_words")) {
      log.info("创建表 sensitive_words");
      String createSql =
        "CREATE TABLE IF NOT EXISTS sensitive_words (" +
          "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
          "word VARCHAR(200) NOT NULL UNIQUE, " +
          "enabled TINYINT NOT NULL, " +
          "created_at DATETIME, " +
          "updated_at DATETIME" +
          ")";
      createTableWithComments("sensitive_words", createSql);
      return;
    }
    ensureColumn("sensitive_words", "word", "VARCHAR(200) NOT NULL");
    ensureColumn("sensitive_words", "enabled", "TINYINT NOT NULL");
    ensureColumn("sensitive_words", "created_at", "DATETIME NULL");
    ensureColumn("sensitive_words", "updated_at", "DATETIME NULL");
  }

  private void ensureSensitivePageSettingsTable() {
    if (!tableExists("sensitive_page_settings")) {
      log.info("创建表 sensitive_page_settings");
      String createSql =
        "CREATE TABLE IF NOT EXISTS sensitive_page_settings (" +
          "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
          "page_key VARCHAR(255) NOT NULL UNIQUE, " +
          "page_name VARCHAR(255), " +
          "enabled TINYINT NOT NULL, " +
          "created_at DATETIME, " +
          "updated_at DATETIME" +
          ")";
      createTableWithComments("sensitive_page_settings", createSql);
      return;
    }
    ensureColumn("sensitive_page_settings", "page_key", "VARCHAR(255) NOT NULL");
    ensureColumn("sensitive_page_settings", "page_name", "VARCHAR(255) NULL");
    ensureColumn("sensitive_page_settings", "enabled", "TINYINT NOT NULL");
    ensureColumn("sensitive_page_settings", "created_at", "DATETIME NULL");
    ensureColumn("sensitive_page_settings", "updated_at", "DATETIME NULL");
  }

  private void ensureSensitiveSettingsTable() {
    if (!tableExists("sensitive_settings")) {
      log.info("创建表 sensitive_settings");
      String createSql =
        "CREATE TABLE IF NOT EXISTS sensitive_settings (" +
          "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
          "enabled TINYINT NOT NULL, " +
          "updated_at DATETIME" +
          ")";
      createTableWithComments("sensitive_settings", createSql);
      return;
    }
    ensureColumn("sensitive_settings", "enabled", "TINYINT NOT NULL");
    ensureColumn("sensitive_settings", "updated_at", "DATETIME NULL");
  }

  private void seedAnnouncementData() {
    try {
      Integer cnt = jdbc.queryForObject("select count(*) from announcements", Integer.class);
      if (cnt != null && cnt > 0) return;
      String sql =
        "INSERT INTO announcements (" +
          "title, summary, content, type, priority, status, cover_url, publish_at, created_at, updated_at, created_by_name" +
          ") VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), NOW(), ?)";
      jdbc.update(
        sql,
        "AI技术在医疗领域的创新应用与发展前景",
        "本文探讨了人工智能技术在医疗诊断、药物研发和个性化治疗等方面的最新应用，分析了面临的挑战及未来的发展趋势。",
        "<p>随着人工智能技术的飞速发展，其在医疗领域的应用日益广泛。从辅助诊断到手术机器人，AI正在重塑医疗行业的未来...</p>",
        "社交",
        "high",
        "published",
        null,
        "Admin"
      );
      jdbc.update(
        sql,
        "大数据分析助力企业决策的实践案例",
        "通过实际案例分析，阐述了企业如何利用大数据分析工具挖掘数据价值，优化业务流程，提升决策效率和市场竞争力。",
        "<p>在数字化转型的浪潮中，数据已成为企业最宝贵的资产之一。如何从海量数据中提取有价值的信息...</p>",
        "技术",
        "middle",
        "published",
        null,
        "Admin"
      );
      jdbc.update(
        sql,
        "区块链技术在供应链管理中的应用",
        "介绍了区块链技术去中心化、不可篡改的特性在供应链追溯、物流透明化和供应链金融等场景中的应用优势和实施路径。",
        "<p>供应链管理的痛点在于信息不对称和信任缺失。区块链技术通过分布式账本...</p>",
        "科技",
        "low",
        "published",
        null,
        "Admin"
      );
      jdbc.update(
        sql,
        "云计算技术发展趋势与未来展望",
        "深入分析了云计算技术的演进历程，探讨了混合云、边缘计算和云原生等新兴技术趋势，展望了云计算对数字经济的推动作用。",
        "<p>云计算作为数字经济的基石，正在进入一个新的发展阶段。企业上云已成为常态...</p>",
        "云技术",
        "high",
        "published",
        null,
        "Admin"
      );
    } catch (Exception e) {
      log.warn("初始化公告示例数据失败: {}", e.getMessage());
    }
  }

  private boolean tableExists(String tableName) {
    Integer cnt =
      jdbc.queryForObject(
        "select count(*) from information_schema.tables where table_schema = database() and table_name = ?",
        Integer.class,
        tableName
      );
    return cnt != null && cnt > 0;
  }

  private boolean columnExists(String tableName, String columnName) {
    Integer cnt =
      jdbc.queryForObject(
        "select count(*) from information_schema.columns where table_schema = database() and table_name = ? and column_name = ?",
        Integer.class,
        tableName,
        columnName
      );
    return cnt != null && cnt > 0;
  }

  private void ensureColumn(String tableName, String columnName, String ddlType) {
    if (columnExists(tableName, columnName)) return;
    String sql = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + ddlType;
    log.info("补齐字段: {}.{}", tableName, columnName);
    jdbc.execute(sql);
  }

  private void createTableWithComments(String tableName, String createSql) {
    jdbc.execute(createSql);
    applyTableComments(tableName, createSql);
  }

  private void applyTableComments(String tableName, String createSql) {
    DatabaseDialect dialect = getDatabaseDialect();
    if (dialect == DatabaseDialect.OTHER) return;

    String tableComment = "表: " + tableName;
    Map<String, String> columns = extractColumnDefinitions(createSql);

    switch (dialect) {
      case MYSQL -> {
        jdbc.execute(
          "ALTER TABLE `" + tableName + "` COMMENT = '" + escapeSqlComment(tableComment) + "'"
        );
        for (Map.Entry<String, String> entry : columns.entrySet()) {
          String columnName = entry.getKey();
          String definition = entry.getValue();
          jdbc.execute(
            "ALTER TABLE `" +
              tableName +
              "` MODIFY COLUMN `" +
              columnName +
              "` " +
              definition +
              " COMMENT '" +
              escapeSqlComment(columnName) +
              "'"
          );
        }
      }
      case POSTGRESQL, ORACLE -> {
        jdbc.execute(
          "COMMENT ON TABLE " + tableName + " IS '" + escapeSqlComment(tableComment) + "'"
        );
        for (String columnName : columns.keySet()) {
          jdbc.execute(
            "COMMENT ON COLUMN " +
              tableName +
              "." +
              columnName +
              " IS '" +
              escapeSqlComment(columnName) +
              "'"
          );
        }
      }
      case SQLSERVER -> {
        jdbc.execute(
          "EXEC sys.sp_addextendedproperty @name = N'MS_Description', @value = N'" +
            escapeSqlComment(tableComment) +
            "', @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'" +
            tableName +
            "'"
        );
        for (String columnName : columns.keySet()) {
          jdbc.execute(
            "EXEC sys.sp_addextendedproperty @name = N'MS_Description', @value = N'" +
              escapeSqlComment(columnName) +
              "', @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = N'" +
              tableName +
              "', @level2type = N'COLUMN', @level2name = N'" +
              columnName +
              "'"
          );
        }
      }
      default -> {
      }
    }
  }

  private Map<String, String> extractColumnDefinitions(String createSql) {
    int start = createSql.indexOf('(');
    int end = createSql.lastIndexOf(')');
    if (start < 0 || end <= start) return Map.of();
    String body = createSql.substring(start + 1, end);
    String[] parts = body.split(", ");
    Map<String, String> columns = new LinkedHashMap<>();
    for (String part : parts) {
      String trimmed = part.trim();
      if (trimmed.startsWith("PRIMARY KEY")
        || trimmed.startsWith("UNIQUE KEY")
        || trimmed.startsWith("UNIQUE")
        || trimmed.startsWith("INDEX")
        || trimmed.startsWith("KEY")
        || trimmed.startsWith("CONSTRAINT")) {
        continue;
      }
      int firstSpace = trimmed.indexOf(' ');
      if (firstSpace <= 0) continue;
      String columnName = trimmed.substring(0, firstSpace);
      String definition = trimmed.substring(firstSpace + 1);
      columns.put(columnName, definition);
    }
    return columns;
  }

  private String escapeSqlComment(String comment) {
    return comment.replace("'", "''");
  }

  private DatabaseDialect getDatabaseDialect() {
    if (databaseDialect != null) {
      return databaseDialect;
    }
    try (Connection connection = Objects.requireNonNull(jdbc.getDataSource()).getConnection()) {
      DatabaseMetaData metaData = connection.getMetaData();
      String product = metaData.getDatabaseProductName().toLowerCase();
      if (product.contains("mysql") || product.contains("mariadb")) {
        databaseDialect = DatabaseDialect.MYSQL;
      } else if (product.contains("postgresql")) {
        databaseDialect = DatabaseDialect.POSTGRESQL;
      } else if (product.contains("oracle")) {
        databaseDialect = DatabaseDialect.ORACLE;
      } else if (product.contains("sql server") || product.contains("microsoft")) {
        databaseDialect = DatabaseDialect.SQLSERVER;
      } else {
        databaseDialect = DatabaseDialect.OTHER;
      }
    } catch (Exception e) {
      log.warn("无法识别数据库类型，将跳过注释: {}", e.getMessage());
      databaseDialect = DatabaseDialect.OTHER;
    }
    return databaseDialect;
  }

  private enum DatabaseDialect {
    MYSQL,
    POSTGRESQL,
    ORACLE,
    SQLSERVER,
    OTHER
  }
}
