-- SQL Server initialization script for TDesign
-- Use: sqlcmd -d tdesign -i database/demo/tdesign_init_sqlserver.sql
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
  gender NVARCHAR(10),
  nickname NVARCHAR(64),
  province_id INT,
  province NVARCHAR(100),
  city_id INT,
  city NVARCHAR(100),
  district_id INT,
  district NVARCHAR(100),
  town_id INT,
  town NVARCHAR(100),
  street_id INT,
  street NVARCHAR(100),
  zip_code NVARCHAR(12),
  address NVARCHAR(255),
  introduction NVARCHAR(MAX),
  avatar NVARCHAR(255),
  tags NVARCHAR(255),
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
  updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE areas (
  id INT NOT NULL PRIMARY KEY,
  parent_id INT NOT NULL DEFAULT 0,
  name NVARCHAR(120) NOT NULL DEFAULT '',
  zip_code NVARCHAR(12),
  level TINYINT NOT NULL DEFAULT 1
);
GO

CREATE INDEX idx_areas_parent ON areas (parent_id);
GO
CREATE INDEX idx_areas_level ON areas (level);
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

CREATE TABLE sys_dict (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  name NVARCHAR(64) NOT NULL,
  code NVARCHAR(64) NOT NULL UNIQUE,
  status TINYINT NOT NULL DEFAULT 1,
  sort INT NOT NULL DEFAULT 0,
  remark NVARCHAR(255),
  created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
  updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME()
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

CREATE TABLE module_registry (
  module_key NVARCHAR(64) NOT NULL PRIMARY KEY,
  name NVARCHAR(128),
  version NVARCHAR(32),
  enabled TINYINT DEFAULT 1,
  install_state NVARCHAR(32),
  installed_at DATETIME2
);
GO

CREATE TABLE security_token_settings (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  session_timeout_minutes INT,
  token_timeout_minutes INT,
  token_refresh_grace_minutes INT,
  allow_url_token_param TINYINT NOT NULL DEFAULT 0
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

-- ----------------------------
-- Records of sys_dict
-- ----------------------------
SET IDENTITY_INSERT sys_dict ON;
INSERT INTO sys_dict (id, name, code, status, sort, remark, created_at, updated_at) VALUES
  (2001, N'性别', N'gender', 1, 1, N'性别选项', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2002, N'地址-省', N'address_province', 1, 2, N'地址省份', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2003, N'地址-市', N'address_city', 1, 3, N'地址城市', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2004, N'地址-区', N'address_district', 1, 4, N'地址区县', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2005, N'公告-类型', N'announcement_type', 1, 5, N'公告类型', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2006, N'公告-优先级', N'announcement_priority', 1, 6, N'公告优先级', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2007, N'公告-状态', N'announcement_status', 1, 7, N'公告状态', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2008, N'通知-类型', N'notification_type', 1, 8, N'通知类型', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2009, N'通知-优先级', N'notification_priority', 1, 9, N'通知优先级', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2010, N'通知-状态', N'notification_status', 1, 10, N'通知状态', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2011, N'消息-优先级', N'message_quality', 1, 11, N'消息优先级', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2012, N'日志-操作类型', N'log_action', 1, 12, N'操作日志类型', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2013, N'用户-状态', N'user_status', 1, 13, N'用户状态', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2014, N'机构-类型', N'org_type', 1, 14, N'机构类型', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2015, N'机构-状态', N'org_status', 1, 15, N'机构状态', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2016, N'菜单-节点类型', N'menu_node_type', 1, 16, N'菜单节点类型', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2017, N'菜单-权限动作', N'menu_action', 1, 17, N'菜单权限动作', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2018, N'存储-提供商', N'storage_provider', 1, 18, N'存储提供商', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2019, N'短信-通道', N'sms_provider', 1, 19, N'短信通道', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2020, N'AI-厂商', N'ai_vendor', 1, 20, N'AI厂商', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2021, N'团队', N'team', 1, 21, N'团队管理', '2026-01-18 21:21:03', '2026-01-18 21:21:03');
SET IDENTITY_INSERT sys_dict OFF;
GO

-- ----------------------------
-- ----------------------------
GO

-- Demo users/roles
IF NOT EXISTS (SELECT 1 FROM roles WHERE id = 1)
BEGIN
  SET IDENTITY_INSERT roles ON;
  INSERT INTO roles (id, name, description) VALUES (1, N'admin', N'管理员 - 拥有所有权限');
  SET IDENTITY_INSERT roles OFF;
END;
GO

IF NOT EXISTS (SELECT 1 FROM users WHERE id = 1)
BEGIN
  SET IDENTITY_INSERT users ON;
  INSERT INTO users (id, account, guid, name, password_hash, status, created_at, updated_at)
  VALUES (
    1,
    N'admin',
    N'e59c3cd1-3b52-47c7-bf88-fad5b2281827',
    N'管理员',
    N'$2a$10$BbVSQCIChdR.4gfwiG1OduJiKE/KpUTbhBXd.7Sr.uwi8eggDpYeu',
    1,
    SYSDATETIME(),
    SYSDATETIME()
  );
  SET IDENTITY_INSERT users OFF;
END;
GO

IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = 1 AND role = N'admin')
BEGIN
  INSERT INTO user_roles (user_id, role) VALUES (1, N'admin');
END;
GO

INSERT INTO module_registry (module_key, name, version, enabled, install_state, installed_at) VALUES ('sms', N'短信验证', '1.0.0', 1, 'PENDING', NULL);
INSERT INTO module_registry (module_key, name, version, enabled, install_state, installed_at) VALUES ('email', N'邮箱验证', '1.0.0', 1, 'PENDING', NULL);
GO


