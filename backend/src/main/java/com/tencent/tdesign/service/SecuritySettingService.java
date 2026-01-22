package com.tencent.tdesign.service;

import com.tencent.tdesign.dto.UiSettingRequest;
import com.tencent.tdesign.entity.SecurityCaptchaSetting;
import com.tencent.tdesign.entity.SecurityPasswordPolicy;
import com.tencent.tdesign.entity.SecuritySetting;
import com.tencent.tdesign.entity.SecurityTokenSetting;
import com.tencent.tdesign.mapper.SecurityCaptchaSettingMapper;
import com.tencent.tdesign.mapper.SecurityPasswordPolicyMapper;
import com.tencent.tdesign.mapper.SecurityTokenSettingMapper;
import org.springframework.stereotype.Service;

@Service
public class SecuritySettingService {
  private final SecurityTokenSettingMapper tokenMapper;
  private final SecurityCaptchaSettingMapper captchaMapper;
  private final SecurityPasswordPolicyMapper passwordMapper;

  public SecuritySettingService(
      SecurityTokenSettingMapper tokenMapper,
      SecurityCaptchaSettingMapper captchaMapper,
      SecurityPasswordPolicyMapper passwordMapper) {
    this.tokenMapper = tokenMapper;
    this.captchaMapper = captchaMapper;
    this.passwordMapper = passwordMapper;
  }

  public SecuritySetting getOrCreate() {
    SecuritySetting out = new SecuritySetting();
    SecurityTokenSetting token = getOrCreateToken();
    out.setSessionTimeoutMinutes(token.getSessionTimeoutMinutes());
    out.setTokenTimeoutMinutes(token.getTokenTimeoutMinutes());
    out.setTokenRefreshGraceMinutes(token.getTokenRefreshGraceMinutes());

    SecurityCaptchaSetting captcha = getOrCreateCaptcha();
    out.setCaptchaEnabled(captcha.getCaptchaEnabled());
    out.setCaptchaType(captcha.getCaptchaType());
    out.setDragCaptchaWidth(captcha.getDragCaptchaWidth());
    out.setDragCaptchaHeight(captcha.getDragCaptchaHeight());
    out.setDragCaptchaThreshold(captcha.getDragCaptchaThreshold());
    out.setImageCaptchaLength(captcha.getImageCaptchaLength());
    out.setImageCaptchaNoiseLines(captcha.getImageCaptchaNoiseLines());

    SecurityPasswordPolicy policy = getOrCreatePasswordPolicy();
    out.setPasswordMinLength(policy.getPasswordMinLength());
    out.setPasswordRequireUppercase(policy.getPasswordRequireUppercase());
    out.setPasswordRequireLowercase(policy.getPasswordRequireLowercase());
    out.setPasswordRequireSpecial(policy.getPasswordRequireSpecial());
    out.setPasswordAllowSequential(policy.getPasswordAllowSequential());
    return out;
  }

