package com.tencent.tdesign.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tdesign.security.AuthTokenFilter;
import com.tencent.tdesign.vo.ApiResponse;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

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
      .headers(headers -> headers
        .frameOptions(frame -> frame.deny())
        .contentTypeOptions(withDefaults())
        .cacheControl(withDefaults())
        .httpStrictTransportSecurity(hsts -> hsts
          .includeSubDomains(true)
          .preload(true)
          .maxAgeInSeconds(31536000)
        )
        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
        .permissionsPolicy(policy -> policy.policy("geolocation=(), microphone=(), camera=(), payment=()"))
      )
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth -> auth
        .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()
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
          "/health",
          "/error",
          "/favicon.ico"
        ).permitAll()
        .anyRequest().authenticated()
      )
      .exceptionHandling(ex -> ex
        .authenticationEntryPoint((request, response, authException) -> {
          if (response.isCommitted()) return;
          writeError(response, HttpStatus.UNAUTHORIZED);
        })
        .accessDeniedHandler((request, response, accessDeniedException) -> {
          if (response.isCommitted()) return;
          writeError(response, HttpStatus.FORBIDDEN);
        })
      )
      .httpBasic(AbstractHttpConfigurer::disable)
      .formLogin(AbstractHttpConfigurer::disable);

    http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  private void writeError(HttpServletResponse response, HttpStatus status) throws IOException {
    if (response.isCommitted()) return;
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    ApiResponse<Void> body = ApiResponse.failure(status.value(), status == HttpStatus.UNAUTHORIZED
      ? "未登录或登录已失效，请重新登录"
      : "权限不足，请联系管理员开通");
    objectMapper.writeValue(response.getOutputStream(), body);
  }
}
