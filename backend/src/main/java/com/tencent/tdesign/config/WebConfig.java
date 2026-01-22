package com.tencent.tdesign.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import cn.dev33.satoken.filter.SaPathCheckFilterForJakartaServlet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tdesign.service.SensitiveService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
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
  @Bean
  public FilterRegistrationBean<CorsFilter> corsFilterRegistration() {
    CorsConfiguration config = new CorsConfiguration();
    config.addAllowedOriginPattern("*");
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    
    FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>(new CorsFilter(source));
    registration.setAsyncSupported(true);
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return registration;
  }

  @Bean
  public FilterRegistrationBean<SaPathCheckFilterForJakartaServlet> saTokenPathCheckFilterRegistration(
    SaPathCheckFilterForJakartaServlet filter
  ) {
    FilterRegistrationBean<SaPathCheckFilterForJakartaServlet> registration = new FilterRegistrationBean<>(filter);
    registration.setAsyncSupported(true);
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
    return registration;
  }

  @Bean
  public FilterRegistrationBean<SensitiveWordFilter> sensitiveWordFilterRegistration(
    SensitiveService sensitiveService,
    ObjectMapper objectMapper
  ) {
    FilterRegistrationBean<SensitiveWordFilter> registration =
      new FilterRegistrationBean<>(new SensitiveWordFilter(sensitiveService, objectMapper));
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
    String location = "file:" + uploadDir.toAbsolutePath().toString() + "/";
    registry.addResourceHandler("/uploads/**").addResourceLocations(location);
  }

}
