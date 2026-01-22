package com.tencent.tdesign.vo;

public class LoginResponse {
  private String status;
  private String token;
  private Long expiresIn;
  private String requestId;
  private String requestKey;

  public LoginResponse() {}

  public static LoginResponse success(String token, long expiresIn) {
    LoginResponse r = new LoginResponse();
    r.setStatus("ok");
    r.setToken(token);
    r.setExpiresIn(expiresIn);
    return r;
  }

  public static LoginResponse pending(String requestId, String requestKey) {
    LoginResponse r = new LoginResponse();
    r.setStatus("pending");
    r.setRequestId(requestId);
    r.setRequestKey(requestKey);
    return r;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public Long getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(Long expiresIn) {
    this.expiresIn = expiresIn;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public String getRequestKey() {
    return requestKey;
  }

  public void setRequestKey(String requestKey) {
    this.requestKey = requestKey;
  }
}
