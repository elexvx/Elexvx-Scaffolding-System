package com.tencent.tdesign.print;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tdesign.service.ModuleBackendProcessManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class PrintService {
  private final JdbcTemplate jdbcTemplate;
  private final ModuleBackendProcessManager processManager;
  private final Map<String, PrintDataProvider> providers;
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;

  public PrintService(JdbcTemplate jdbcTemplate, ModuleBackendProcessManager processManager, List<PrintDataProvider> dataProviders, ObjectMapper objectMapper) {
    this.jdbcTemplate = jdbcTemplate;
    this.processManager = processManager;
    this.objectMapper = objectMapper;
    this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    this.providers = dataProviders.stream().collect(Collectors.toMap(PrintDataProvider::definitionId, p -> p, (a, b) -> a));
  }

  public List<PrintDefinition> listDefinitions() {
    List<Map<String, Object>> rows = jdbcTemplate.queryForList(
      "SELECT id, biz_type, name, template_key FROM print_template WHERE enabled = 1 ORDER BY updated_at DESC"
    );
    List<PrintDefinition> definitions = new ArrayList<>();
    for (Map<String, Object> row : rows) {
      PrintDefinition item = new PrintDefinition();
      String templateKey = stringValue(row.get("template_key"));
      String bizType = stringValue(row.get("biz_type"));
      String id = stringValue(row.get("id"));
      item.setDefinitionId(templateKey.isBlank() ? bizType + ":" + id : templateKey);
      item.setName(stringValue(row.get("name")));
      item.setTemplateType(TemplateType.PDFBOX_LAYOUT);
      item.setPermission("plugin:" + bizType + ":print");
      definitions.add(item);
    }
    return definitions;
  }

  public String createJob(PrintJobRequest request, String authToken) {
    try {
      Map<String, Object> template = resolveTemplate(request.getDefinitionId());
      if (template == null) throw new IllegalArgumentException("未找到对应模板");

      Map<String, Object> data = null;
      if (request.getParams() != null && request.getParams().get("data") instanceof Map<?, ?> mapData) {
        data = (Map<String, Object>) mapData;
      }
      if (data == null) {
        PrintDataProvider provider = providers.get(request.getDefinitionId());
        if (provider != null) data = provider.load(request.getBusinessRef(), request.getParams());
      }
      if (data == null) data = new HashMap<>();

      Map<String, Object> payload = new HashMap<>();
      payload.put("templateId", longValue(template.get("id")));
      payload.put("bizType", stringValue(template.get("biz_type")));
      payload.put("bizId", request.getBusinessRef());
      payload.put("data", data);

      int port = ensurePrintModulePort();
      HttpRequest httpRequest = HttpRequest.newBuilder()
        .uri(URI.create("http://127.0.0.1:" + port + "/render/pdf"))
        .timeout(Duration.ofSeconds(30))
        .header("Content-Type", "application/json")
        .header("Authorization", authToken)
        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload), StandardCharsets.UTF_8))
        .build();
      HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
      if (response.statusCode() >= 400) {
        throw new IllegalArgumentException("打印模块调用失败: " + response.body());
      }
      Map<String, Object> body = objectMapper.readValue(response.body(), new TypeReference<>() {});
      return String.valueOf(body.get("jobId"));
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException("创建打印任务失败: " + e.getMessage());
    }
  }

  public byte[] load(String jobId, String authToken, String requestBaseUrl) {
    try {
      Map<String, Object> row = jdbcTemplate.queryForMap("SELECT file_url FROM print_job WHERE id = ?", Long.parseLong(jobId));
      String fileUrl = stringValue(row.get("file_url"));
      if (fileUrl.isBlank()) throw new IllegalArgumentException("打印文件不存在");
      String resolved = fileUrl.startsWith("http") ? fileUrl : requestBaseUrl + (fileUrl.startsWith("/") ? fileUrl : "/" + fileUrl);
      HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(resolved))
        .timeout(Duration.ofSeconds(30))
        .header("Authorization", authToken)
        .GET()
        .build();
      HttpResponse<byte[]> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofByteArray());
      if (resp.statusCode() >= 400) throw new IllegalArgumentException("下载文件失败");
      return resp.body();
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException("打印任务不存在");
    }
  }

  private int ensurePrintModulePort() {
    processManager.ensureRunning("print");
    int port = processManager.getPort("print");
    if (port <= 0) throw new IllegalArgumentException("打印模块未就绪");
    return port;
  }

  private Map<String, Object> resolveTemplate(String definitionId) {
    if (definitionId == null || definitionId.isBlank()) return null;
    List<Map<String, Object>> byKey = jdbcTemplate.queryForList(
      "SELECT id, biz_type, template_key FROM print_template WHERE enabled = 1 AND template_key = ? ORDER BY updated_at DESC LIMIT 1",
      definitionId
    );
    if (!byKey.isEmpty()) return byKey.get(0);
    if (definitionId.contains(":")) {
      String[] parts = definitionId.split(":", 2);
      List<Map<String, Object>> byId = jdbcTemplate.queryForList(
        "SELECT id, biz_type, template_key FROM print_template WHERE enabled = 1 AND biz_type = ? AND id = ? LIMIT 1",
        parts[0], Long.parseLong(parts[1])
      );
      if (!byId.isEmpty()) return byId.get(0);
    }
    List<Map<String, Object>> byBizType = jdbcTemplate.queryForList(
      "SELECT id, biz_type, template_key FROM print_template WHERE enabled = 1 AND biz_type = ? ORDER BY updated_at DESC LIMIT 1",
      definitionId
    );
    return byBizType.isEmpty() ? null : byBizType.get(0);
  }

  private String stringValue(Object value) {
    return Optional.ofNullable(value).map(String::valueOf).orElse("");
  }

  private long longValue(Object value) {
    return Long.parseLong(stringValue(value));
  }
}
