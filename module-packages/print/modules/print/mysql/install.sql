CREATE TABLE IF NOT EXISTS print_template (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  biz_type VARCHAR(64) NOT NULL,
  name VARCHAR(128) NOT NULL,
  template_key VARCHAR(128) NULL,
  paper_size VARCHAR(32) NULL,
  orientation VARCHAR(16) NULL,
  schema_json LONGTEXT NULL,
  html LONGTEXT NULL,
  css LONGTEXT NULL,
  enabled TINYINT(1) DEFAULT 1,
  current_version INT DEFAULT 1,
  created_by BIGINT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by BIGINT NULL,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_print_template_key (template_key)
);

CREATE TABLE IF NOT EXISTS print_template_version (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  template_id BIGINT NOT NULL,
  version INT NOT NULL,
  schema_json LONGTEXT NULL,
  html LONGTEXT NULL,
  css LONGTEXT NULL,
  published TINYINT(1) DEFAULT 0,
  created_by BIGINT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_template_version (template_id, version)
);

CREATE TABLE IF NOT EXISTS print_job (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  biz_type VARCHAR(64) NULL,
  biz_id VARCHAR(64) NULL,
  template_id BIGINT NULL,
  template_version INT NULL,
  mode VARCHAR(16) NULL,
  status VARCHAR(16) NULL,
  file_url VARCHAR(1024) NULL,
  html_snapshot LONGTEXT NULL,
  meta_json TEXT NULL,
  created_by BIGINT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_print_job_biz (biz_type, biz_id)
);

INSERT INTO sys_menu_items (
  parent_id,node_type,path,route_name,component,redirect,title_zh_cn,title_en_us,icon,hidden,keep_alive,frame_src,frame_blank,enabled,required_modules,require_role,require_permission,order_no,version,actions
)
SELECT s.id,'PAGE','print','PrintCenter','IFRAME',NULL,'打印中心','Print Center','print',0,1,'/modules/print/index.html',0,1,'print',NULL,NULL,99,0,'query,create,update,delete'
FROM sys_menu_items s
WHERE s.route_name='system' AND NOT EXISTS (SELECT 1 FROM sys_menu_items m WHERE m.route_name='PrintCenter')
LIMIT 1;

SET @admin_role_id = (SELECT id FROM roles WHERE code='admin' LIMIT 1);
SET @admin_role_id = IFNULL(@admin_role_id,1);

INSERT IGNORE INTO role_menus(role_id, menu_id)
SELECT @admin_role_id, id FROM sys_menu_items WHERE route_name='PrintCenter';

INSERT IGNORE INTO role_permissions(role_id, permission) VALUES
(@admin_role_id, 'system:PrintCenter:query'),
(@admin_role_id, 'system:PrintCenter:create'),
(@admin_role_id, 'system:PrintCenter:update'),
(@admin_role_id, 'system:PrintCenter:delete');
