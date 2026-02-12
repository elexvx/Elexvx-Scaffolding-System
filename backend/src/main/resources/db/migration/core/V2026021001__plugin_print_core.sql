CREATE SCHEMA IF NOT EXISTS core;

CREATE TABLE IF NOT EXISTS core.plugin_package (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  plugin_id VARCHAR(100) NOT NULL,
  version VARCHAR(50) NOT NULL,
  vendor VARCHAR(128),
  package_hash CHAR(64) NOT NULL,
  uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_plugin_pkg (plugin_id, version)
);

CREATE TABLE IF NOT EXISTS core.plugin_state (
  plugin_id VARCHAR(100) PRIMARY KEY,
  state VARCHAR(30) NOT NULL,
  last_error TEXT,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS core.plugin_install_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  plugin_id VARCHAR(100) NOT NULL,
  trace_id VARCHAR(64) NOT NULL,
  step_name VARCHAR(80) NOT NULL,
  status VARCHAR(20) NOT NULL,
  detail TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS core.plugin_frontend_registry (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  plugin_id VARCHAR(100) NOT NULL,
  entry_url VARCHAR(255) NOT NULL,
  exposed_module VARCHAR(120) NOT NULL,
  route_path VARCHAR(120) NOT NULL,
  enabled TINYINT(1) NOT NULL DEFAULT 0,
  UNIQUE KEY uk_plugin_route (plugin_id, route_path)
);

CREATE TABLE IF NOT EXISTS core.print_definition (
  definition_id VARCHAR(120) PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  template_type VARCHAR(20) NOT NULL,
  template_id VARCHAR(120) NOT NULL,
  permission VARCHAR(120) NOT NULL,
  plugin_id VARCHAR(100),
  enabled TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS core.print_job (
  job_id VARCHAR(64) PRIMARY KEY,
  definition_id VARCHAR(120) NOT NULL,
  business_ref VARCHAR(120),
  mode VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL,
  operator VARCHAR(80) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS core.print_archive (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  job_id VARCHAR(64) NOT NULL,
  file_url VARCHAR(512),
  sha256 CHAR(64) NOT NULL,
  template_version VARCHAR(50) NOT NULL,
  data_snapshot_hash CHAR(64) NOT NULL,
  printed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_print_archive_job(job_id)
);
