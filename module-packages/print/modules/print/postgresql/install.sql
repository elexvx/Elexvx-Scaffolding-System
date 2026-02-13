CREATE TABLE IF NOT EXISTS print_template (
  id BIGSERIAL PRIMARY KEY,
  biz_type VARCHAR(64) NOT NULL,
  name VARCHAR(128) NOT NULL,
  template_key VARCHAR(128) UNIQUE,
  paper_size VARCHAR(32),
  orientation VARCHAR(16),
  schema_json TEXT,
  html TEXT,
  css TEXT,
  enabled SMALLINT DEFAULT 1,
  current_version INT DEFAULT 1,
  created_by BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_by BIGINT,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS print_template_version (
  id BIGSERIAL PRIMARY KEY,
  template_id BIGINT NOT NULL,
  version INT NOT NULL,
  schema_json TEXT,
  html TEXT,
  css TEXT,
  published SMALLINT DEFAULT 0,
  created_by BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(template_id, version)
);

CREATE TABLE IF NOT EXISTS print_job (
  id BIGSERIAL PRIMARY KEY,
  biz_type VARCHAR(64),
  biz_id VARCHAR(64),
  template_id BIGINT,
  template_version INT,
  mode VARCHAR(16),
  status VARCHAR(16),
  file_url VARCHAR(1024),
  html_snapshot TEXT,
  meta_json TEXT,
  created_by BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_print_job_biz ON print_job(biz_type, biz_id);

INSERT INTO sys_menu_items (
  parent_id,node_type,path,route_name,component,redirect,title_zh_cn,title_en_us,icon,hidden,keep_alive,frame_src,frame_blank,enabled,required_modules,require_role,require_permission,order_no,version,actions
)
SELECT s.id,'PAGE','print','PrintCenter','IFRAME',NULL,'打印中心','Print Center','print',0,1,'/modules/print/index.html',0,1,'print',NULL,NULL,99,0,'query,create,update,delete'
FROM sys_menu_items s
WHERE s.route_name='system' AND NOT EXISTS (SELECT 1 FROM sys_menu_items m WHERE m.route_name='PrintCenter')
LIMIT 1;

WITH ar AS (SELECT COALESCE((SELECT id FROM roles WHERE code='admin' LIMIT 1),1) AS role_id)
INSERT INTO role_menus(role_id, menu_id)
SELECT ar.role_id, m.id FROM ar, sys_menu_items m WHERE m.route_name='PrintCenter'
ON CONFLICT DO NOTHING;

WITH ar AS (SELECT COALESCE((SELECT id FROM roles WHERE code='admin' LIMIT 1),1) AS role_id)
INSERT INTO role_permissions(role_id, permission)
SELECT role_id, permission FROM ar, (VALUES
('system:PrintCenter:query'),('system:PrintCenter:create'),('system:PrintCenter:update'),('system:PrintCenter:delete')
) p(permission)
ON CONFLICT DO NOTHING;
