package com.tencent.tdesign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
  @NotBlank(message = "Account is required")
  @Size(max = 64, message = "Account length must be <= 64")
  @Pattern(regexp = "^[a-zA-Z0-9_@.-]+$", message = "Account contains invalid characters")
  private String account;

  @NotBlank(message = "Password is required")
  @Size(min = 6, max = 64, message = "Password length must be 6-64")
  private String password;

  @NotBlank(message = "Confirm password is required")
  private String confirmPassword;

  private String captchaId;

  private String captchaCode;

  public String getAccount() {
    return account;
  }

  public void setAccount(String account) {
    this.account = account;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getConfirmPassword() {
    return confirmPassword;
  }

  public void setConfirmPassword(String confirmPassword) {
    this.confirmPassword = confirmPassword;
  }

  public String getCaptchaId() {
    return captchaId;
  }

  public void setCaptchaId(String captchaId) {
    this.captchaId = captchaId;
  }

  public String getCaptchaCode() {
    return captchaCode;
  }

  public void setCaptchaCode(String captchaCode) {
    this.captchaCode = captchaCode;
  }
}
