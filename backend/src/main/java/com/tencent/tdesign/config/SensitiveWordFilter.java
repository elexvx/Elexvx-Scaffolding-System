package com.tencent.tdesign.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tdesign.service.SensitiveService;
import com.tencent.tdesign.vo.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

public class SensitiveWordFilter extends OncePerRequestFilter {
  private static final String HEADER_PAGE_PATH = "X-Page-Path";

  private final SensitiveService sensitiveService;
  private final ObjectMapper objectMapper;

  public SensitiveWordFilter(SensitiveService sensitiveService, ObjectMapper objectMapper) {
    this.sensitiveService = sensitiveService;
    this.objectMapper = objectMapper;
  }

  @Override
  protected void doFilterInternal(
    @NonNull HttpServletRequest request,
    @NonNull HttpServletResponse response,
    @NonNull FilterChain filterChain
  )
    throws ServletException, IOException {
    if (!shouldCheck(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
    String pageKey = resolvePageKey(request);
    if (!sensitiveService.isPageEnabled(pageKey)) {
      filterChain.doFilter(cachedRequest, response);
      return;
    }

    String body = cachedRequest.getCachedBodyString();
    if (body == null || body.isBlank()) {
      filterChain.doFilter(cachedRequest, response);
      return;
    }

    Object payload;
    try {
      payload = objectMapper.readValue(body, Object.class);
    } catch (Exception e) {
      filterChain.doFilter(cachedRequest, response);
      return;
    }

    SensitiveService.SensitiveHit hit = sensitiveService.findHit(payload);
    if (hit != null) {
      respondViolation(response, hit);
      return;
    }

    filterChain.doFilter(cachedRequest, response);
  }

  private String resolvePageKey(HttpServletRequest request) {
    String header = request.getHeader(HEADER_PAGE_PATH);
    if (header != null && !header.isBlank()) return header;
    String referer = request.getHeader("Referer");
    if (referer == null || referer.isBlank()) return null;
    try {
      URI uri = URI.create(referer);
      return uri.getPath();
    } catch (Exception ignored) {
      return null;
    }
  }

  private boolean shouldCheck(HttpServletRequest request) {
    String method = request.getMethod();
    if (method == null) return false;
    String upper = method.toUpperCase();
    if ("GET".equals(upper) || "HEAD".equals(upper) || "OPTIONS".equals(upper)) {
      return false;
    }
    String uri = request.getRequestURI();
    if (uri != null && uri.contains("/system/sensitive")) {
      return false;
    }
    String contentType = request.getContentType();
    if (contentType == null || contentType.isBlank()) {
      return true;
    }
    return contentType.toLowerCase().contains(MediaType.APPLICATION_JSON_VALUE);
  }

  private void respondViolation(HttpServletResponse response, SensitiveService.SensitiveHit hit) throws IOException {
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    String field = (hit.fieldPath() == null || hit.fieldPath().isBlank()) ? "输入内容" : hit.fieldPath();
    String word = hit.word() == null ? "" : hit.word();
    String message = word.isBlank()
      ? "字段 " + field + " 包含敏感词，提交已被拒绝"
      : "字段 " + field + " 包含敏感词“" + word + "”，提交已被拒绝";
    ApiResponse<Object> api = ApiResponse.failure(422, message);
    response.getWriter().write(objectMapper.writeValueAsString(api));
  }
}
