# 数据库脚本说明

## 目录结构

- `database/demo/`
  - 建表 + 演示数据脚本
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
# MySQL（建议指定 utf8mb4）
mysql --default-character-set=utf8mb4 -u root -p tdesign < database/demo/tdesign_init.sql
mysql --default-character-set=utf8mb4 -u root -p tdesign < database/schema/tdesign_schema.sql

# PostgreSQL
psql -d tdesign -f database/demo/tdesign_init_pg.sql
psql -d tdesign -f database/schema/tdesign_schema_pg.sql

# SQL Server
sqlcmd -d tdesign -i database/demo/tdesign_init_sqlserver.sql
sqlcmd -d tdesign -i database/schema/tdesign_schema_sqlserver.sql
```

## MySQL 编码注意事项（重要）

1. SQL 文件均为 UTF-8（无 BOM）保存。
2. 导入时请使用 `utf8mb4` 客户端字符集（如上 `--default-character-set=utf8mb4`）。
3. JDBC URL 建议使用以下组合，避免驱动报 `Unsupported character encoding 'utf8mb4'`：
   - `useUnicode=true`
   - `characterEncoding=UTF-8`
   - `connectionCollation=utf8mb4_0900_ai_ci`

示例：

```text
jdbc:mysql://localhost:3306/tdesign?createDatabaseIfNotExist=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_0900_ai_ci&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
```
