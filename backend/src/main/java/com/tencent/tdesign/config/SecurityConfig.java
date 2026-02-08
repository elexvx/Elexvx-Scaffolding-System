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
/**
 * Spring Security 访问控制配置。
 *
 * <p>设计要点：
 * <ul>
 *   <li>采用无状态会话（{@code SessionCreationPolicy.STATELESS}），实际登录态由 Token + 服务端会话存储维护。</li>
 *   <li>白名单仅放行登录/注册/验证码、静态资源、Swagger 文档等公共端点；其他请求必须携带有效 Token。</li>
 *   <li>认证失败与鉴权失败统一返回 {@code ApiResponse}，避免前端对非 JSON 响应做兼容处理。</li>
 * </ul>
 */
public class SecurityConfig {
  private final AuthTokenFilter authTokenFilter;
  private final ObjectMapper objectMapper;

  public SecurityConfig(AuthTokenFilter authTokenFilter, ObjectMapper objectMapper) {
    this.authTokenFilter = authTokenFilter;
    this.objectMapper = objectMapper;
  }

  @Bean
  /**
   * 定义过滤器链与鉴权策略。
   *
   * <p>Token 解析过滤器放在 {@link UsernamePasswordAuthenticationFilter} 之前，以便后续鉴权决策能读取到
   * {@code SecurityContext} 中的认证信息。
   */
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .headers(headers -> headers
        .frameOptions(frame -> frame.sameOrigin())
        .contentTypeOptions(withDefaults())
        .cacheControl(withDefaults())
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
          "/uploads/system/**",
          "/uploads/business/**",
          "/files/**",
          "/health",
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

  /**
   * 输出统一错误响应体。
   *
   * <p>注意：过滤器链可能在响应已提交后再次触发异常处理，因此写响应前必须检查 {@code isCommitted()}。
   */
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
