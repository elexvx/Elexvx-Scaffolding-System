package com.tencent.tdesign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SmsLoginRequest {
  @NotBlank(message = "Phone is required")
  @Pattern(regexp = "^[+0-9\\s-]{6,20}$", message = "Phone format is invalid")
  private String phone;

  @NotBlank(message = "Verification code is required")
  private String code;

  private Boolean force;

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public Boolean getForce() {
    return force;
  }

  public void setForce(Boolean force) {
    this.force = force;
  }
}
