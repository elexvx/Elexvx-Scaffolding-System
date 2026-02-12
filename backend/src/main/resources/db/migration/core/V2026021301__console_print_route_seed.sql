-- Seed console print route data for existing databases.
-- This migration is idempotent.

INSERT INTO sys_menu_items (
  parent_id,
  node_type,
  path,
  route_name,
  component,
  redirect,
  title_zh_cn,
  title_en_us,
  icon,
  hidden,
  keep_alive,
  frame_src,
  frame_blank,
  enabled,
  required_modules,
  require_role,
  require_permission,
  order_no,
  version,
  actions
)
SELECT
  c.id,
  'PAGE',
  'print',
  'ConsolePrint',
  '/console/print/index',
  NULL,
  'Print Center',
  'Print Center',
  'print',
  0,
  1,
  NULL,
  0,
  1,
  NULL,
  NULL,
  NULL,
  1,
  0,
  'query'
FROM sys_menu_items c
WHERE c.route_name = 'console'
  AND NOT EXISTS (
    SELECT 1
    FROM sys_menu_items s
    WHERE s.route_name = 'ConsolePrint'
  );

INSERT INTO role_menus (role_id, menu_id)
SELECT 1, s.id
FROM sys_menu_items s
WHERE s.route_name = 'ConsolePrint'
  AND NOT EXISTS (
    SELECT 1
    FROM role_menus rm
    WHERE rm.role_id = 1
      AND rm.menu_id = s.id
  );

INSERT INTO role_permissions (role_id, permission)
SELECT 1, 'system:ConsolePrint:query'
WHERE NOT EXISTS (
  SELECT 1
  FROM role_permissions rp
  WHERE rp.role_id = 1
    AND rp.permission = 'system:ConsolePrint:query'
);
