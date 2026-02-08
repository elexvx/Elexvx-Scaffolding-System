package com.tencent.tdesign.vo;

import com.tencent.tdesign.annotation.Sensitive;
import com.tencent.tdesign.enums.DesensitizedType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class UserListItem {
  private Long id;
  private String guid;
  private String account;
  private String name;
  @Sensitive(desensitizedType = DesensitizedType.PHONE)
  private String mobile;
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
  private List<Long> orgUnitIds;
  private List<String> orgUnitNames;
  private List<Long> departmentIds;
  private List<String> departmentNames;
  private Integer status;
  private LocalDateTime createdAt;

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

  public List<Long> getOrgUnitIds() {
    return orgUnitIds;
  }

  public void setOrgUnitIds(List<Long> orgUnitIds) {
    this.orgUnitIds = orgUnitIds;
  }

  public List<String> getOrgUnitNames() {
    return orgUnitNames;
  }

  public void setOrgUnitNames(List<String> orgUnitNames) {
    this.orgUnitNames = orgUnitNames;
  }

  public List<Long> getDepartmentIds() {
    return departmentIds;
  }

  public void setDepartmentIds(List<Long> departmentIds) {
    this.departmentIds = departmentIds;
  }

  public List<String> getDepartmentNames() {
    return departmentNames;
  }

  public void setDepartmentNames(List<String> departmentNames) {
    this.departmentNames = departmentNames;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
