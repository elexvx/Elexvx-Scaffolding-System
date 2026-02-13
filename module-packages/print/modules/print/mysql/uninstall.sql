DELETE FROM role_permissions WHERE permission IN (
'system:PrintCenter:query','system:PrintCenter:create','system:PrintCenter:update','system:PrintCenter:delete'
);
DELETE FROM role_menus WHERE menu_id IN (SELECT id FROM sys_menu_items WHERE route_name='PrintCenter');
DELETE FROM sys_menu_items WHERE route_name='PrintCenter';
DROP TABLE IF EXISTS print_job;
DROP TABLE IF EXISTS print_template_version;
DROP TABLE IF EXISTS print_template;
