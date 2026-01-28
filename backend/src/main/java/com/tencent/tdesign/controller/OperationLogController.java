package com.tencent.tdesign.controller;

import com.tencent.tdesign.service.OperationLogService;
import com.tencent.tdesign.security.AccessControlService;
import com.tencent.tdesign.security.AuthContext;
import com.tencent.tdesign.util.PermissionUtil;
import com.tencent.tdesign.vo.ApiResponse;
import com.tencent.tdesign.vo.OperationLogVO;
import com.tencent.tdesign.vo.PageResult;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/log")
public class OperationLogController {
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private final OperationLogService service;
  private final AuthContext authContext;
  private final AccessControlService accessControlService;

  public OperationLogController(
    OperationLogService service,
    AuthContext authContext,
    AccessControlService accessControlService
  ) {
    this.service = service;
    this.authContext = authContext;
    this.accessControlService = accessControlService;
  }

  @GetMapping("/page")
  public ApiResponse<PageResult<OperationLogVO>> page(
    @RequestParam(required = false) String keyword,
    @RequestParam(required = false) String action,
    @RequestParam(required = false) String start,
    @RequestParam(required = false) String end,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) {
    PermissionUtil.check("system:SystemLog:query");
    LocalDate startDate = parseDate(start);
    LocalDate endDate = parseDate(end);
    Long userId = accessControlService.hasRole("admin") ? null : authContext.requireUserId();
    return ApiResponse.success(service.page(keyword, action, startDate, endDate, userId, page, size));
  }

  @GetMapping("/export")
  public ResponseEntity<byte[]> export(
    @RequestParam(required = false) String keyword,
    @RequestParam(required = false) String action,
    @RequestParam(required = false) String start,
    @RequestParam(required = false) String end
  ) {
    PermissionUtil.check("system:SystemLog:query");
    LocalDate startDate = parseDate(start);
    LocalDate endDate = parseDate(end);
    Long userId = accessControlService.hasRole("admin") ? null : authContext.requireUserId();
    List<OperationLogVO> list = service.listAll(keyword, action, startDate, endDate, userId);

    StringBuilder sb = new StringBuilder();
    sb.append("id,action,module,detail,account,ip,device,createdAt\n");
    for (OperationLogVO row : list) {
      sb.append(safe(row.getId()))
        .append(',')
        .append(safe(row.getAction()))
        .append(',')
        .append(safe(row.getModule()))
        .append(',')
        .append(safe(row.getDetail()))
        .append(',')
        .append(safe(row.getAccount()))
        .append(',')
        .append(safe(row.getIpAddress()))
        .append(',')
        .append(safe(row.getDeviceInfo()))
        .append(',')
        .append(safe(row.getCreatedAt()))
        .append('\n');
    }

    String fileName = "operation-logs-" + LocalDate.now().format(DATE_FORMAT) + ".csv";
    String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(new MediaType("text", "csv", Objects.requireNonNull(StandardCharsets.UTF_8)));
    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encoded + "\"");
    return ResponseEntity.ok().headers(headers).body(sb.toString().getBytes(StandardCharsets.UTF_8));
  }

  private LocalDate parseDate(String value) {
    if (value == null || value.isBlank()) return null;
    return LocalDate.parse(value, DATE_FORMAT);
  }

  private String safe(Object value) {
    if (value == null) return "";
    String s = String.valueOf(value);
    s = s.replace("\"", "\"\"");
    if (s.contains(",") || s.contains("\n")) {
      return "\"" + s + "\"";
    }
    return s;
  }
}
