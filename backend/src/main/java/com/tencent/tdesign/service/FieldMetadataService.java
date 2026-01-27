package com.tencent.tdesign.service;

import com.tencent.tdesign.dto.FieldColumnRequest;
import com.tencent.tdesign.vo.DatabaseMetadataResponse;
import com.tencent.tdesign.vo.FieldMetadataResponse;
import com.tencent.tdesign.vo.TableMetadataResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class FieldMetadataService {
  private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[A-Za-z0-9_]+$");
  private static final Pattern LENGTH_PATTERN = Pattern.compile("^\\d+(,\\d+)?$");
  private static final Pattern NUMBER_PATTERN = Pattern.compile("^-?\\d+(\\.\\d+)?$");
  private static final Set<String> ALLOWED_TYPES =
    Set.of("varchar", "int", "integer", "bigint", "decimal", "datetime", "text", "date", "timestamp", "float", "double", "boolean", "tinyint");

  private final JdbcTemplate jdbc;

  public FieldMetadataService(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public DatabaseMetadataResponse loadMetadata() {
    String database = currentDatabase();
    DatabaseMetadataResponse response = new DatabaseMetadataResponse();
    response.setName(database);
    List<Map<String, Object>> tables = jdbc.queryForList(
      "SELECT TABLE_NAME, TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ? ORDER BY TABLE_NAME",
      database
    );
    List<TableMetadataResponse> tableResponses = new ArrayList<>();
    for (Map<String, Object> tableRow : tables) {
      String tableName = String.valueOf(tableRow.get("TABLE_NAME"));
      String comment = tableRow.get("TABLE_COMMENT") == null ? "" : String.valueOf(tableRow.get("TABLE_COMMENT"));
      TableMetadataResponse table = new TableMetadataResponse();
      table.setName(tableName);
      table.setComment(comment);
      table.setColumns(loadColumns(database, tableName));
      tableResponses.add(table);
    }
    response.setTables(tableResponses);
    return response;
  }

  public void updateColumn(FieldColumnRequest request) {
    String database = currentDatabase();
    String targetDatabase = normalizeIdentifier(request.getDatabase(), "数据库标识非法");
    if (!database.equalsIgnoreCase(targetDatabase)) {
      throw new IllegalArgumentException("数据库标识不匹配");
    }
    String table = normalizeIdentifier(request.getTable(), "表标识非法");
    String name = normalizeIdentifier(request.getName(), "字段名非法");
    String type = normalizeType(request.getType());
    String length = normalizeLength(request.getLength());

    boolean required = Boolean.TRUE.equals(request.getRequired());
    String columnSql = buildColumnDefinition(type, length, required, request.getDefaultValue(), request.getDescription());

    jdbc.execute("ALTER TABLE `" + table + "` MODIFY COLUMN `" + name + "` " + columnSql);

    if (request.getPrimaryKey() != null) {
      updatePrimaryKey(table, name, request.getPrimaryKey());
    }
  }

  private String currentDatabase() {
    String database = jdbc.queryForObject("SELECT DATABASE()", String.class);
    if (database == null || database.isBlank()) {
      throw new IllegalStateException("无法获取当前数据库名称");
    }
    return database;
  }

  private List<FieldMetadataResponse> loadColumns(String database, String tableName) {
    List<Map<String, Object>> columns = jdbc.queryForList(
      "SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, COLUMN_DEFAULT, COLUMN_COMMENT, IS_NULLABLE, COLUMN_KEY " +
        "FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? ORDER BY ORDINAL_POSITION",
      database,
      tableName
    );
    List<FieldMetadataResponse> response = new ArrayList<>();
    for (Map<String, Object> columnRow : columns) {
      FieldMetadataResponse column = new FieldMetadataResponse();
      column.setName(String.valueOf(columnRow.get("COLUMN_NAME")));
      column.setType(String.valueOf(columnRow.get("DATA_TYPE")));
      Object length = columnRow.get("CHARACTER_MAXIMUM_LENGTH");
      column.setLength(length == null ? "" : String.valueOf(length));
      Object defaultValue = columnRow.get("COLUMN_DEFAULT");
      column.setDefaultValue(defaultValue == null ? "" : String.valueOf(defaultValue));
      Object comment = columnRow.get("COLUMN_COMMENT");
      column.setDescription(comment == null ? "" : String.valueOf(comment));
      Object nullable = columnRow.get("IS_NULLABLE");
      column.setRequired("NO".equalsIgnoreCase(nullable == null ? "" : String.valueOf(nullable)));
      Object key = columnRow.get("COLUMN_KEY");
      column.setPrimaryKey("PRI".equalsIgnoreCase(key == null ? "" : String.valueOf(key)));
      response.add(column);
    }
    return response;
  }

  private String normalizeIdentifier(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(message);
    }
    String trimmed = value.trim();
    if (!IDENTIFIER_PATTERN.matcher(trimmed).matches()) {
      throw new IllegalArgumentException(message);
    }
    return trimmed;
  }

  private String normalizeType(String type) {
    if (type == null || type.isBlank()) {
      throw new IllegalArgumentException("字段类型不能为空");
    }
    String normalized = type.trim().toLowerCase(Locale.ROOT);
    if (!ALLOWED_TYPES.contains(normalized)) {
      throw new IllegalArgumentException("不支持的字段类型: " + type);
    }
    return normalized;
  }

  private String normalizeLength(String length) {
    if (length == null || length.isBlank()) {
      return "";
    }
    String trimmed = length.trim();
    if (!LENGTH_PATTERN.matcher(trimmed).matches()) {
      throw new IllegalArgumentException("字段长度格式非法");
    }
    return trimmed;
  }

  private String buildColumnDefinition(String type, String length, boolean required, String defaultValue, String description) {
    StringBuilder builder = new StringBuilder();
    builder.append(type);
    if (!length.isBlank() && allowLength(type)) {
      builder.append("(").append(length).append(")");
    }
    builder.append(required ? " NOT NULL" : " NULL");
    if (defaultValue != null && !defaultValue.isBlank()) {
      builder.append(" DEFAULT ").append(formatDefaultValue(type, defaultValue.trim()));
    }
    if (description != null) {
      builder.append(" COMMENT '").append(escapeSql(description)).append("'");
    }
    return builder.toString();
  }

  private boolean allowLength(String type) {
    return !Set.of("text", "date", "datetime", "timestamp").contains(type);
  }

  private String formatDefaultValue(String type, String value) {
    String upper = value.toUpperCase(Locale.ROOT);
    if ("CURRENT_TIMESTAMP".equals(upper)) {
      return upper;
    }
    if (Set.of("int", "integer", "bigint", "decimal", "float", "double", "tinyint").contains(type)) {
      if (NUMBER_PATTERN.matcher(value).matches()) {
        return value;
      }
    }
    if ("boolean".equals(type)) {
      if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
        return value.toLowerCase(Locale.ROOT);
      }
      if ("0".equals(value) || "1".equals(value)) {
        return value;
      }
    }
    return "'" + escapeSql(value) + "'";
  }

  private String escapeSql(String value) {
    return value.replace("'", "''");
  }

  private void updatePrimaryKey(String table, String name, boolean isPrimaryKey) {
    List<String> pkColumns = jdbc.queryForList(
      "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA = DATABASE() " +
        "AND TABLE_NAME = ? AND CONSTRAINT_NAME = 'PRIMARY' ORDER BY ORDINAL_POSITION",
      String.class,
      table
    );
    boolean currentlyPrimary = pkColumns.stream().anyMatch(col -> col.equalsIgnoreCase(name));
    if (isPrimaryKey && !currentlyPrimary) {
      if (!pkColumns.isEmpty()) {
        throw new IllegalArgumentException("当前表已存在主键，请先移除现有主键");
      }
      jdbc.execute("ALTER TABLE `" + table + "` ADD PRIMARY KEY (`" + name + "`)");
    }
    if (!isPrimaryKey && currentlyPrimary) {
      if (pkColumns.size() > 1) {
        throw new IllegalArgumentException("当前表为组合主键，暂不支持直接移除");
      }
      jdbc.execute("ALTER TABLE `" + table + "` DROP PRIMARY KEY");
    }
  }
}
