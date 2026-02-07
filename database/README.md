# Database scripts

## Directory layout

- `database/demo/`
  - schema + demo data scripts
- `database/schema/`
  - schema-only scripts (no demo data)
- `database/comments/`
  - merged comments script: `tdesign_comments.sql`

## Demo data scripts

- MySQL: `database/demo/tdesign_init.sql`
- PostgreSQL: `database/demo/tdesign_init_pg.sql`
- Oracle: `database/demo/tdesign_init_oracle.sql`
- SQL Server: `database/demo/tdesign_init_sqlserver.sql`

## Schema-only scripts

- MySQL: `database/schema/tdesign_schema.sql`
- PostgreSQL: `database/schema/tdesign_schema_pg.sql`
- Oracle: `database/schema/tdesign_schema_oracle.sql`
- SQL Server: `database/schema/tdesign_schema_sqlserver.sql`

## Import examples

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
```

```sql
-- Oracle
@database/demo/tdesign_init_oracle.sql
@database/schema/tdesign_schema_oracle.sql
```

## Notes

- Module tables `verification_sms_settings` and `verification_email_settings` are not in base init scripts and are created by module install SQL in `backend/src/main/resources/modules/**/install.sql`.
- Legacy mixed dictionary table `sys_dict_items` has been removed from base init scripts.
- `database/comments/tdesign_comments.sql` contains Oracle/PostgreSQL/SQL Server sections in one file; run only the section for your DB.
