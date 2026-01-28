-- SQL Server initialization script for TDesign
-- Use: sqlcmd -d tdesign -i database/tdesign_init_sqlserver.sql
-- Note: run on an empty schema or drop existing tables first.

CREATE TABLE users (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  account NVARCHAR(64) NOT NULL UNIQUE,
  guid NVARCHAR(36) NOT NULL UNIQUE,
  name NVARCHAR(64) NOT NULL,
  password_hash NVARCHAR(100) NOT NULL,
  mobile NVARCHAR(20),
  phone NVARCHAR(20),
  email NVARCHAR(100),
  id_card NVARCHAR(32),
  seat NVARCHAR(50),
  entity NVARCHAR(100),
  leader NVARCHAR(64),
  position NVARCHAR(64),
  join_day DATE,
  team NVARCHAR(255),
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
  updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE org_units (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  parent_id BIGINT,
  name NVARCHAR(128) NOT NULL,
  short_name NVARCHAR(64),
  type NVARCHAR(32) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  phone NVARCHAR(32),
  email NVARCHAR(128),
  created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
  updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE org_unit_leaders (
  org_unit_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  PRIMARY KEY (org_unit_id, user_id)
);
GO

CREATE TABLE user_org_units (
  user_id BIGINT NOT NULL,
  org_unit_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, org_unit_id)
);
GO

CREATE TABLE roles (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  name NVARCHAR(64) NOT NULL UNIQUE,
  description NVARCHAR(255)
);
GO

CREATE TABLE user_roles (
  user_id BIGINT NOT NULL,
  role NVARCHAR(64) NOT NULL,
  PRIMARY KEY (user_id, role)
);
GO

CREATE TABLE role_permissions (
  role_id BIGINT NOT NULL,
  permission NVARCHAR(128) NOT NULL,
  PRIMARY KEY (role_id, permission)
);
GO

CREATE TABLE sys_menu_items (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  parent_id BIGINT,
  node_type NVARCHAR(16) NOT NULL,
  path NVARCHAR(255) NOT NULL,
  route_name NVARCHAR(128) NOT NULL,
  component NVARCHAR(255),
  redirect NVARCHAR(255),
  title_zh_cn NVARCHAR(64) NOT NULL,
  title_en_us NVARCHAR(64),
  icon NVARCHAR(64),
  hidden TINYINT NOT NULL DEFAULT 0,
  frame_src NVARCHAR(512),
  frame_blank TINYINT NOT NULL DEFAULT 0,
  enabled TINYINT NOT NULL DEFAULT 1,
  order_no INT NOT NULL DEFAULT 0,
  actions NVARCHAR(128),
  version INT NOT NULL DEFAULT 0,
  created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
  updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
  CONSTRAINT uk_sys_menu_items_route_name UNIQUE (route_name)
);
GO

CREATE TABLE role_menus (
  role_id BIGINT NOT NULL,
  menu_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, menu_id)
);
GO

CREATE TABLE ui_brand_settings (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  website_name NVARCHAR(100),
  app_version NVARCHAR(50),
  logo_expanded_url NVARCHAR(255),
  logo_collapsed_url NVARCHAR(255),
  favicon_url NVARCHAR(255),
  qr_code_url NVARCHAR(255)
);
GO

CREATE TABLE ui_layout_settings (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  default_home NVARCHAR(255),
  show_footer TINYINT,
  is_sidebar_compact TINYINT,
  show_breadcrumb TINYINT,
  menu_auto_collapsed TINYINT,
  layout NVARCHAR(20),
  split_menu TINYINT,
  side_mode NVARCHAR(20),
  is_footer_aside TINYINT,
  is_sidebar_fixed TINYINT,
  is_header_fixed TINYINT,
  is_use_tabs_router TINYINT,
  show_header TINYINT
);
GO

CREATE TABLE ui_theme_settings (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  auto_theme TINYINT,
  light_start_time NVARCHAR(10),
  dark_start_time NVARCHAR(10),
  mode NVARCHAR(20),
  brand_theme NVARCHAR(20)
);
GO

CREATE TABLE ui_footer_settings (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  footer_company NVARCHAR(100),
  footer_icp NVARCHAR(100),
  copyright_start_year NVARCHAR(10)
);
GO

CREATE TABLE ui_login_settings (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  login_bg_url NVARCHAR(255),
  allow_multi_device_login TINYINT
);
GO

CREATE TABLE ui_legal_settings (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  user_agreement NVARCHAR(MAX),
  privacy_agreement NVARCHAR(MAX)
);
GO

CREATE TABLE ui_system_settings (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  log_retention_days INT,
  ai_assistant_enabled TINYINT
);
GO

CREATE TABLE verification_sms_settings (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  sms_enabled TINYINT,
  sms_provider NVARCHAR(32),
  sms_aliyun_enabled TINYINT,
  sms_aliyun_access_key_id NVARCHAR(256),
  sms_aliyun_access_key_secret NVARCHAR(256),
  sms_aliyun_sign_name NVARCHAR(128),
  sms_aliyun_template_code NVARCHAR(64),
  sms_aliyun_region_id NVARCHAR(64),
  sms_aliyun_endpoint NVARCHAR(255),
  sms_tencent_enabled TINYINT,
  sms_tencent_secret_id NVARCHAR(256),
  sms_tencent_secret_key NVARCHAR(256),
  sms_tencent_sign_name NVARCHAR(128),
  sms_tencent_template_id NVARCHAR(64),
  sms_tencent_region NVARCHAR(64),
  sms_tencent_endpoint NVARCHAR(255),
  sms_sdk_app_id NVARCHAR(64)
);
GO

