package com.tencent.tdesign.verification;

import com.tencent.tdesign.entity.VerificationSetting;

public interface VerificationProvider {
  String getType();

  int getExpiresInSeconds();

  void sendCode(VerificationSetting setting, String target, String sourceIp, String providerHint);

  boolean verify(String target, String code);

  void invalidate(String target);
}
