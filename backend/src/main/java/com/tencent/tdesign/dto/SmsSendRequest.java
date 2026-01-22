package com.tencent.tdesign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SmsSendRequest {
  @NotBlank(message = "Phone is required")
  @Pattern(regexp = "^[+0-9\\s-]{6,20}$", message = "Phone format is invalid")
  private String phone;

  private String provider;

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }
}
