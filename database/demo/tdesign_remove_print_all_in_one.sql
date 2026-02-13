-- =============================================
-- tdesign_remove_print_all_in_one.sql
-- 作用：一键移除“打印功能”相关菜单、权限与数据表（幂等）
-- 说明：可直接下载后导入执行。
-- =============================================

START TRANSACTION;

-- 1) 清理菜单授权（先删关联，避免外键/脏关联）
DELETE rm
FROM role_menus rm
JOIN sys_menu_items sm ON sm.id = rm.menu_id
WHERE sm.route_name IN ('ConsolePrint', 'ExamplePrint');

-- 2) 清理权限点
DELETE FROM role_permissions
WHERE permission IN (
  'system:ConsolePrint:query',
  'system:ExamplePrint:query',
  'plugin:warehouse:print'
);

-- 3) 清理菜单
DELETE FROM sys_menu_items
WHERE route_name IN ('ConsolePrint', 'ExamplePrint');

-- 4) 清理打印模块业务表（模块库中的表）
DROP TABLE IF EXISTS print_template_version;
DROP TABLE IF EXISTS print_template;
DROP TABLE IF EXISTS print_job;

-- 5) 清理 core 侧打印表（若存在 core schema）
DROP TABLE IF EXISTS core.print_archive;
DROP TABLE IF EXISTS core.print_job;
DROP TABLE IF EXISTS core.print_definition;

COMMIT;
