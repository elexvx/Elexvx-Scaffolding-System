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

  public AuthTokenFilter(AuthTokenService tokenService) {
    this.tokenService = tokenService;
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
        AuthPrincipal principal = new AuthPrincipal(session.getUserId(), token);
        UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(principal, token, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    }
    filterChain.doFilter(request, response);
  }

  private String resolveToken(HttpServletRequest request) {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (StringUtils.hasText(header)) {
      return header.startsWith("Bearer ") ? header.substring(7) : header;
    }
    String token = request.getParameter("token");
    return StringUtils.hasText(token) ? token : null;
  }
}