  public boolean applyRequest(UiSettingRequest req) {
    boolean changed = false;

    SecurityTokenSetting token = tokenMapper.selectTop();
    if (token == null)
      token = new SecurityTokenSetting();
    boolean tokenChanged = false;
    if (req.getSessionTimeoutMinutes() != null) {
      token.setSessionTimeoutMinutes(req.getSessionTimeoutMinutes());
      tokenChanged = true;
    }
    if (req.getTokenTimeoutMinutes() != null) {
      token.setTokenTimeoutMinutes(req.getTokenTimeoutMinutes());
      tokenChanged = true;
    }
    if (req.getTokenRefreshGraceMinutes() != null) {
      token.setTokenRefreshGraceMinutes(req.getTokenRefreshGraceMinutes());
      tokenChanged = true;
    }
    if (tokenChanged) {
      upsertToken(token);
      changed = true;
    }

    SecurityCaptchaSetting captcha = captchaMapper.selectTop();
    if (captcha == null)
      captcha = new SecurityCaptchaSetting();
    boolean captchaChanged = false;
    if (req.getCaptchaEnabled() != null) {
      captcha.setCaptchaEnabled(req.getCaptchaEnabled());
      captchaChanged = true;
    }
    if (req.getCaptchaType() != null) {
      captcha.setCaptchaType(req.getCaptchaType());
      captchaChanged = true;
    }
    if (req.getDragCaptchaWidth() != null) {
      captcha.setDragCaptchaWidth(req.getDragCaptchaWidth());
      captchaChanged = true;
    }
    if (req.getDragCaptchaHeight() != null) {
      captcha.setDragCaptchaHeight(req.getDragCaptchaHeight());
      captchaChanged = true;
    }
    if (req.getDragCaptchaThreshold() != null) {
      captcha.setDragCaptchaThreshold(req.getDragCaptchaThreshold());
      captchaChanged = true;
    }
    if (req.getImageCaptchaLength() != null) {
      captcha.setImageCaptchaLength(req.getImageCaptchaLength());
      captchaChanged = true;
    }
    if (req.getImageCaptchaNoiseLines() != null) {
      captcha.setImageCaptchaNoiseLines(req.getImageCaptchaNoiseLines());
      captchaChanged = true;
    }
    if (captchaChanged) {
      upsertCaptcha(captcha);
      changed = true;
    }

    SecurityPasswordPolicy policy = passwordMapper.selectTop();
    if (policy == null)
      policy = new SecurityPasswordPolicy();
    boolean policyChanged = false;
    if (req.getPasswordMinLength() != null) {
      policy.setPasswordMinLength(req.getPasswordMinLength());
      policyChanged = true;
    }
    if (req.getPasswordRequireUppercase() != null) {
      policy.setPasswordRequireUppercase(req.getPasswordRequireUppercase());
      policyChanged = true;
    }
    if (req.getPasswordRequireLowercase() != null) {
      policy.setPasswordRequireLowercase(req.getPasswordRequireLowercase());
      policyChanged = true;
    }
    if (req.getPasswordRequireSpecial() != null) {
      policy.setPasswordRequireSpecial(req.getPasswordRequireSpecial());
      policyChanged = true;
    }
    if (req.getPasswordAllowSequential() != null) {
      policy.setPasswordAllowSequential(req.getPasswordAllowSequential());
      policyChanged = true;
    }
    if (policyChanged) {
      upsertPasswordPolicy(policy);
      changed = true;
    }

    return changed;
  }

  private SecurityTokenSetting getOrCreateToken() {
    SecurityTokenSetting token = tokenMapper.selectTop();
    if (token == null) {
      token = new SecurityTokenSetting();
      tokenMapper.insert(token);
    }
    return token;
  }

  private SecurityCaptchaSetting getOrCreateCaptcha() {
    SecurityCaptchaSetting captcha = captchaMapper.selectTop();
    if (captcha == null) {
      captcha = new SecurityCaptchaSetting();
      captchaMapper.insert(captcha);
    }
    return captcha;
  }

  private SecurityPasswordPolicy getOrCreatePasswordPolicy() {
    SecurityPasswordPolicy policy = passwordMapper.selectTop();
    if (policy == null) {
      policy = new SecurityPasswordPolicy();
      passwordMapper.insert(policy);
    }
    return policy;
  }

  private void upsertToken(SecurityTokenSetting setting) {
    if (setting.getId() == null)
      tokenMapper.insert(setting);
    else
      tokenMapper.update(setting);
  }

  private void upsertCaptcha(SecurityCaptchaSetting setting) {
    if (setting.getId() == null)
      captchaMapper.insert(setting);
    else
      captchaMapper.update(setting);
  }

  private void upsertPasswordPolicy(SecurityPasswordPolicy policy) {
    if (policy.getId() == null)
      passwordMapper.insert(policy);
    else
      passwordMapper.update(policy);
  }
}
