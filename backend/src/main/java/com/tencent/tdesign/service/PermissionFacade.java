package com.tencent.tdesign.service;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.tencent.tdesign.dao.AuthQueryDao;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class PermissionFacade {
  private static final String KEY_ASSUMED_ROLES = "assumedRoles";
  private static final String KEY_ASSUMED_PERMISSIONS = "assumedPermissions";

  private final AuthQueryDao authDao;

  public PermissionFacade(AuthQueryDao authDao) {
    this.authDao = authDao;
  }

  public List<String> getAssignedRoles(long userId) {
    return authDao.findRoleNamesByUserId(userId);
  }

  public List<String> getEffectiveRoles(long userId) {
    List<String> override = getAssumedRoles(userId);
    return override.isEmpty() ? authDao.findRoleNamesByUserId(userId) : override;
  }

  public List<String> getEffectivePermissions(long userId) {
    List<String> override = getAssumedPermissions(userId);
    return override.isEmpty() ? authDao.findPermissionsByUserId(userId) : override;
  }

  public List<Long> getAccessibleMenuIds(long userId) {
    List<String> override = getAssumedRoles(userId);
    if (!override.isEmpty()) {
      return authDao.findMenuIdsByRoleNames(override);
    }
    return authDao.findMenuIdsByUserId(userId);
  }

  public boolean isAdminAccount(long userId) {
    return getAssignedRoles(userId).stream().anyMatch(r -> "admin".equalsIgnoreCase(r));
  }

  public boolean isAssumed(long userId) {
    return !getAssumedRoles(userId).isEmpty();
  }

  public List<String> assumeRoles(long userId, List<String> roleNames) {
    List<String> normalized = normalizeRoles(roleNames);
    if (normalized.isEmpty()) throw new IllegalArgumentException("请选择要切换的角色");

    List<String> existing = authDao.findExistingRoleNames(normalized);
    if (existing.size() != normalized.size()) {
      Set<String> missing = new HashSet<>(normalized);
      missing.removeAll(existing);
      throw new IllegalArgumentException("以下角色不存在: " + String.join(", ", missing));
    }
    List<String> permissions = authDao.findPermissionsByRoleNames(normalized);
    SaSession session = StpUtil.getSessionByLoginId(userId);
    session.set(KEY_ASSUMED_ROLES, normalized);
    session.set(KEY_ASSUMED_PERMISSIONS, permissions);
    return normalized;
  }

  public void clearAssumedRoles(long userId) {
    SaSession session = StpUtil.getSessionByLoginId(userId, false);
    if (session == null) return;
    session.delete(KEY_ASSUMED_ROLES);
    session.delete(KEY_ASSUMED_PERMISSIONS);
  }

  private List<String> getAssumedRoles(long userId) {
    SaSession session = StpUtil.getSessionByLoginId(userId, false);
    if (session == null) return Collections.emptyList();
    Object value = session.get(KEY_ASSUMED_ROLES);
    if (value instanceof List<?>) {
      List<?> raw = (List<?>) value;
      List<String> list = new ArrayList<>();
      raw.forEach(it -> {
        if (it != null) list.add(String.valueOf(it));
      });
      return normalizeRoles(list);
    }
    return Collections.emptyList();
  }

  private List<String> getAssumedPermissions(long userId) {
    SaSession session = StpUtil.getSessionByLoginId(userId, false);
    if (session == null) return Collections.emptyList();
    Object value = session.get(KEY_ASSUMED_PERMISSIONS);
    if (value instanceof List<?>) {
      List<String> list = new ArrayList<>();
      for (Object it : (List<?>) value) {
        if (it != null) list.add(String.valueOf(it));
      }
      return list;
    }
    return Collections.emptyList();
  }

  private List<String> normalizeRoles(List<String> roleNames) {
    if (roleNames == null) return Collections.emptyList();
    Set<String> set = new HashSet<>();
    for (String r : roleNames) {
      if (r == null) continue;
      String v = r.trim();
      if (v.isEmpty()) continue;
      set.add(v);
    }
    List<String> list = new ArrayList<>();
    for (String r : set) list.add(r);
    list.sort(String.CASE_INSENSITIVE_ORDER);
    return list;
  }
}
