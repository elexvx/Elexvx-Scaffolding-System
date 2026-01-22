package com.tencent.tdesign.dto;

import jakarta.validation.constraints.Email;
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

  @NotBlank(message = "Name is required")
  @Size(max = 64, message = "Name length must be <= 64")
  private String name;

  @NotBlank(message = "Email is required")
  @Email(message = "Email format is invalid")
  @Size(max = 100, message = "Email length must be <= 100")
  private String email;

  @NotBlank(message = "Id card is required")
  @Pattern(regexp = "(^\\d{15}$)|(^\\d{17}[\\dXx]$)", message = "Id card format is invalid")
  @Size(max = 32, message = "Id card length must be <= 32")
  private String idCard;

  @NotBlank(message = "Mobile is required")
  @Size(max = 20, message = "Mobile length must be <= 20")
  @Pattern(regexp = "^1\\d{10}$", message = "Mobile format is invalid")
  private String mobile;

  @NotBlank(message = "Captcha id is required")
  private String captchaId;

  @NotBlank(message = "Captcha code is required")
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getIdCard() {
    return idCard;
  }

  public void setIdCard(String idCard) {
    this.idCard = idCard;
  }

  public String getMobile() {
    return mobile;
  }

  public void setMobile(String mobile) {
    this.mobile = mobile;
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
