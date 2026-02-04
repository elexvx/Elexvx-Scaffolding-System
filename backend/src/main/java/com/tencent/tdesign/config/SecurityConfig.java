package com.tencent.tdesign.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tdesign.security.AuthTokenFilter;
import com.tencent.tdesign.vo.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
  private final AuthTokenFilter authTokenFilter;
  private final ObjectMapper objectMapper;

  public SecurityConfig(AuthTokenFilter authTokenFilter, ObjectMapper objectMapper) {
    this.authTokenFilter = authTokenFilter;
    this.objectMapper = objectMapper;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(
          "/auth/login",
          "/auth/login/sms",
          "/auth/login/email",
          "/auth/sms/send",
          "/auth/email/send",
          "/auth/login/confirm",
          "/auth/login/pending/stream",
          "/auth/register",
          "/auth/captcha",
          "/captcha/**",
          "/auth/password/reset",
          "/auth/logout",
          "/system/ui/public",
          "/system/watermark",
          "/uploads/**",
          "/files/**",
          "/error",
          "/v3/api-docs/**",
          "/v3/api-docs",
          "/swagger-ui/**",
          "/swagger-ui.html",
          "/doc.html",
          "/favicon.ico"
        ).permitAll()
        .anyRequest().authenticated()
      )
      .exceptionHandling(ex -> ex
        .authenticationEntryPoint((request, response, authException) -> writeError(response, HttpStatus.UNAUTHORIZED))
        .accessDeniedHandler((request, response, accessDeniedException) -> writeError(response, HttpStatus.FORBIDDEN))
      )
      .httpBasic(Customizer.withDefaults());

    http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  private void writeError(HttpServletResponse response, HttpStatus status) throws IOException {
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    ApiResponse<Void> body = ApiResponse.failure(status.value(), status == HttpStatus.UNAUTHORIZED
      ? "未登录或登录已失效，请重新登录"
      : "权限不足，请联系管理员开通");
    objectMapper.writeValue(response.getOutputStream(), body);
  }
}
