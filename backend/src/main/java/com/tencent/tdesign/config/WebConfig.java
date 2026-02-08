package com.tencent.tdesign.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tdesign.service.ModuleRegistryService;
import com.tencent.tdesign.service.SensitiveService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  private final String corsAllowedOriginPatterns;

  public WebConfig(@Value("${tdesign.web.cors.allowed-origin-patterns:*}") String corsAllowedOriginPatterns) {
    this.corsAllowedOriginPatterns = corsAllowedOriginPatterns;
  }

  @Bean
  public FilterRegistrationBean<CorsFilter> corsFilterRegistration() {
    CorsConfiguration config = new CorsConfiguration();
    for (String pattern : splitCsv(corsAllowedOriginPatterns)) {
      config.addAllowedOriginPattern(pattern);
    }
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    config.setAllowCredentials(false);
    config.addExposedHeader("Content-Disposition");
    config.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    
    FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>(new CorsFilter(source));
    registration.setAsyncSupported(true);
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return registration;
  }

  @Bean
  public FilterRegistrationBean<SensitiveWordFilter> sensitiveWordFilterRegistration(
    ModuleRegistryService moduleRegistryService,
    SensitiveService sensitiveService,
    ObjectMapper objectMapper
  ) {
    FilterRegistrationBean<SensitiveWordFilter> registration =
      new FilterRegistrationBean<>(new SensitiveWordFilter(moduleRegistryService, sensitiveService, objectMapper));
    registration.setAsyncSupported(true);
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
    return registration;
  }

  @Override
  public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
    Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads");
    try {
      Files.createDirectories(uploadDir);
      Files.createDirectories(uploadDir.resolve("system"));
      Files.createDirectories(uploadDir.resolve("business"));
    } catch (Exception ignored) {}
    String base = "file:" + uploadDir.toAbsolutePath().toString() + "/";
    registry.addResourceHandler("/uploads/system/**").addResourceLocations(base + "system/");
    registry.addResourceHandler("/uploads/business/**").addResourceLocations(base + "business/");
  }

  private List<String> splitCsv(String raw) {
    if (raw == null) return List.of("*");
    String cleaned = raw.trim();
    if (cleaned.isEmpty()) return List.of("*");
    String[] parts = cleaned.split(",");
    List<String> out = new ArrayList<>();
    for (String p : parts) {
      String v = p == null ? "" : p.trim();
      if (!v.isEmpty()) out.add(v);
    }
    return out.isEmpty() ? List.of("*") : out;
  }
}
