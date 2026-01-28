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
    ensureColumn("users", "mobile", nullable(dialect.varchar(20)));
    ensureColumn("users", "phone", nullable(dialect.varchar(20)));
    ensureColumn("users", "email", nullable(dialect.varchar(100)));
    ensureColumn("users", "id_card", nullable(dialect.varchar(32)));
    ensureColumn("users", "seat", nullable(dialect.varchar(50)));
    ensureColumn("users", "entity", nullable(dialect.varchar(100)));
    ensureColumn("users", "leader", nullable(dialect.varchar(64)));
    ensureColumn("users", "position", nullable(dialect.varchar(64)));
    ensureColumn("users", "join_day", nullable(dialect.dateType()));
    ensureColumn("users", "team", nullable(dialect.varchar(255)));
    ensureColumn("users", "status", nullable(dialect.tinyIntType()));
    ensureColumn("users", "created_at", nullable(dialect.dateTimeType()));
    ensureColumn("users", "updated_at", nullable(dialect.dateTimeType()));
  }

  private void ensureOrgUnitsTable() {
    if (!tableExists("org_units")) {
      log.info("创建表 org_units");
      jdbc.execute(
        "CREATE TABLE org_units (" +
          dialect.identityPrimaryKey("id") + ", " +
          column("parent_id", dialect.bigIntType(), null) + ", " +
          column("name", dialect.varchar(128), "NOT NULL") + ", " +
          column("short_name", dialect.varchar(64), null) + ", " +
          column("type", dialect.varchar(32), "NOT NULL") + ", " +
          column("sort_order", dialect.intType(), "NOT NULL DEFAULT 0") + ", " +
          column("status", dialect.tinyIntType(), "NOT NULL DEFAULT 1") + ", " +
          column("phone", dialect.varchar(32), null) + ", " +
          column("email", dialect.varchar(128), null) + ", " +
          timestampColumn("created_at", true, true, false) + ", " +
          timestampColumn("updated_at", true, true, true) +
          ")"
      );
      return;
    }
    ensureColumn("org_units", "parent_id", nullable(dialect.bigIntType()));
    ensureColumn("org_units", "short_name", nullable(dialect.varchar(64)));
    ensureColumn("org_units", "type", nullable(dialect.varchar(32)));
    ensureColumn("org_units", "sort_order", nullable(dialect.intType()));
    ensureColumn("org_units", "status", nullable(dialect.tinyIntType()));
    ensureColumn("org_units", "phone", nullable(dialect.varchar(32)));
    ensureColumn("org_units", "email", nullable(dialect.varchar(128)));
    ensureColumn("org_units", "created_at", nullable(dialect.dateTimeType()));
    ensureColumn("org_units", "updated_at", nullable(dialect.dateTimeType()));
  }

  private void ensureOrgUnitLeadersTable() {
    if (!tableExists("org_unit_leaders")) {
      log.info("创建表 org_unit_leaders");
      jdbc.execute(
        "CREATE TABLE org_unit_leaders (" +
          column("org_unit_id", dialect.bigIntType(), "NOT NULL") + ", " +
          column("user_id", dialect.bigIntType(), "NOT NULL") + ", " +
          "PRIMARY KEY (org_unit_id, user_id)" +
          ")"
      );
    }
  }

  private void ensureUserOrgUnitsTable() {
    if (!tableExists("user_org_units")) {
      log.info("创建表 user_org_units");
      jdbc.execute(
        "CREATE TABLE user_org_units (" +
          column("user_id", dialect.bigIntType(), "NOT NULL") + ", " +
          column("org_unit_id", dialect.bigIntType(), "NOT NULL") + ", " +
          "PRIMARY KEY (user_id, org_unit_id)" +
          ")"
      );
    }
  }

  private void ensureUserDepartmentsTable() {
    if (!tableExists("user_departments")) {
      log.info("创建表 user_departments");
      jdbc.execute(
        "CREATE TABLE IF NOT EXISTS user_departments (" +
          "user_id BIGINT NOT NULL, " +
          "department_id BIGINT NOT NULL, " +
          "PRIMARY KEY (user_id, department_id)" +
          ")"
      );
    }
  }

  private void ensureRolesTables() {
    if (!tableExists("roles")) {
      log.info("创建表 roles");
      jdbc.execute(
        "CREATE TABLE roles (" +
          dialect.identityPrimaryKey("id") + ", " +
          column("name", dialect.varchar(64), "UNIQUE NOT NULL") + ", " +
          column("description", dialect.varchar(255), null) +
          ")"
      );
    } else {
      ensureColumn("roles", "description", nullable(dialect.varchar(255)));
    }

    if (!tableExists("user_roles")) {
      log.info("创建表 user_roles");
      jdbc.execute(
        "CREATE TABLE user_roles (" +
          column("user_id", dialect.bigIntType(), "NOT NULL") + ", " +
          column("role", dialect.varchar(64), "NOT NULL") + ", " +
          "PRIMARY KEY (user_id, role)" +
          ")"
      );
    } else {
      ensureColumn("user_roles", "role", nullable(dialect.varchar(64)));
    }

    if (!tableExists("role_permissions")) {
      log.info("创建表 role_permissions");
      jdbc.execute(
        "CREATE TABLE role_permissions (" +
          column("role_id", dialect.bigIntType(), "NOT NULL") + ", " +
          column("permission", dialect.varchar(128), "NOT NULL") + ", " +
          "PRIMARY KEY (role_id, permission)" +
          ")"
      );
    }
  }

  private void ensureMenuItemsTable() {
    if (!tableExists("sys_menu_items")) {
      log.info("create table sys_menu_items");
      jdbc.execute(
        "CREATE TABLE sys_menu_items (" +
          dialect.identityPrimaryKey("id") + ", " +
          column("parent_id", dialect.bigIntType(), null) + ", " +
          column("node_type", dialect.varchar(16), "NOT NULL") + ", " +
          column("path", dialect.varchar(255), "NOT NULL") + ", " +
          column("route_name", dialect.varchar(128), "NOT NULL") + ", " +
          column("component", dialect.varchar(255), null) + ", " +
          column("redirect", dialect.varchar(255), null) + ", " +
          column("title_zh_cn", dialect.varchar(64), "NOT NULL") + ", " +
          column("title_en_us", dialect.varchar(64), null) + ", " +
          column("icon", dialect.varchar(64), null) + ", " +
          column("hidden", dialect.tinyIntType(), "NOT NULL DEFAULT 0") + ", " +
          column("frame_src", dialect.varchar(512), null) + ", " +
          column("frame_blank", dialect.tinyIntType(), "NOT NULL DEFAULT 0") + ", " +
          column("enabled", dialect.tinyIntType(), "NOT NULL DEFAULT 1") + ", " +
          column("order_no", dialect.intType(), "NOT NULL DEFAULT 0") + ", " +
          column("actions", dialect.varchar(128), null) + ", " +
          column("version", dialect.intType(), "NOT NULL DEFAULT 0") + ", " +
          timestampColumn("created_at", true, true, false) + ", " +
          timestampColumn("updated_at", true, true, true) + ", " +
          dialect.uniqueConstraint("uk_sys_menu_items_route_name", "route_name") +
          ")"
      );
      return;
    }

    ensureColumn("sys_menu_items", "parent_id", nullable(dialect.bigIntType()));
    ensureColumn("sys_menu_items", "node_type", nullable(dialect.varchar(16)));
    ensureColumn("sys_menu_items", "path", nullable(dialect.varchar(255)));
    ensureColumn("sys_menu_items", "route_name", nullable(dialect.varchar(128)));
    ensureColumn("sys_menu_items", "component", nullable(dialect.varchar(255)));
    ensureColumn("sys_menu_items", "redirect", nullable(dialect.varchar(255)));
    ensureColumn("sys_menu_items", "title_zh_cn", nullable(dialect.varchar(64)));
    ensureColumn("sys_menu_items", "title_en_us", nullable(dialect.varchar(64)));
    ensureColumn("sys_menu_items", "icon", nullable(dialect.varchar(64)));
    ensureColumn("sys_menu_items", "hidden", nullable(dialect.tinyIntType()));
    ensureColumn("sys_menu_items", "frame_src", nullable(dialect.varchar(512)));
    ensureColumn("sys_menu_items", "frame_blank", nullable(dialect.tinyIntType()));
    ensureColumn("sys_menu_items", "enabled", nullable(dialect.tinyIntType()));
    ensureColumn("sys_menu_items", "order_no", nullable(dialect.intType()));
    ensureColumn("sys_menu_items", "actions", nullable(dialect.varchar(128)));
    ensureColumn("sys_menu_items", "version", nullable(dialect.intType()));
    ensureColumn("sys_menu_items", "created_at", nullable(dialect.dateTimeType()));
    ensureColumn("sys_menu_items", "updated_at", nullable(dialect.dateTimeType()));
  }

  private void ensureRoleMenusTable() {
    if (!tableExists("role_menus")) {
      log.info("创建表 role_menus");
      jdbc.execute(
        "CREATE TABLE role_menus (" +
          column("role_id", dialect.bigIntType(), "NOT NULL") + ", " +
          column("menu_id", dialect.bigIntType(), "NOT NULL") + ", " +
          "PRIMARY KEY (role_id, menu_id)" +
          ")"
      );
    }
  }

  private void ensureUiBrandSettingsTable() {
    if (!tableExists("ui_brand_settings")) {
      log.info("create table ui_brand_settings");
      jdbc.execute(
        "CREATE TABLE ui_brand_settings (" +
          dialect.identityPrimaryKey("id") + ", " +
          column("website_name", dialect.varchar(100), null) + ", " +
          column("app_version", dialect.varchar(50), null) + ", " +
          column("logo_expanded_url", dialect.varchar(255), null) + ", " +
          column("logo_collapsed_url", dialect.varchar(255), null) + ", " +
          column("favicon_url", dialect.varchar(255), null) + ", " +
          column("qr_code_url", dialect.varchar(255), null) +
          ")"
      );
      return;
    }
    ensureColumn("ui_brand_settings", "website_name", nullable(dialect.varchar(100)));
    ensureColumn("ui_brand_settings", "app_version", nullable(dialect.varchar(50)));
    ensureColumn("ui_brand_settings", "logo_expanded_url", nullable(dialect.varchar(255)));
    ensureColumn("ui_brand_settings", "logo_collapsed_url", nullable(dialect.varchar(255)));
    ensureColumn("ui_brand_settings", "favicon_url", nullable(dialect.varchar(255)));
    ensureColumn("ui_brand_settings", "qr_code_url", nullable(dialect.varchar(255)));
  }

  private void ensureUiLayoutSettingsTable() {
    if (!tableExists("ui_layout_settings")) {
      log.info("create table ui_layout_settings");
      jdbc.execute(
        "CREATE TABLE ui_layout_settings (" +
          dialect.identityPrimaryKey("id") + ", " +
          column("default_home", dialect.varchar(255), null) + ", " +
          column("show_footer", dialect.tinyIntType(), null) + ", " +
          column("is_sidebar_compact", dialect.tinyIntType(), null) + ", " +
          column("show_breadcrumb", dialect.tinyIntType(), null) + ", " +
          column("menu_auto_collapsed", dialect.tinyIntType(), null) + ", " +
          column("layout", dialect.varchar(20), null) + ", " +
          column("split_menu", dialect.tinyIntType(), null) + ", " +
          column("side_mode", dialect.varchar(20), null) + ", " +
          column("is_footer_aside", dialect.tinyIntType(), null) + ", " +
          column("is_sidebar_fixed", dialect.tinyIntType(), null) + ", " +
          column("is_header_fixed", dialect.tinyIntType(), null) + ", " +
          column("is_use_tabs_router", dialect.tinyIntType(), null) + ", " +
          column("show_header", dialect.tinyIntType(), null) +
          ")"
      );
      return;
    }
    ensureColumn("ui_layout_settings", "default_home", nullable(dialect.varchar(255)));
    ensureColumn("ui_layout_settings", "show_footer", nullable(dialect.tinyIntType()));
    ensureColumn("ui_layout_settings", "is_sidebar_compact", nullable(dialect.tinyIntType()));
    ensureColumn("ui_layout_settings", "show_breadcrumb", nullable(dialect.tinyIntType()));
    ensureColumn("ui_layout_settings", "menu_auto_collapsed", nullable(dialect.tinyIntType()));
    ensureColumn("ui_layout_settings", "layout", nullable(dialect.varchar(20)));
    ensureColumn("ui_layout_settings", "split_menu", nullable(dialect.tinyIntType()));
    ensureColumn("ui_layout_settings", "side_mode", nullable(dialect.varchar(20)));
    ensureColumn("ui_layout_settings", "is_footer_aside", nullable(dialect.tinyIntType()));
    ensureColumn("ui_layout_settings", "is_sidebar_fixed", nullable(dialect.tinyIntType()));
    ensureColumn("ui_layout_settings", "is_header_fixed", nullable(dialect.tinyIntType()));
    ensureColumn("ui_layout_settings", "is_use_tabs_router", nullable(dialect.tinyIntType()));
    ensureColumn("ui_layout_settings", "show_header", nullable(dialect.tinyIntType()));
  }

  private void ensureUiThemeSettingsTable() {
    if (!tableExists("ui_theme_settings")) {
      log.info("create table ui_theme_settings");
      jdbc.execute(
        "CREATE TABLE ui_theme_settings (" +
          dialect.identityPrimaryKey("id") + ", " +
          column("auto_theme", dialect.tinyIntType(), null) + ", " +
          column("light_start_time", dialect.varchar(10), null) + ", " +
          column("dark_start_time", dialect.varchar(10), null) + ", " +
          column("mode", dialect.varchar(20), null) + ", " +
          column("brand_theme", dialect.varchar(20), null) +
          ")"
      );
      return;
    }
    ensureColumn("ui_theme_settings", "auto_theme", nullable(dialect.tinyIntType()));
    ensureColumn("ui_theme_settings", "light_start_time", nullable(dialect.varchar(10)));
    ensureColumn("ui_theme_settings", "dark_start_time", nullable(dialect.varchar(10)));
    ensureColumn("ui_theme_settings", "mode", nullable(dialect.varchar(20)));
    ensureColumn("ui_theme_settings", "brand_theme", nullable(dialect.varchar(20)));
  }

  private void ensureUiFooterSettingsTable() {
    if (!tableExists("ui_footer_settings")) {
      log.info("create table ui_footer_settings");
      jdbc.execute(
        "CREATE TABLE ui_footer_settings (" +
          dialect.identityPrimaryKey("id") + ", " +
          column("footer_company", dialect.varchar(100), null) + ", " +
          column("footer_icp", dialect.varchar(100), null) + ", " +
          column("copyright_start_year", dialect.varchar(10), null) +
          ")"
      );
      return;
    }
    ensureColumn("ui_footer_settings", "footer_company", nullable(dialect.varchar(100)));
    ensureColumn("ui_footer_settings", "footer_icp", nullable(dialect.varchar(100)));
    ensureColumn("ui_footer_settings", "copyright_start_year", nullable(dialect.varchar(10)));
  }

  private void ensureUiLoginSettingsTable() {
    if (!tableExists("ui_login_settings")) {
      log.info("create table ui_login_settings");
      jdbc.execute(
        "CREATE TABLE ui_login_settings (" +
          dialect.identityPrimaryKey("id") + ", " +
          column("login_bg_url", dialect.varchar(255), null) + ", " +
          column("allow_multi_device_login", dialect.tinyIntType(), null) +
          ")"
      );
      return;
    }
    ensureColumn("ui_login_settings", "login_bg_url", nullable(dialect.varchar(255)));
    ensureColumn("ui_login_settings", "allow_multi_device_login", nullable(dialect.tinyIntType()));
  }

  private void ensureUiLegalSettingsTable() {
    if (!tableExists("ui_legal_settings")) {
      log.info("create table ui_legal_settings");
      jdbc.execute(
        "CREATE TABLE ui_legal_settings (" +
          dialect.identityPrimaryKey("id") + ", " +
          column("user_agreement", dialect.textType(), null) + ", " +
          column("privacy_agreement", dialect.textType(), null) +
          ")"
      );
      return;
    }
    ensureColumn("ui_legal_settings", "user_agreement", nullable(dialect.textType()));
    ensureColumn("ui_legal_settings", "privacy_agreement", nullable(dialect.textType()));
  }

  private void ensureUiSystemSettingsTable() {
    if (!tableExists("ui_system_settings")) {
      log.info("create table ui_system_settings");
      jdbc.execute(
        "CREATE TABLE ui_system_settings (" +
          dialect.identityPrimaryKey("id") + ", " +
          column("log_retention_days", dialect.intType(), null) + ", " +
          column("ai_assistant_enabled", dialect.tinyIntType(), null) +
          ")"
      );
      return;
    }
    ensureColumn("ui_system_settings", "log_retention_days", nullable(dialect.intType()));
    ensureColumn("ui_system_settings", "ai_assistant_enabled", nullable(dialect.tinyIntType()));
  }

  private void ensureVerificationSmsSettingsTable() {
    if (!tableExists("verification_sms_settings")) {
      log.info("create table verification_sms_settings");
      jdbc.execute(
        "CREATE TABLE verification_sms_settings (" +
          dialect.identityPrimaryKey("id") + ", " +
          column("sms_enabled", dialect.tinyIntType(), null) + ", " +
          column("sms_provider", dialect.varchar(32), null) + ", " +
          column("sms_aliyun_enabled", dialect.tinyIntType(), null) + ", " +
          column("sms_aliyun_access_key_id", dialect.varchar(256), null) + ", " +
          column("sms_aliyun_access_key_secret", dialect.varchar(256), null) + ", " +
          column("sms_aliyun_sign_name", dialect.varchar(128), null) + ", " +
          column("sms_aliyun_template_code", dialect.varchar(64), null) + ", " +
          column("sms_aliyun_region_id", dialect.varchar(64), null) + ", " +
          column("sms_aliyun_endpoint", dialect.varchar(255), null) + ", " +
          column("sms_tencent_enabled", dialect.tinyIntType(), null) + ", " +
          column("sms_tencent_secret_id", dialect.varchar(256), null) + ", " +
          column("sms_tencent_secret_key", dialect.varchar(256), null) + ", " +
          column("sms_tencent_sign_name", dialect.varchar(128), null) + ", " +
          column("sms_tencent_template_id", dialect.varchar(64), null) + ", " +
          column("sms_tencent_region", dialect.varchar(64), null) + ", " +
          column("sms_tencent_endpoint", dialect.varchar(255), null) + ", " +
          column("sms_sdk_app_id", dialect.varchar(64), null) +
          ")"
      );
      return;
    }
    ensureColumn("verification_sms_settings", "sms_enabled", nullable(dialect.tinyIntType()));
    ensureColumn("verification_sms_settings", "sms_provider", nullable(dialect.varchar(32)));
    ensureColumn("verification_sms_settings", "sms_aliyun_enabled", nullable(dialect.tinyIntType()));
    ensureColumn("verification_sms_settings", "sms_aliyun_access_key_id", nullable(dialect.varchar(256)));
    ensureColumn("verification_sms_settings", "sms_aliyun_access_key_secret", nullable(dialect.varchar(256)));
    ensureColumn("verification_sms_settings", "sms_aliyun_sign_name", nullable(dialect.varchar(128)));
    ensureColumn("verification_sms_settings", "sms_aliyun_template_code", nullable(dialect.varchar(64)));
    ensureColumn("verification_sms_settings", "sms_aliyun_region_id", nullable(dialect.varchar(64)));
    ensureColumn("verification_sms_settings", "sms_aliyun_endpoint", nullable(dialect.varchar(255)));
    ensureColumn("verification_sms_settings", "sms_tencent_enabled", nullable(dialect.tinyIntType()));
    ensureColumn("verification_sms_settings", "sms_tencent_secret_id", nullable(dialect.varchar(256)));
    ensureColumn("verification_sms_settings", "sms_tencent_secret_key", nullable(dialect.varchar(256)));
    ensureColumn("verification_sms_settings", "sms_tencent_sign_name", nullable(dialect.varchar(128)));
    ensureColumn("verification_sms_settings", "sms_tencent_template_id", nullable(dialect.varchar(64)));
    ensureColumn("verification_sms_settings", "sms_tencent_region", nullable(dialect.varchar(64)));
    ensureColumn("verification_sms_settings", "sms_tencent_endpoint", nullable(dialect.varchar(255)));
    ensureColumn("verification_sms_settings", "sms_sdk_app_id", nullable(dialect.varchar(64)));
  }

  private void ensureVerificationEmailSettingsTable() {
    if (!tableExists("verification_email_settings")) {
      log.info("create table verification_email_settings");
      jdbc.execute(
        "CREATE TABLE verification_email_settings (" +
          dialect.identityPrimaryKey("id") + ", " +
          column("email_enabled", dialect.tinyIntType(), null) + ", " +
          column("email_host", dialect.varchar(255), null) + ", " +
          column("email_port", dialect.intType(), null) + ", " +
          column("email_username", dialect.varchar(128), null) + ", " +
          column("email_password", dialect.varchar(256), null) + ", " +
          column("email_from", dialect.varchar(128), null) + ", " +
          column("email_ssl", dialect.tinyIntType(), null) +
          ")"
      );
      return;
    }
    ensureColumn("verification_email_settings", "email_enabled", nullable(dialect.tinyIntType()));
    ensureColumn("verification_email_settings", "email_host", nullable(dialect.varchar(255)));
    ensureColumn("verification_email_settings", "email_port", nullable(dialect.intType()));
    ensureColumn("verification_email_settings", "email_username", nullable(dialect.varchar(128)));
    ensureColumn("verification_email_settings", "email_password", nullable(dialect.varchar(256)));
    ensureColumn("verification_email_settings", "email_from", nullable(dialect.varchar(128)));
    ensureColumn("verification_email_settings", "email_ssl", nullable(dialect.tinyIntType()));
  }

  private void ensureSecurityTokenSettingsTable() {
    if (!tableExists("security_token_settings")) {
      log.info("create table security_token_settings");
      jdbc.execute(
        "CREATE TABLE security_token_settings (" +
          dialect.identityPrimaryKey("id") + ", " +
          column("session_timeout_minutes", dialect.intType(), null) + ", " +
          column("token_timeout_minutes", dialect.intType(), null) + ", " +
          column("token_refresh_grace_minutes", dialect.intType(), null) +
          ")"
      );
      return;
    }
    ensureColumn("security_token_settings", "session_timeout_minutes", nullable(dialect.intType()));
    ensureColumn("security_token_settings", "token_timeout_minutes", nullable(dialect.intType()));
    ensureColumn("security_token_settings", "token_refresh_grace_minutes", nullable(dialect.intType()));
  }

  private void ensureSecurityCaptchaSettingsTable() {
    if (!tableExists("security_captcha_settings")) {
      log.info("create table security_captcha_settings");
      jdbc.execute(
        "CREATE TABLE security_captcha_settings (" +
          dialect.identityPrimaryKey("id") + ", " +
          column("captcha_enabled", dialect.tinyIntType(), null) + ", " +
          column("captcha_type", dialect.varchar(20), null) + ", " +
          column("drag_captcha_width", dialect.intType(), null) + ", " +
          column("drag_captcha_height", dialect.intType(), null) + ", " +
          column("drag_captcha_threshold", dialect.intType(), null) + ", " +
          column("image_captcha_length", dialect.intType(), null) + ", " +
          column("image_captcha_noise_lines", dialect.intType(), null) +
          ")"
      );
      return;
    }
    ensureColumn("security_captcha_settings", "captcha_enabled", nullable(dialect.tinyIntType()));
    ensureColumn("security_captcha_settings", "captcha_type", nullable(dialect.varchar(20)));
    ensureColumn("security_captcha_settings", "drag_captcha_width", nullable(dialect.intType()));
    ensureColumn("security_captcha_settings", "drag_captcha_height", nullable(dialect.intType()));
    ensureColumn("security_captcha_settings", "drag_captcha_threshold", nullable(dialect.intType()));
    ensureColumn("security_captcha_settings", "image_captcha_length", nullable(dialect.intType()));
    ensureColumn("security_captcha_settings", "image_captcha_noise_lines", nullable(dialect.intType()));
  }

  private void ensureSecurityPasswordPolicyTable() {
    if (!tableExists("security_password_policy")) {
      log.info("create table security_password_policy");
      jdbc.execute(
        "CREATE TABLE security_password_policy (" +
          dialect.identityPrimaryKey("id") + ", " +
          column("password_min_length", dialect.intType(), null) + ", " +
          column("password_require_uppercase", dialect.tinyIntType(), null) + ", " +
          column("password_require_lowercase", dialect.tinyIntType(), null) + ", " +
          column("password_require_special", dialect.tinyIntType(), null) + ", " +
          column("password_allow_sequential", dialect.tinyIntType(), null) +
          ")"
      );
      return;
    }
    ensureColumn("security_password_policy", "password_min_length", nullable(dialect.intType()));
    ensureColumn("security_password_policy", "password_require_uppercase", nullable(dialect.tinyIntType()));
    ensureColumn("security_password_policy", "password_require_lowercase", nullable(dialect.tinyIntType()));
    ensureColumn("security_password_policy", "password_require_special", nullable(dialect.tinyIntType()));
    ensureColumn("security_password_policy", "password_allow_sequential", nullable(dialect.tinyIntType()));
  }

  private void ensureOperationLogsTable() {
    if (!tableExists("operation_logs")) {
      log.info("创建表 operation_logs");
      jdbc.execute(
        "CREATE TABLE operation_logs (" +
          dialect.identityPrimaryKey("id") + ", " +
          column("action", dialect.varchar(32), "NOT NULL") + ", " +
          column("module", dialect.varchar(64), null) + ", " +
          column("detail", dialect.varchar(512), null) + ", " +
          column("user_id", dialect.bigIntType(), null) + ", " +
          column("user_guid", dialect.varchar(36), null) + ", " +
          column("account", dialect.varchar(64), null) + ", " +
          column("ip_address", dialect.varchar(64), null) + ", " +
          column("device_model", dialect.varchar(128), null) + ", " +
          column("os", dialect.varchar(64), null) + ", " +
          column("browser", dialect.varchar(64), null) + ", " +
          column("created_at", dialect.dateTimeType(), null) +
          ")"
      );
      return;
    }
    ensureColumn("operation_logs", "action", dialect.varchar(32) + " NOT NULL");
    ensureColumn("operation_logs", "module", nullable(dialect.varchar(64)));
    ensureColumn("operation_logs", "detail", nullable(dialect.varchar(512)));
    ensureColumn("operation_logs", "user_id", nullable(dialect.bigIntType()));
    ensureColumn("operation_logs", "user_guid", nullable(dialect.varchar(36)));
    ensureColumn("operation_logs", "account", nullable(dialect.varchar(64)));
    ensureColumn("operation_logs", "ip_address", nullable(dialect.varchar(64)));
    ensureColumn("operation_logs", "device_model", nullable(dialect.varchar(128)));
    ensureColumn("operation_logs", "os", nullable(dialect.varchar(64)));
    ensureColumn("operation_logs", "browser", nullable(dialect.varchar(64)));
    ensureColumn("operation_logs", "created_at", nullable(dialect.dateTimeType()));
  }

  private void ensureUserParametersTable() {
    if (!tableExists("user_parameters")) {
      log.info("创建表 user_parameters");
      jdbc.execute(
        "CREATE TABLE user_parameters (" +
          dialect.identityPrimaryKey("id") + ", " +
          column("user_id", dialect.bigIntType(), "NOT NULL") + ", " +
          column("param_key", dialect.varchar(100), "NOT NULL") + ", " +
          column("param_value", dialect.textType(), "NOT NULL") + ", " +
          column("description", dialect.varchar(255), null) + ", " +
          column("created_at", dialect.dateTimeType(), null) + ", " +
          column("updated_at", dialect.dateTimeType(), null) +
          ")"
      );
    }
  }

  private void ensureStorageSettingsTable() {
    if (!tableExists("storage_settings")) {
      log.info("创建表 storage_settings");
      jdbc.execute(
        "CREATE TABLE storage_settings (" +
          dialect.identityPrimaryKey("id") + ", " +
          column("provider", dialect.varchar(20), "NOT NULL") + ", " +
          column("bucket", dialect.varchar(128), null) + ", " +
          column("region", dialect.varchar(128), null) + ", " +
          column("endpoint", dialect.varchar(255), null) + ", " +
          column("access_key", dialect.varchar(128), null) + ", " +
          column("secret_key", dialect.varchar(128), null) + ", " +
          column("custom_domain", dialect.varchar(255), null) + ", " +
          column("path_prefix", dialect.varchar(128), null) + ", " +
          column("public_read", dialect.tinyIntType(), null) +
          ")"
      );
      return;
    }
    ensureColumn("storage_settings", "provider", dialect.varchar(20) + " NOT NULL");
    ensureColumn("storage_settings", "bucket", nullable(dialect.varchar(128)));
    ensureColumn("storage_settings", "region", nullable(dialect.varchar(128)));
    ensureColumn("storage_settings", "endpoint", nullable(dialect.varchar(255)));
    ensureColumn("storage_settings", "access_key", nullable(dialect.varchar(128)));
    ensureColumn("storage_settings", "secret_key", nullable(dialect.varchar(128)));
    ensureColumn("storage_settings", "custom_domain", nullable(dialect.varchar(255)));
    ensureColumn("storage_settings", "path_prefix", nullable(dialect.varchar(128)));
    ensureColumn("storage_settings", "public_read", nullable(dialect.tinyIntType()));
  }

  private void ensureAnnouncementTable() {
    if (!tableExists("announcements")) {
      log.info("创建表 announcements");
      jdbc.execute(
        "CREATE TABLE announcements (" +
          dialect.identityPrimaryKey("id") + ", " +
          column("title", dialect.varchar(200), "NOT NULL") + ", " +
          column("summary", dialect.varchar(200), null) + ", " +
          column("content", dialect.textType(), "NOT NULL") + ", " +
          column("type", dialect.varchar(32), "NOT NULL") + ", " +
          column("priority", dialect.varchar(16), "NOT NULL") + ", " +
          column("status", dialect.varchar(16), "NOT NULL") + ", " +
          column("cover_url", dialect.varchar(255), null) + ", " +
          column("attachment_url", dialect.varchar(255), null) + ", " +
          column("attachment_name", dialect.varchar(255), null) + ", " +
          column("publish_at", dialect.dateTimeType(), null) + ", " +
          column("created_at", dialect.dateTimeType(), null) + ", " +
          column("updated_at", dialect.dateTimeType(), null) + ", " +
          column("created_by_id", dialect.bigIntType(), null) + ", " +
          column("created_by_name", dialect.varchar(64), null) + ", " +
          column("is_broadcasted", dialect.tinyIntType(), "NOT NULL DEFAULT 0") +
          ")"
      );
    } else {
      ensureColumn("announcements", "summary", nullable(dialect.varchar(200)));
      ensureColumn("announcements", "cover_url", nullable(dialect.varchar(255)));
      ensureColumn("announcements", "attachment_url", nullable(dialect.varchar(255)));
      ensureColumn("announcements", "attachment_name", nullable(dialect.varchar(255)));
      ensureColumn("announcements", "publish_at", nullable(dialect.dateTimeType()));
      ensureColumn("announcements", "created_by_id", nullable(dialect.bigIntType()));
      ensureColumn("announcements", "created_by_name", nullable(dialect.varchar(64)));
      ensureColumn("announcements", "is_broadcasted", dialect.tinyIntType() + " NOT NULL DEFAULT 0");
    }
    seedAnnouncementData();
  }

  private void ensureNotificationTable() {
    if (!tableExists("notifications")) {
      log.info("创建表 notifications");
      jdbc.execute(
        "CREATE TABLE notifications (" +
          dialect.identityPrimaryKey("id") + ", " +
          column("title", dialect.varchar(200), "NOT NULL") + ", " +
          column("summary", dialect.varchar(200), null) + ", " +
          column("content", dialect.textType(), "NOT NULL") + ", " +
          column("priority", dialect.varchar(16), "NOT NULL") + ", " +
          column("status", dialect.varchar(16), "NOT NULL") + ", " +
          column("type", dialect.varchar(32), null) + ", " +
          column("cover_url", dialect.varchar(512), null) + ", " +
          column("attachment_url", dialect.varchar(512), null) + ", " +
          column("attachment_name", dialect.varchar(255), null) + ", " +
          column("publish_at", dialect.dateTimeType(), null) + ", " +
          column("created_at", dialect.dateTimeType(), null) + ", " +
          column("updated_at", dialect.dateTimeType(), null) + ", " +
          column("created_by_id", dialect.bigIntType(), null) + ", " +
          column("created_by_name", dialect.varchar(64), null) +
          ")"
      );
      return;
    }
    ensureColumn("notifications", "summary", nullable(dialect.varchar(200)));
    ensureColumn("notifications", "type", nullable(dialect.varchar(32)));
    ensureColumn("notifications", "cover_url", nullable(dialect.varchar(512)));
    ensureColumn("notifications", "attachment_url", nullable(dialect.varchar(512)));
    ensureColumn("notifications", "attachment_name", nullable(dialect.varchar(255)));
    ensureColumn("notifications", "publish_at", nullable(dialect.dateTimeType()));
    ensureColumn("notifications", "created_by_id", nullable(dialect.bigIntType()));
    ensureColumn("notifications", "created_by_name", nullable(dialect.varchar(64)));
  }

  private void ensureAiProviderSettingsTable() {
    if (!tableExists("ai_provider_settings")) {
      log.info("创建表 ai_provider_settings");
      jdbc.execute(
        "CREATE TABLE ai_provider_settings (" +
          dialect.identityPrimaryKey("id") + ", " +
          column("name", dialect.varchar(64), "NOT NULL") + ", " +
          column("vendor", dialect.varchar(32), "NOT NULL") + ", " +
          column("base_url", dialect.varchar(255), "NOT NULL") + ", " +
          column("endpoint_path", dialect.varchar(255), null) + ", " +
          column("model", dialect.varchar(128), null) + ", " +
          column("api_key", dialect.varchar(512), null) + ", " +
          column("api_version", dialect.varchar(64), null) + ", " +
          column("temperature", dialect.doubleType(), null) + ", " +
          column("max_tokens", dialect.intType(), null) + ", " +
          column("is_default", dialect.tinyIntType(), null) + ", " +
          column("enabled", dialect.tinyIntType(), null) + ", " +
          column("extra_headers", dialect.varchar(2000), null) + ", " +
          column("remark", dialect.varchar(512), null) + ", " +
          column("last_test_status", dialect.varchar(32), null) + ", " +
          column("last_test_message", dialect.varchar(512), null) + ", " +
          column("last_tested_at", dialect.dateTimeType(), null) + ", " +
          column("created_at", dialect.dateTimeType(), null) + ", " +
          column("updated_at", dialect.dateTimeType(), null) +
          ")"
      );
      return;
    }
    ensureColumn("ai_provider_settings", "endpoint_path", nullable(dialect.varchar(255)));
    ensureColumn("ai_provider_settings", "api_version", nullable(dialect.varchar(64)));
    ensureColumn("ai_provider_settings", "temperature", nullable(dialect.doubleType()));
    ensureColumn("ai_provider_settings", "max_tokens", nullable(dialect.intType()));
    ensureColumn("ai_provider_settings", "is_default", nullable(dialect.tinyIntType()));
    ensureColumn("ai_provider_settings", "enabled", nullable(dialect.tinyIntType()));
    ensureColumn("ai_provider_settings", "extra_headers", nullable(dialect.varchar(2000)));
    ensureColumn("ai_provider_settings", "remark", nullable(dialect.varchar(512)));
    ensureColumn("ai_provider_settings", "last_test_status", nullable(dialect.varchar(32)));
    ensureColumn("ai_provider_settings", "last_test_message", nullable(dialect.varchar(512)));
    ensureColumn("ai_provider_settings", "last_tested_at", nullable(dialect.dateTimeType()));
    ensureColumn("ai_provider_settings", "created_at", nullable(dialect.dateTimeType()));
    ensureColumn("ai_provider_settings", "updated_at", nullable(dialect.dateTimeType()));
  }

  private void ensureSensitiveWordsTable() {
    if (!tableExists("sensitive_words")) {
      log.info("创建表 sensitive_words");
      jdbc.execute(
        "CREATE TABLE sensitive_words (" +
          dialect.identityPrimaryKey("id") + ", " +
          column("word", dialect.varchar(200), "NOT NULL UNIQUE") + ", " +
          column("enabled", dialect.tinyIntType(), "NOT NULL") + ", " +
          column("created_at", dialect.dateTimeType(), null) + ", " +
          column("updated_at", dialect.dateTimeType(), null) +
          ")"
      );
      return;
    }
    ensureColumn("sensitive_words", "word", dialect.varchar(200) + " NOT NULL");
    ensureColumn("sensitive_words", "enabled", dialect.tinyIntType() + " NOT NULL");
    ensureColumn("sensitive_words", "created_at", nullable(dialect.dateTimeType()));
    ensureColumn("sensitive_words", "updated_at", nullable(dialect.dateTimeType()));
  }

  private void ensureSensitivePageSettingsTable() {
    if (!tableExists("sensitive_page_settings")) {
      log.info("创建表 sensitive_page_settings");
      jdbc.execute(
        "CREATE TABLE sensitive_page_settings (" +
          dialect.identityPrimaryKey("id") + ", " +
          column("page_key", dialect.varchar(255), "NOT NULL UNIQUE") + ", " +
          column("page_name", dialect.varchar(255), null) + ", " +
          column("enabled", dialect.tinyIntType(), "NOT NULL") + ", " +
          column("created_at", dialect.dateTimeType(), null) + ", " +
          column("updated_at", dialect.dateTimeType(), null) +
          ")"
      );
      return;
    }
    ensureColumn("sensitive_page_settings", "page_key", dialect.varchar(255) + " NOT NULL");
    ensureColumn("sensitive_page_settings", "page_name", nullable(dialect.varchar(255)));
    ensureColumn("sensitive_page_settings", "enabled", dialect.tinyIntType() + " NOT NULL");
    ensureColumn("sensitive_page_settings", "created_at", nullable(dialect.dateTimeType()));
    ensureColumn("sensitive_page_settings", "updated_at", nullable(dialect.dateTimeType()));
  }

  private void ensureSensitiveSettingsTable() {
    if (!tableExists("sensitive_settings")) {
      log.info("创建表 sensitive_settings");
      jdbc.execute(
        "CREATE TABLE sensitive_settings (" +
          dialect.identityPrimaryKey("id") + ", " +
          column("enabled", dialect.tinyIntType(), "NOT NULL") + ", " +
          column("updated_at", dialect.dateTimeType(), null) +
          ")"
      );
      return;
    }
    ensureColumn("sensitive_settings", "enabled", dialect.tinyIntType() + " NOT NULL");
    ensureColumn("sensitive_settings", "updated_at", nullable(dialect.dateTimeType()));
  }

  private void seedAnnouncementData() {
    try {
      Integer cnt = jdbc.queryForObject("select count(*) from announcements", Integer.class);
      if (cnt != null && cnt > 0) return;
      String now = dialect.currentTimestampFunction();
      String sql =
        "INSERT INTO announcements (" +
          "title, summary, content, type, priority, status, cover_url, publish_at, created_at, updated_at, created_by_name" +
          ") VALUES (?, ?, ?, ?, ?, ?, ?, " + now + ", " + now + ", " + now + ", ?)";
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
    try (Connection conn = dataSource.getConnection()) {
      DatabaseMetaData meta = conn.getMetaData();
      String catalog = conn.getCatalog();
      String schema = resolveSchema(meta, conn);
      if (tableExists(meta, catalog, schema, tableName)) {
        return true;
      }
      return tableExists(meta, catalog, schema, tableName.toUpperCase());
    } catch (SQLException e) {
      log.warn("检查表是否存在失败: {}", e.getMessage());
      return false;
    }
  }

  private boolean tableExists(DatabaseMetaData meta, String catalog, String schema, String tableName)
    throws SQLException {
    try (ResultSet rs = meta.getTables(catalog, schema, tableName, new String[] {"TABLE"})) {
      return rs.next();
    }
  }

  private boolean columnExists(String tableName, String columnName) {
    try (Connection conn = dataSource.getConnection()) {
      DatabaseMetaData meta = conn.getMetaData();
      String catalog = conn.getCatalog();
      String schema = resolveSchema(meta, conn);
      if (columnExists(meta, catalog, schema, tableName, columnName)) {
        return true;
      }
      return columnExists(meta, catalog, schema, tableName.toUpperCase(), columnName.toUpperCase());
    } catch (SQLException e) {
      log.warn("检查字段是否存在失败: {}", e.getMessage());
      return false;
    }
  }

  private boolean columnExists(DatabaseMetaData meta, String catalog, String schema, String tableName, String columnName)
    throws SQLException {
    try (ResultSet rs = meta.getColumns(catalog, schema, tableName, columnName)) {
      return rs.next();
    }
  }

  private String resolveSchema(DatabaseMetaData meta, Connection conn) throws SQLException {
    String schema = conn.getSchema();
    if (schema != null && !schema.isBlank()) {
      return schema;
    }
    String user = meta.getUserName();
    if (user != null && !user.isBlank()) {
      return user;
    }
    return null;
  }

  private void ensureColumn(String tableName, String columnName, String ddlType) {
    if (columnExists(tableName, columnName)) return;
    String sql = dialect.addColumnClause(tableName, columnName, ddlType);
    log.info("补齐字段: {}.{}", tableName, columnName);
    jdbc.execute(sql);
  }

  private String column(String name, String type, String constraints) {
    if (constraints == null || constraints.isBlank()) {
      return name + " " + type;
    }
    return name + " " + type + " " + constraints;
  }

  private String timestampColumn(String name, boolean notNull, boolean withDefault, boolean autoUpdate) {
    StringBuilder builder = new StringBuilder(name).append(" ").append(dialect.dateTimeType());
    builder.append(notNull ? " NOT NULL" : " NULL");
    if (withDefault) {
      builder.append(" DEFAULT ").append(dialect.currentTimestampFunction());
    }
    if (autoUpdate) {
      builder.append(dialect.onUpdateClause());
    }
    return builder.toString();
  }

  private String nullable(String type) {
    return type + " NULL";
  }

  private enum DatabaseDialect {
    MYSQL("mysql", "MySQL") {
      @Override
      String identityPrimaryKey(String name) {
        return name + " BIGINT PRIMARY KEY AUTO_INCREMENT";
      }

      @Override
      String varchar(int length) {
        return "VARCHAR(" + length + ")";
      }

      @Override
      String textType() {
        return "TEXT";
      }

      @Override
      String tinyIntType() {
        return "TINYINT";
      }

      @Override
      String intType() {
        return "INT";
      }

      @Override
      String bigIntType() {
        return "BIGINT";
      }

      @Override
      String doubleType() {
        return "DOUBLE";
      }

      @Override
      String dateType() {
        return "DATE";
      }

      @Override
      String dateTimeType() {
        return "DATETIME";
      }

      @Override
      String currentTimestampFunction() {
        return "CURRENT_TIMESTAMP";
      }

      @Override
      String onUpdateClause() {
        return " ON UPDATE " + currentTimestampFunction();
      }

      @Override
      String uniqueConstraint(String name, String columns) {
        return "UNIQUE KEY " + name + " (" + columns + ")";
      }

      @Override
      String addColumnClause(String table, String column, String ddlType) {
        return "ALTER TABLE " + table + " ADD COLUMN " + column + " " + ddlType;
      }
    },
    POSTGRESQL("postgresql", "PostgreSQL") {
      @Override
      String identityPrimaryKey(String name) {
        return name + " BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY";
      }

      @Override
      String varchar(int length) {
        return "VARCHAR(" + length + ")";
      }

      @Override
      String textType() {
        return "TEXT";
      }

      @Override
      String tinyIntType() {
        return "SMALLINT";
      }

      @Override
      String intType() {
        return "INTEGER";
      }

      @Override
      String bigIntType() {
        return "BIGINT";
      }

      @Override
      String doubleType() {
        return "DOUBLE PRECISION";
      }

      @Override
      String dateType() {
        return "DATE";
      }

      @Override
      String dateTimeType() {
        return "TIMESTAMP";
      }

      @Override
      String currentTimestampFunction() {
        return "CURRENT_TIMESTAMP";
      }

      @Override
      String onUpdateClause() {
        return "";
      }

      @Override
      String uniqueConstraint(String name, String columns) {
        return "CONSTRAINT " + name + " UNIQUE (" + columns + ")";
      }

      @Override
      String addColumnClause(String table, String column, String ddlType) {
        return "ALTER TABLE " + table + " ADD COLUMN " + column + " " + ddlType;
      }
    },
    ORACLE("oracle", "Oracle") {
      @Override
      String identityPrimaryKey(String name) {
        return name + " NUMBER(19) GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY";
      }

      @Override
      String varchar(int length) {
        return "VARCHAR2(" + length + ")";
      }

      @Override
      String textType() {
        return "CLOB";
      }

      @Override
      String tinyIntType() {
        return "NUMBER(3)";
      }

      @Override
      String intType() {
        return "NUMBER(10)";
      }

      @Override
      String bigIntType() {
        return "NUMBER(19)";
      }

      @Override
      String doubleType() {
        return "NUMBER";
      }

      @Override
      String dateType() {
        return "DATE";
      }

      @Override
      String dateTimeType() {
        return "TIMESTAMP";
      }

      @Override
      String currentTimestampFunction() {
        return "SYSTIMESTAMP";
      }

      @Override
      String onUpdateClause() {
        return "";
      }

      @Override
      String uniqueConstraint(String name, String columns) {
        return "CONSTRAINT " + name + " UNIQUE (" + columns + ")";
      }

      @Override
      String addColumnClause(String table, String column, String ddlType) {
        return "ALTER TABLE " + table + " ADD " + column + " " + ddlType;
      }
    },
    SQLSERVER("sqlserver", "Microsoft SQL Server") {
      @Override
      String identityPrimaryKey(String name) {
        return name + " BIGINT IDENTITY(1,1) PRIMARY KEY";
      }

      @Override
      String varchar(int length) {
        return "NVARCHAR(" + length + ")";
      }

      @Override
      String textType() {
        return "NVARCHAR(MAX)";
      }

      @Override
      String tinyIntType() {
        return "TINYINT";
      }

      @Override
      String intType() {
        return "INT";
      }

      @Override
      String bigIntType() {
        return "BIGINT";
      }

      @Override
      String doubleType() {
        return "FLOAT";
      }

      @Override
      String dateType() {
        return "DATE";
      }

      @Override
      String dateTimeType() {
        return "DATETIME2";
      }

      @Override
      String currentTimestampFunction() {
        return "SYSDATETIME()";
      }

      @Override
      String onUpdateClause() {
        return "";
      }

      @Override
      String uniqueConstraint(String name, String columns) {
        return "CONSTRAINT " + name + " UNIQUE (" + columns + ")";
      }

      @Override
      String addColumnClause(String table, String column, String ddlType) {
        return "ALTER TABLE " + table + " ADD " + column + " " + ddlType;
      }
    },
    UNKNOWN("unknown", null) {
      @Override
      String identityPrimaryKey(String name) {
        return MYSQL.identityPrimaryKey(name);
      }

      @Override
      String varchar(int length) {
        return MYSQL.varchar(length);
      }

      @Override
      String textType() {
        return MYSQL.textType();
      }

      @Override
      String tinyIntType() {
        return MYSQL.tinyIntType();
      }

      @Override
      String intType() {
        return MYSQL.intType();
      }

      @Override
      String bigIntType() {
        return MYSQL.bigIntType();
      }

      @Override
      String doubleType() {
        return MYSQL.doubleType();
      }

      @Override
      String dateType() {
        return MYSQL.dateType();
      }

      @Override
      String dateTimeType() {
        return MYSQL.dateTimeType();
      }

      @Override
      String currentTimestampFunction() {
        return MYSQL.currentTimestampFunction();
      }

      @Override
      String onUpdateClause() {
        return MYSQL.onUpdateClause();
      }

      @Override
      String uniqueConstraint(String name, String columns) {
        return MYSQL.uniqueConstraint(name, columns);
      }

      @Override
      String addColumnClause(String table, String column, String ddlType) {
        return MYSQL.addColumnClause(table, column, ddlType);
      }
    };

    private final String id;
    private final String productName;

    DatabaseDialect(String id, String productName) {
      this.id = id;
      this.productName = productName;
    }

    String getId() {
      return id;
    }

    abstract String identityPrimaryKey(String name);

    abstract String varchar(int length);

    abstract String textType();

    abstract String tinyIntType();

    abstract String intType();

    abstract String bigIntType();

    abstract String doubleType();

    abstract String dateType();

    abstract String dateTimeType();

    abstract String currentTimestampFunction();

    abstract String onUpdateClause();

    abstract String uniqueConstraint(String name, String columns);

    abstract String addColumnClause(String table, String column, String ddlType);

    static DatabaseDialect resolve(String configured, DataSource dataSource) {
      if (configured != null && !configured.isBlank()) {
        DatabaseDialect fromConfig = fromId(configured.trim());
        if (fromConfig != UNKNOWN) {
          return fromConfig;
        }
      }
      return detect(dataSource);
    }

    static DatabaseDialect fromId(String id) {
      for (DatabaseDialect dialect : values()) {
        if (dialect.id.equalsIgnoreCase(id)) {
          return dialect;
        }
      }
      return UNKNOWN;
    }

    static DatabaseDialect detect(DataSource dataSource) {
      try (Connection conn = dataSource.getConnection()) {
        DatabaseMetaData meta = conn.getMetaData();
        String product = meta.getDatabaseProductName();
        for (DatabaseDialect dialect : values()) {
          if (dialect.productName != null && dialect.productName.equalsIgnoreCase(product)) {
            return dialect;
          }
        }
        log.warn("未能识别数据库产品: {}，将使用默认 MySQL 方言", product);
        return UNKNOWN;
      } catch (SQLException e) {
        log.warn("识别数据库方言失败: {}", e.getMessage());
        return UNKNOWN;
      }
    }
  }
}