CREATE TABLE verification_email_settings (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  email_enabled TINYINT,
  email_host NVARCHAR(255),
  email_port INT,
  email_username NVARCHAR(128),
  email_password NVARCHAR(256),
  email_from NVARCHAR(128),
  email_ssl TINYINT
);
GO

CREATE TABLE security_token_settings (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  session_timeout_minutes INT,
  token_timeout_minutes INT,
  token_refresh_grace_minutes INT
);
GO

CREATE TABLE security_captcha_settings (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  captcha_enabled TINYINT,
  captcha_type NVARCHAR(20),
  drag_captcha_width INT,
  drag_captcha_height INT,
  drag_captcha_threshold INT,
  image_captcha_length INT,
  image_captcha_noise_lines INT
);
GO

CREATE TABLE security_password_policy (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  password_min_length INT,
  password_require_uppercase TINYINT,
  password_require_lowercase TINYINT,
  password_require_special TINYINT,
  password_allow_sequential TINYINT
);
GO

CREATE TABLE operation_logs (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  action NVARCHAR(32) NOT NULL,
  module NVARCHAR(64),
  detail NVARCHAR(512),
  user_id BIGINT,
  user_guid NVARCHAR(36),
  account NVARCHAR(64),
  ip_address NVARCHAR(64),
  device_model NVARCHAR(128),
  os NVARCHAR(64),
  browser NVARCHAR(64),
  created_at DATETIME2
);
GO

CREATE TABLE user_parameters (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  user_id BIGINT NOT NULL,
  param_key NVARCHAR(100) NOT NULL,
  param_value NVARCHAR(MAX) NOT NULL,
  description NVARCHAR(255),
  created_at DATETIME2,
  updated_at DATETIME2
);
GO

CREATE TABLE storage_settings (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  provider NVARCHAR(20) NOT NULL,
  bucket NVARCHAR(128),
  region NVARCHAR(128),
  endpoint NVARCHAR(255),
  access_key NVARCHAR(128),
  secret_key NVARCHAR(128),
  custom_domain NVARCHAR(255),
  path_prefix NVARCHAR(128),
  public_read TINYINT
);
GO

CREATE TABLE announcements (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  title NVARCHAR(200) NOT NULL,
  summary NVARCHAR(200),
  content NVARCHAR(MAX) NOT NULL,
  type NVARCHAR(32) NOT NULL,
  priority NVARCHAR(16) NOT NULL,
  status NVARCHAR(16) NOT NULL,
  cover_url NVARCHAR(255),
  attachment_url NVARCHAR(255),
  attachment_name NVARCHAR(255),
  publish_at DATETIME2,
  created_at DATETIME2,
  updated_at DATETIME2,
  created_by_id BIGINT,
  created_by_name NVARCHAR(64),
  is_broadcasted TINYINT NOT NULL DEFAULT 0
);
GO

CREATE TABLE notifications (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  title NVARCHAR(200) NOT NULL,
  summary NVARCHAR(200),
  content NVARCHAR(MAX) NOT NULL,
  priority NVARCHAR(16) NOT NULL,
  status NVARCHAR(16) NOT NULL,
  type NVARCHAR(32),
  cover_url NVARCHAR(512),
  attachment_url NVARCHAR(512),
  attachment_name NVARCHAR(255),
  publish_at DATETIME2,
  created_at DATETIME2,
  updated_at DATETIME2,
  created_by_id BIGINT,
  created_by_name NVARCHAR(64)
);
GO

CREATE TABLE ai_provider_settings (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  name NVARCHAR(64) NOT NULL,
  vendor NVARCHAR(32) NOT NULL,
  base_url NVARCHAR(255) NOT NULL,
  endpoint_path NVARCHAR(255),
  model NVARCHAR(128),
  api_key NVARCHAR(512),
  api_version NVARCHAR(64),
  temperature FLOAT,
  max_tokens INT,
  is_default TINYINT,
  enabled TINYINT,
  extra_headers NVARCHAR(2000),
  remark NVARCHAR(512),
  last_test_status NVARCHAR(32),
  last_test_message NVARCHAR(512),
  last_tested_at DATETIME2,
  created_at DATETIME2,
  updated_at DATETIME2
);
GO

CREATE TABLE sensitive_words (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  word NVARCHAR(200) NOT NULL UNIQUE,
  enabled TINYINT NOT NULL,
  created_at DATETIME2,
  updated_at DATETIME2
);
GO

CREATE TABLE sensitive_page_settings (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  page_key NVARCHAR(255) NOT NULL UNIQUE,
  page_name NVARCHAR(255),
  enabled TINYINT NOT NULL,
  created_at DATETIME2,
  updated_at DATETIME2
);
GO

CREATE TABLE sensitive_settings (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  enabled TINYINT NOT NULL,
  updated_at DATETIME2
);
GO
