package com.tencent.tdesign.vo;

import com.tencent.tdesign.annotation.Sensitive;
import com.tencent.tdesign.enums.DesensitizedType;
import java.time.LocalDate;
import java.util.List;

public class UserListItem {
  private Long id;
  private String guid;
  private String account;
  private String name;
  @Sensitive(desensitizedType = DesensitizedType.PHONE)
  private String mobile;
  @Sensitive(desensitizedType = DesensitizedType.PHONE)
  private String phone;
  @Sensitive(desensitizedType = DesensitizedType.EMAIL)
  private String email;
  @Sensitive(desensitizedType = DesensitizedType.ID_CARD)
  private String idCard;
  private String seat;
  private String entity;
  private String leader;
  private String position;
  private LocalDate joinDay;
  private String team;
  private List<String> roles;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getGuid() {
    return guid;
  }

  public void setGuid(String guid) {
    this.guid = guid;
  }

  public String getAccount() {
    return account;
  }

  public void setAccount(String account) {
    this.account = account;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getMobile() {
    return mobile;
  }

  public void setMobile(String mobile) {
    this.mobile = mobile;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
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

  public String getSeat() {
    return seat;
  }

  public void setSeat(String seat) {
    this.seat = seat;
  }

  public String getEntity() {
    return entity;
  }

  public void setEntity(String entity) {
    this.entity = entity;
  }

  public String getLeader() {
    return leader;
  }

  public void setLeader(String leader) {
    this.leader = leader;
  }

  public String getPosition() {
    return position;
  }

  public void setPosition(String position) {
    this.position = position;
  }

  public LocalDate getJoinDay() {
    return joinDay;
  }

  public void setJoinDay(LocalDate joinDay) {
    this.joinDay = joinDay;
  }

  public String getTeam() {
    return team;
  }

  public void setTeam(String team) {
    this.team = team;
  }

  public List<String> getRoles() {
    return roles;
  }

  public void setRoles(List<String> roles) {
    this.roles = roles;
  }
}
