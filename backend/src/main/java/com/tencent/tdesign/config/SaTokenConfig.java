package com.tencent.tdesign.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {
  @Override
  public void addInterceptors(@NonNull InterceptorRegistry registry) {
    // 注册 Sa-Token 拦截器
    registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
      .addPathPatterns("/**")
      // 排除不需要登录的接口
      .excludePathPatterns(
        "/auth/login",
        "/auth/login/sms",
        "/auth/login/email",
        "/auth/sms/send",        // 登录接口
        "/auth/email/send",
        "/auth/login/confirm", // 登录确认接口
        "/auth/login/pending/stream", // 登录等待确认SSE
        "/auth/register",     // 注册接口
        "/auth/captcha",      // 验证码接口
        "/captcha/**",        // AjCaptcha 接口
        "/auth/password/reset",
        "/auth/logout",       // 退出接口（幂等）
        "/system/ui",         // UI 配置接口
        "/system/watermark",  // 水印配置接口
        "/uploads/**",        // 静态资源
        "/files/**",          // Encrypted file access
        "/error",             // 错误页面
        "/v3/api-docs/**",    // Swagger API docs
        "/v3/api-docs",       // Swagger API docs (root)
        "/swagger-ui/**",     // Swagger UI resources
        "/swagger-ui.html",   // Swagger UI entry
        "/doc.html",          // Knife4j entry (if used)
        "/favicon.ico"        // Favicon
      );
  }
}
