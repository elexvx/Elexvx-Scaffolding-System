# Database initialization scripts

## MySQL

Use the existing MySQL script:

```bash
mysql -u root -p tdesign < database/tdesign_init.sql
```

## PostgreSQL

```bash
psql -d tdesign -f database/tdesign_init_pg.sql
```

## Oracle

```sql
@database/tdesign_init_oracle.sql
```

> Run on an empty schema or drop existing tables first.

## SQL Server

```bash
sqlcmd -d tdesign -i database/tdesign_init_sqlserver.sql
```

> Run on an empty schema or drop existing tables first.
