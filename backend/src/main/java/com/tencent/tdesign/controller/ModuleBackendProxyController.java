package com.tencent.tdesign.controller;

import com.tencent.tdesign.service.ModuleBackendProcessManager;
import com.tencent.tdesign.service.ModuleRegistryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Enumeration;
import java.util.Locale;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/module-api")
public class ModuleBackendProxyController {
  private final ModuleRegistryService moduleRegistryService;
  private final ModuleBackendProcessManager processManager;
  private final HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();

  public ModuleBackendProxyController(ModuleRegistryService moduleRegistryService, ModuleBackendProcessManager processManager) {
    this.moduleRegistryService = moduleRegistryService;
    this.processManager = processManager;
  }

  @RequestMapping("/{moduleKey}/**")
  public void proxy(@PathVariable String moduleKey, HttpServletRequest request, HttpServletResponse response) throws IOException {
    String key = normalizeKey(moduleKey);
    moduleRegistryService.assertModuleAvailable(key);
    processManager.ensureRunning(key);
    int port = processManager.getPort(key);
    if (port <= 0) {
      response.setStatus(503);
      response.getWriter().write("模块后端未就绪");
      return;
    }

    String requestUri = request.getRequestURI();
    String contextPath = String.valueOf(request.getContextPath() == null ? "" : request.getContextPath());
    String prefix = contextPath + "/module-api/" + key + "/";
    String rest = requestUri.startsWith(prefix) ? requestUri.substring(prefix.length()) : "";
    String query = request.getQueryString();
    String target = "http://127.0.0.1:" + port + "/" + rest + (query == null || query.isBlank() ? "" : "?" + query);

    HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(target)).method(request.getMethod(), buildBodyPublisher(request));
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String name = headerNames.nextElement();
      if (name == null) continue;
      String lower = name.toLowerCase(Locale.ROOT);
      if ("host".equals(lower) || "content-length".equals(lower)) continue;
      Enumeration<String> values = request.getHeaders(name);
      while (values.hasMoreElements()) {
        String v = values.nextElement();
        if (v != null) builder.header(name, v);
      }
    }

    try {
      HttpResponse<byte[]> proxied = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
      response.setStatus(proxied.statusCode());
      for (var header : proxied.headers().map().entrySet()) {
        String name = header.getKey();
        if (name == null) continue;
        String lower = name.toLowerCase(Locale.ROOT);
        if ("transfer-encoding".equals(lower)) continue;
        for (String v : header.getValue()) {
          response.addHeader(name, v);
        }
      }
      response.getOutputStream().write(proxied.body());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      response.setStatus(502);
      response.getWriter().write("模块后端请求中断");
    } catch (Exception e) {
      response.setStatus(502);
      response.getWriter().write("模块后端代理失败: " + e.getMessage());
    }
  }

  private HttpRequest.BodyPublisher buildBodyPublisher(HttpServletRequest request) throws IOException {
    String method = String.valueOf(request.getMethod()).toUpperCase(Locale.ROOT);
    if ("GET".equals(method) || "HEAD".equals(method)) return HttpRequest.BodyPublishers.noBody();
    byte[] body = request.getInputStream().readAllBytes();
    if (body.length == 0) return HttpRequest.BodyPublishers.noBody();
    return HttpRequest.BodyPublishers.ofByteArray(body);
  }

  private String normalizeKey(String moduleKey) {
    return String.valueOf(moduleKey == null ? "" : moduleKey).trim().toLowerCase(Locale.ROOT);
  }
}
