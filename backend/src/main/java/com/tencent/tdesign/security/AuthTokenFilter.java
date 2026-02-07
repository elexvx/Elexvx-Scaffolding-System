package com.tencent.tdesign.security;

import com.tencent.tdesign.service.AuthTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {
  private final AuthTokenService tokenService;
  private final com.tencent.tdesign.service.SecuritySettingService securitySettingService;

  public AuthTokenFilter(
    AuthTokenService tokenService,
    com.tencent.tdesign.service.SecuritySettingService securitySettingService
  ) {
    this.tokenService = tokenService;
    this.securitySettingService = securitySettingService;
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    String token = resolveToken(request);
    if (StringUtils.hasText(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
      AuthSession session = tokenService.getSession(token);
      if (session != null) {
        touchSession(token, session);
        AuthPrincipal principal = new AuthPrincipal(session.getUserId(), token);
        UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(principal, token, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    }
    filterChain.doFilter(request, response);
  }

  private void touchSession(String token, AuthSession session) {
    long now = System.currentTimeMillis();
    Object lastAccessObj = session.getAttributes().get("lastAccessTime");
    Long lastAccess = lastAccessObj instanceof Number ? ((Number) lastAccessObj).longValue() : null;
    if (lastAccess == null || now - lastAccess > 30_000L) {
      session.getAttributes().put("lastAccessTime", now);
      tokenService.updateSession(token, session);
    }
  }

  private String resolveToken(HttpServletRequest request) {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (StringUtils.hasText(header)) {
      return header.startsWith("Bearer ") ? header.substring(7) : header;
    }
    String uri = request.getRequestURI();
    boolean isConcurrentStream = uri != null && uri.endsWith("/auth/concurrent/stream");
    boolean allowUrlTokenParam = Boolean.TRUE.equals(
      securitySettingService.getOrCreate().getAllowUrlTokenParam()
    );
    if (!allowUrlTokenParam && !isConcurrentStream) {
      return null;
    }
    String token = request.getParameter("token");
    if (!StringUtils.hasText(token)) {
      token = request.getParameter(HttpHeaders.AUTHORIZATION);
    }
    if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
      token = token.substring(7);
    }
    return StringUtils.hasText(token) ? token : null;
  }
}
