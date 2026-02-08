package com.tencent.tdesign.dto;

import java.time.LocalDate;
import java.util.List;

public class UserUpdateRequest {
  private String name;
  private List<String> roles;

  private String mobile;
  private String email;
  private String idCard;
  private String seat;
  private String entity;
  private String leader;
  private String position;
  private LocalDate joinDay;
  private String team;
  private Integer status;
  private List<Long> orgUnitIds;
  private List<Long> departmentIds;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getRoles() {
    return roles;
  }

  public void setRoles(List<String> roles) {
    this.roles = roles;
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

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public List<Long> getOrgUnitIds() {
    return orgUnitIds;
  }

  public void setOrgUnitIds(List<Long> orgUnitIds) {
    this.orgUnitIds = orgUnitIds;
  }

  public List<Long> getDepartmentIds() {
    return departmentIds;
  }

  public void setDepartmentIds(List<Long> departmentIds) {
    this.departmentIds = departmentIds;
  }
}
