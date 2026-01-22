package com.tencent.tdesign.config;

import cn.dev33.satoken.listener.SaTokenEventCenter;
import cn.dev33.satoken.listener.SaTokenListener;
import cn.dev33.satoken.stp.SaLoginModel;
import com.tencent.tdesign.service.OnlineUserTokenRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class OnlineUserSaTokenListener implements SaTokenListener {

  private final OnlineUserTokenRegistry tokenRegistry;

  public OnlineUserSaTokenListener(OnlineUserTokenRegistry tokenRegistry) {
    this.tokenRegistry = tokenRegistry;
  }

  @PostConstruct
  public void register() {
    if (!SaTokenEventCenter.hasListener(OnlineUserSaTokenListener.class)) {
      SaTokenEventCenter.registerListener(this);
    }
  }

  @Override
  public void doLogin(String loginType, Object loginId, String tokenValue, SaLoginModel loginModel) {
    tokenRegistry.addToken(tokenValue);
  }

  @Override
  public void doLogout(String loginType, Object loginId, String tokenValue) {
    tokenRegistry.removeToken(tokenValue);
  }

  @Override
  public void doKickout(String loginType, Object loginId, String tokenValue) {
    tokenRegistry.removeToken(tokenValue);
  }

  @Override
  public void doReplaced(String loginType, Object loginId, String tokenValue) {
    tokenRegistry.removeToken(tokenValue);
  }

  @Override
  public void doDisable(String loginType, Object loginId, String service, int level, long disableTime) {}

  @Override
  public void doUntieDisable(String loginType, Object loginId, String service) {}

  @Override
  public void doOpenSafe(String loginType, String tokenValue, String service, long safeTime) {}

  @Override
  public void doCloseSafe(String loginType, String tokenValue, String service) {}

  @Override
  public void doCreateSession(String id) {}

  @Override
  public void doLogoutSession(String id) {}

  @Override
  public void doRenewTimeout(String loginType, Object loginId, long timeout) {}
}
