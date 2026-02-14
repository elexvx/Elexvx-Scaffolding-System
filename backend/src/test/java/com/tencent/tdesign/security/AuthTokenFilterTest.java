package com.tencent.tdesign.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tencent.tdesign.entity.SecuritySetting;
import com.tencent.tdesign.service.AuthTokenService;
import com.tencent.tdesign.service.SecuritySettingService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

class AuthTokenFilterTest {

  @Test
  void resolveTokenWithoutQueryParamSkipsSecuritySettingLookup() {
    AuthTokenService tokenService = org.mockito.Mockito.mock(AuthTokenService.class);
    SecuritySettingService securitySettingService = org.mockito.Mockito.mock(SecuritySettingService.class);
    AuthTokenFilter filter = new AuthTokenFilter(tokenService, securitySettingService);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/system/user/page");

    String token = (String) ReflectionTestUtils.invokeMethod(filter, "resolveToken", request);

    assertNull(token);
    verify(securitySettingService, never()).getOrCreate();
  }

  @Test
  void resolveTokenWithQueryParamChecksSecuritySetting() {
    AuthTokenService tokenService = org.mockito.Mockito.mock(AuthTokenService.class);
    SecuritySettingService securitySettingService = org.mockito.Mockito.mock(SecuritySettingService.class);
    SecuritySetting setting = new SecuritySetting();
    setting.setAllowUrlTokenParam(true);
    when(securitySettingService.getOrCreate()).thenReturn(setting);
    AuthTokenFilter filter = new AuthTokenFilter(tokenService, securitySettingService);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/system/user/page");
    request.setParameter("token", "abc");

    String token = (String) ReflectionTestUtils.invokeMethod(filter, "resolveToken", request);

    assertEquals("abc", token);
    verify(securitySettingService).getOrCreate();
  }

  @Test
  void concurrentStreamAllowsQueryTokenWithoutSecuritySetting() {
    AuthTokenService tokenService = org.mockito.Mockito.mock(AuthTokenService.class);
    SecuritySettingService securitySettingService = org.mockito.Mockito.mock(SecuritySettingService.class);
    AuthTokenFilter filter = new AuthTokenFilter(tokenService, securitySettingService);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/auth/concurrent/stream");
    request.setParameter(HttpHeaders.AUTHORIZATION, "Bearer abc");

    String token = (String) ReflectionTestUtils.invokeMethod(filter, "resolveToken", request);

    assertEquals("abc", token);
    verify(securitySettingService, never()).getOrCreate();
  }
}
