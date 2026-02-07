package com.tencent.tdesign.service;

import com.tencent.tdesign.dto.DictionaryCreateRequest;
import com.tencent.tdesign.dto.DictionaryItemCreateRequest;
import com.tencent.tdesign.dto.DictionaryItemUpdateRequest;
import com.tencent.tdesign.dto.DictionaryUpdateRequest;
import com.tencent.tdesign.entity.SysDict;
import com.tencent.tdesign.entity.SysDictItem;
import com.tencent.tdesign.mapper.SysDictMapper;
import com.tencent.tdesign.util.PermissionUtil;
import com.tencent.tdesign.vo.DictionaryImportResult;
import com.tencent.tdesign.vo.PageResult;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DictionaryService {
  private static final int MAX_IMPORT_ERRORS = 50;
  private static final Pattern CODE_PATTERN = Pattern.compile("^[a-z0-9_]+$");

  private final SysDictMapper dictMapper;
  private final DictionaryItemTableService itemTableService;
  private final OperationLogService operationLogService;

  public DictionaryService(
    SysDictMapper dictMapper,
    DictionaryItemTableService itemTableService,
    OperationLogService operationLogService
  ) {
    this.dictMapper = dictMapper;
    this.itemTableService = itemTableService;
    this.operationLogService = operationLogService;
  }

  public PageResult<SysDict> page(String keyword, Integer status, int page, int size) {
    PermissionUtil.check("system:SystemDict:query");
    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 1), 200);
    int offset = safePage * safeSize;
    String kw = StringUtils.hasText(keyword) ? keyword.trim() : null;
    List<SysDict> rows = dictMapper.selectPage(kw, status, offset, safeSize);
    long total = dictMapper.countByKeyword(kw, status);
    return new PageResult<>(rows, total);
  }

  public SysDict get(long id) {
    PermissionUtil.check("system:SystemDict:query");
    SysDict dict = dictMapper.selectById(id);
    if (dict == null) {
      throw new IllegalArgumentException("Dictionary not found.");
    }
    return dict;
  }

  @Transactional
  public SysDict create(DictionaryCreateRequest req) {
    PermissionUtil.check("system:SystemDict:create");
    String code = normalizeCode(req.getCode());
    if (dictMapper.selectByCode(code) != null) {
      throw new IllegalArgumentException("Dictionary code already exists.");
    }

    SysDict dict = new SysDict();
    dict.setName(req.getName().trim());
    dict.setCode(code);
    dict.setStatus(req.getStatus() == null ? 1 : req.getStatus());
    dict.setSort(req.getSort() == null ? 0 : req.getSort());
    dict.setRemark(req.getRemark());
    dict.setCreatedAt(LocalDateTime.now());
    dict.setUpdatedAt(LocalDateTime.now());
    dictMapper.insert(dict);

    itemTableService.ensureDictTable(dict);
    operationLogService.log("CREATE", "Dictionary", "Create dictionary: " + dict.getName());
    return dict;
  }

  @Transactional
  public SysDict update(long id, DictionaryUpdateRequest req) {
    PermissionUtil.check("system:SystemDict:update");
    SysDict dict = dictMapper.selectById(id);
    if (dict == null) {
      throw new IllegalArgumentException("Dictionary not found.");
    }

    String previousCode = dict.getCode();
    if (req.getName() != null) {
      dict.setName(req.getName().trim());
    }

    if (req.getCode() != null) {
      String code = normalizeCode(req.getCode());
      SysDict exists = dictMapper.selectByCode(code);
      if (exists != null && !Objects.equals(exists.getId(), id)) {
        throw new IllegalArgumentException("Dictionary code already exists.");
      }
      if (!code.equals(previousCode)) {
        itemTableService.renameDictTable(dict, code);
      }
      dict.setCode(code);
    }

    if (req.getStatus() != null) {
      dict.setStatus(req.getStatus());
    }
    if (req.getSort() != null) {
      dict.setSort(req.getSort());
    }
    if (req.getRemark() != null) {
      dict.setRemark(req.getRemark());
    }

    dict.setUpdatedAt(LocalDateTime.now());
    dictMapper.update(dict);
    operationLogService.log("UPDATE", "Dictionary", "Update dictionary: " + dict.getName());
    return dict;
  }

  @Transactional
  public boolean delete(long id) {
    PermissionUtil.check("system:SystemDict:delete");
    SysDict dict = dictMapper.selectById(id);
    if (dict == null) {
      return true;
    }

    itemTableService.dropDictTable(dict.getCode());
    itemTableService.deleteLegacyByDictId(id);
    dictMapper.deleteById(id);
    operationLogService.log("DELETE", "Dictionary", "Delete dictionary: " + dict.getName());
    return true;
  }

  public PageResult<SysDictItem> pageItems(long dictId, String keyword, Integer status, int page, int size) {
    PermissionUtil.check("system:SystemDict:query");
    SysDict dict = dictMapper.selectById(dictId);
    if (dict == null) {
      throw new IllegalArgumentException("Dictionary not found.");
    }
    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 1), 200);
    int offset = safePage * safeSize;
    String kw = StringUtils.hasText(keyword) ? keyword.trim() : null;

    List<SysDictItem> rows = itemTableService.selectPage(dict, kw, status, offset, safeSize);
    long total = itemTableService.countByDict(dict, kw, status);
    return new PageResult<>(rows, total);
  }

  public List<SysDictItem> listItemsByCode(String code) {
    PermissionUtil.check("system:SystemDict:query");
    if (!StringUtils.hasText(code)) {
      return List.of();
    }

    SysDict dict = dictMapper.selectByCode(code.trim().toLowerCase(Locale.ROOT));
    if (dict == null || !Integer.valueOf(1).equals(dict.getStatus())) {
      return List.of();
    }
    return itemTableService.selectEnabledByDict(dict);
  }

  @Transactional
  public SysDictItem createItem(long dictId, DictionaryItemCreateRequest req) {
    PermissionUtil.check("system:SystemDict:create");
    SysDict dict = dictMapper.selectById(dictId);
    if (dict == null) {
      throw new IllegalArgumentException("Dictionary not found.");
    }

    String value = req.getValue().trim();
    SysDictItem existing = itemTableService.selectByDictValue(dict, value);
    if (existing != null) {
      throw new IllegalArgumentException("Dictionary value already exists.");
    }

    SysDictItem item = new SysDictItem();
    item.setDictId(dictId);
    item.setLabel(req.getLabel().trim());
    item.setValue(value);
    item.setValueType(normalizeValueType(req.getValueType()));
    item.setStatus(req.getStatus() == null ? 1 : req.getStatus());
    item.setSort(req.getSort() == null ? 0 : req.getSort());
    item.setTagColor(req.getTagColor());
    item.setCreatedAt(LocalDateTime.now());
    item.setUpdatedAt(LocalDateTime.now());
    itemTableService.insert(dict, item);

    operationLogService.log("CREATE", "Dictionary", "Create dictionary item: " + dict.getName() + " - " + item.getLabel());
    return item;
  }

  @Transactional
  public SysDictItem updateItem(long id, DictionaryItemUpdateRequest req) {
    PermissionUtil.check("system:SystemDict:update");
    DictItemLocation location = requireItemLocation(id);
    SysDict dict = location.dict;
    SysDictItem item = location.item;

    if (req.getValue() != null) {
      String value = req.getValue().trim();
      SysDictItem existing = itemTableService.selectByDictValue(dict, value);
      if (existing != null && !Objects.equals(existing.getId(), item.getId())) {
        throw new IllegalArgumentException("Dictionary value already exists.");
      }
      item.setValue(value);
    }
    if (req.getLabel() != null) {
      item.setLabel(req.getLabel().trim());
    }
    if (req.getValueType() != null) {
      item.setValueType(normalizeValueType(req.getValueType()));
    }
    if (req.getStatus() != null) {
      item.setStatus(req.getStatus());
    }
    if (req.getSort() != null) {
      item.setSort(req.getSort());
    }
    if (req.getTagColor() != null) {
      item.setTagColor(req.getTagColor());
    }
    item.setUpdatedAt(LocalDateTime.now());

    itemTableService.update(dict, item);
    operationLogService.log("UPDATE", "Dictionary", "Update dictionary item: " + item.getLabel());
    return item;
  }

  @Transactional
  public boolean deleteItem(long id) {
    PermissionUtil.check("system:SystemDict:delete");
    DictItemLocation location = findItemLocation(id);
    if (location == null) {
      return true;
    }

    itemTableService.deleteById(location.dict, id);
    operationLogService.log("DELETE", "Dictionary", "Delete dictionary item: " + location.item.getLabel());
    return true;
  }

  @Transactional
  public DictionaryImportResult importItems(long dictId, MultipartFile file) {
    PermissionUtil.check("system:SystemDict:create");
    SysDict dict = dictMapper.selectById(dictId);
    if (dict == null) {
      throw new IllegalArgumentException("Dictionary not found.");
    }
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("Uploaded file is empty.");
    }

    DictionaryImportResult result = new DictionaryImportResult();
    String name = file.getOriginalFilename();
    String lower = name == null ? "" : name.toLowerCase(Locale.ROOT);
    List<DictItemRow> rows;
    try {
      if (lower.endsWith(".xlsx") || lower.endsWith(".xls")) {
        rows = readFromExcel(file);
      } else if (lower.endsWith(".csv") || lower.endsWith(".txt")) {
        rows = readFromCsv(file);
      } else {
        throw new IllegalArgumentException("Only .xlsx/.xls/.csv/.txt files are supported.");
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to read dictionary file: " + e.getMessage());
    }

    int total = 0;
    int imported = 0;
    int updated = 0;
    int skipped = 0;
    int failed = 0;

    for (DictItemRow row : rows) {
      total += 1;
      if (!StringUtils.hasText(row.label) || !StringUtils.hasText(row.value)) {
        failed += 1;
        if (result.getErrors().size() < MAX_IMPORT_ERRORS) {
          result.addError("Row " + total + " missing label or value.");
        }
        continue;
      }

      String value = row.value.trim();
      SysDictItem existing = itemTableService.selectByDictValue(dict, value);
      if (existing == null) {
        SysDictItem item = new SysDictItem();
        item.setDictId(dictId);
        item.setLabel(row.label.trim());
        item.setValue(value);
        item.setValueType(normalizeValueType(row.valueType));
        item.setStatus(row.status == null ? 1 : row.status);
        item.setSort(row.sort == null ? 0 : row.sort);
        item.setTagColor(row.tagColor);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        itemTableService.insert(dict, item);
        imported += 1;
      } else {
        existing.setLabel(row.label.trim());
        existing.setValueType(normalizeValueType(row.valueType));
        existing.setStatus(row.status == null ? existing.getStatus() : row.status);
        existing.setSort(row.sort == null ? existing.getSort() : row.sort);
        if (row.tagColor != null) {
          existing.setTagColor(row.tagColor);
        }
        existing.setUpdatedAt(LocalDateTime.now());
        itemTableService.update(dict, existing);
        updated += 1;
      }
    }

    result.setTotal(total);
    result.setImported(imported);
    result.setUpdated(updated);
    result.setSkipped(skipped);
    result.setFailed(failed);
    operationLogService.log("IMPORT", "Dictionary", "Import dictionary items: " + dict.getName() + " imported=" + imported);
    return result;
  }

  public ResponseEntity<byte[]> exportItems(long dictId) {
    PermissionUtil.check("system:SystemDict:query");
    SysDict dict = dictMapper.selectById(dictId);
    if (dict == null) {
      throw new IllegalArgumentException("Dictionary not found.");
    }

    List<SysDictItem> items = itemTableService.selectByDict(dict);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.writeBytes("label,value,value_type,status,sort,tag_color\n".getBytes(StandardCharsets.UTF_8));
    for (SysDictItem item : items) {
      String line = String.format(
        "%s,%s,%s,%s,%s,%s\n",
        escapeCsv(item.getLabel()),
        escapeCsv(item.getValue()),
        escapeCsv(item.getValueType()),
        item.getStatus() == null ? "" : item.getStatus(),
        item.getSort() == null ? "" : item.getSort(),
        escapeCsv(item.getTagColor())
      );
      out.writeBytes(line.getBytes(StandardCharsets.UTF_8));
    }

    String filename = "dict_" + dict.getCode() + "_items.csv";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("text/csv"));
    headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
    return ResponseEntity.ok().headers(headers).body(out.toByteArray());
  }

  public void downloadTemplate(HttpServletResponse response) {
    PermissionUtil.check("system:SystemDict:query");
    try {
      String content = "label,value,value_type,status,sort,tag_color\nsample,example,string,1,1,success\n";
      response.setContentType("text/csv; charset=UTF-8");
      response.setHeader("Content-Disposition", "attachment; filename=dict_items_template.csv");
      response.getOutputStream().write(content.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate template: " + e.getMessage());
    }
  }

  private DictItemLocation requireItemLocation(long id) {
    DictItemLocation location = findItemLocation(id);
    if (location == null) {
      throw new IllegalArgumentException("Dictionary item not found.");
    }
    return location;
  }

  private DictItemLocation findItemLocation(long id) {
    List<SysDict> dicts = dictMapper.selectAll();
    if (dicts == null || dicts.isEmpty()) {
      return null;
    }

    for (SysDict dict : dicts) {
      if (dict == null || dict.getId() == null || !StringUtils.hasText(dict.getCode())) {
        continue;
      }
      try {
        SysDictItem item = itemTableService.selectById(dict, id);
        if (item != null) {
          return new DictItemLocation(dict, item);
        }
      } catch (Exception ignored) {
        // Skip broken dictionary tables and continue searching.
      }
    }
    return null;
  }

  private String normalizeCode(String code) {
    if (code == null) {
      throw new IllegalArgumentException("Dictionary code is required.");
    }
    String value = code.trim().toLowerCase(Locale.ROOT);
    if (value.isEmpty()) {
      throw new IllegalArgumentException("Dictionary code is required.");
    }
    if (!CODE_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("Dictionary code only allows letters, numbers, and underscore.");
    }
    return value;
  }

  private String normalizeValueType(String valueType) {
    if (!StringUtils.hasText(valueType)) {
      return "string";
    }
    String type = valueType.trim().toLowerCase(Locale.ROOT);
    return switch (type) {
      case "number", "int", "integer" -> "number";
      case "bool", "boolean" -> "boolean";
      default -> "string";
    };
  }

  private List<DictItemRow> readFromExcel(MultipartFile file) throws Exception {
    List<DictItemRow> rows = new ArrayList<>();
    DataFormatter formatter = new DataFormatter();
    try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
      Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
      if (sheet == null) {
        return rows;
      }
      boolean headerSkipped = false;
      for (Row row : sheet) {
        String label = formatter.formatCellValue(row.getCell(0));
        String value = formatter.formatCellValue(row.getCell(1));
        String valueType = formatter.formatCellValue(row.getCell(2));
        String status = formatter.formatCellValue(row.getCell(3));
        String sort = formatter.formatCellValue(row.getCell(4));
        String tagColor = formatter.formatCellValue(row.getCell(5));
        if (!headerSkipped && isHeaderRow(label, value)) {
          headerSkipped = true;
          continue;
        }
        DictItemRow item = new DictItemRow();
        item.label = label;
        item.value = value;
        item.valueType = valueType;
        item.status = parseInteger(status);
        item.sort = parseInteger(sort);
        item.tagColor = StringUtils.hasText(tagColor) ? tagColor.trim() : null;
        if (isEmptyRow(item)) {
          continue;
        }
        rows.add(item);
      }
    }
    return rows;
  }

  private List<DictItemRow> readFromCsv(MultipartFile file) throws Exception {
    List<DictItemRow> rows = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      boolean headerSkipped = false;
      while ((line = reader.readLine()) != null) {
        if (line.isBlank()) {
          continue;
        }
        String[] parts = line.split(",", -1);
        String label = parts.length > 0 ? parts[0].trim() : "";
        String value = parts.length > 1 ? parts[1].trim() : "";
        if (!headerSkipped && isHeaderRow(label, value)) {
          headerSkipped = true;
          continue;
        }
        DictItemRow item = new DictItemRow();
        item.label = label;
        item.value = value;
        item.valueType = parts.length > 2 ? parts[2].trim() : null;
        item.status = parts.length > 3 ? parseInteger(parts[3]) : null;
        item.sort = parts.length > 4 ? parseInteger(parts[4]) : null;
        item.tagColor = parts.length > 5 ? parts[5].trim() : null;
        if (isEmptyRow(item)) {
          continue;
        }
        rows.add(item);
      }
    }
    return rows;
  }

  private boolean isHeaderRow(String label, String value) {
    String lower = (label + value).toLowerCase(Locale.ROOT);
    return lower.contains("label") || lower.contains("name") || lower.contains("value");
  }

  private Integer parseInteger(String input) {
    if (!StringUtils.hasText(input)) {
      return null;
    }
    try {
      return Integer.parseInt(input.trim());
    } catch (NumberFormatException ignored) {
      return null;
    }
  }

  private boolean isEmptyRow(DictItemRow row) {
    return !StringUtils.hasText(row.label) && !StringUtils.hasText(row.value);
  }

  private String escapeCsv(String value) {
    if (value == null) {
      return "";
    }
    String escaped = value.replace("\"", "\"\"");
    if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r")) {
      return "\"" + escaped + "\"";
    }
    return escaped;
  }

  private static class DictItemRow {
    private String label;
    private String value;
    private String valueType;
    private Integer status;
    private Integer sort;
    private String tagColor;
  }

  private static class DictItemLocation {
    private final SysDict dict;
    private final SysDictItem item;

    private DictItemLocation(SysDict dict, SysDictItem item) {
      this.dict = dict;
      this.item = item;
    }
  }
}
