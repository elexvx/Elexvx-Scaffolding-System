# 数据库脚本

## 目录结构

- `database/demo/`
  - 表结构 + 演示数据脚本
- `database/schema/`
  - 仅表结构脚本（无演示数据）
- `database/comments/`
  - 合并后的注释脚本：`tdesign_comments.sql`

## 含演示数据脚本

- MySQL：`database/demo/tdesign_init.sql`
- PostgreSQL：`database/demo/tdesign_init_pg.sql`
- Oracle：`database/demo/tdesign_init_oracle.sql`
- SQL Server：`database/demo/tdesign_init_sqlserver.sql`

## 仅表结构脚本

- MySQL：`database/schema/tdesign_schema.sql`
- PostgreSQL：`database/schema/tdesign_schema_pg.sql`
- Oracle：`database/schema/tdesign_schema_oracle.sql`
- SQL Server：`database/schema/tdesign_schema_sqlserver.sql`

## 导入示例

```bash
# MySQL
mysql -u root -p tdesign < database/demo/tdesign_init.sql
mysql -u root -p tdesign < database/schema/tdesign_schema.sql

# PostgreSQL
psql -d tdesign -f database/demo/tdesign_init_pg.sql
psql -d tdesign -f database/schema/tdesign_schema_pg.sql

# SQL Server
sqlcmd -d tdesign -i database/demo/tdesign_init_sqlserver.sql
sqlcmd -d tdesign -i database/schema/tdesign_schema_sqlserver.sql
