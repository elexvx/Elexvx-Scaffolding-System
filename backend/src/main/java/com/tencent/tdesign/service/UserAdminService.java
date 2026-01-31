package com.tencent.tdesign.service;

import com.tencent.tdesign.dao.AuthQueryDao;
import com.tencent.tdesign.dto.UserCreateRequest;
import com.tencent.tdesign.dto.UserUpdateRequest;
import com.tencent.tdesign.entity.UserEntity;
import com.tencent.tdesign.mapper.OrgUnitMapper;
import com.tencent.tdesign.mapper.RoleMapper;
import com.tencent.tdesign.mapper.UserDepartmentMapper;
import com.tencent.tdesign.mapper.UserMapper;
import com.tencent.tdesign.mapper.UserOrgUnitMapper;
import com.tencent.tdesign.security.AuthContext;
import com.tencent.tdesign.util.SensitiveMaskUtil;
import com.tencent.tdesign.vo.PageResult;
import com.tencent.tdesign.vo.UserListItem;
import com.tencent.tdesign.annotation.AiFunction;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class UserAdminService {
  private static final String ROOT_ADMIN_ACCOUNT = "admin";
  private final UserMapper userMapper;
  private final RoleMapper roleMapper;
  private final AuthQueryDao authDao;
  private final OrgUnitMapper orgUnitMapper;
  private final UserOrgUnitMapper userOrgUnitMapper;
  private final UserDepartmentMapper userDepartmentMapper;
  private final OperationLogService operationLogService;
  private final PermissionFacade permissionFacade;
  private final PasswordPolicyService passwordPolicyService;
  private final AuthContext authContext;

  public UserAdminService(
    UserMapper userMapper,
    RoleMapper roleMapper,
    AuthQueryDao authDao,
    OrgUnitMapper orgUnitMapper,
    UserOrgUnitMapper userOrgUnitMapper,
    UserDepartmentMapper userDepartmentMapper,
    OperationLogService operationLogService,
    PermissionFacade permissionFacade,
    PasswordPolicyService passwordPolicyService,
    AuthContext authContext
  ) {
    this.userMapper = userMapper;
    this.roleMapper = roleMapper;
    this.authDao = authDao;
    this.orgUnitMapper = orgUnitMapper;
    this.userOrgUnitMapper = userOrgUnitMapper;
    this.userDepartmentMapper = userDepartmentMapper;
    this.operationLogService = operationLogService;
    this.permissionFacade = permissionFacade;
    this.passwordPolicyService = passwordPolicyService;
    this.authContext = authContext;
  }

  @AiFunction(
    name = "queryUser",
    description = "查询用户列表，支持按账号或姓名模糊搜索",
    requiredPermissions = { "system:SystemUser:query" }
  )
  public PageResult<UserListItem> page(
    String keyword,
    String mobile,
    Long orgUnitId,
    Long departmentId,
    Integer status,
    java.time.LocalDateTime startTime,
    java.time.LocalDateTime endTime,
    int page,
    int size
  ) {
    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 1), 200);
    int offset = safePage * safeSize;
    String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
    String mobileKeyword = (mobile == null || mobile.isBlank()) ? null : mobile.trim();
    List<UserEntity> rows =
      userMapper.selectPage(kw, mobileKeyword, orgUnitId, departmentId, status, startTime, endTime, offset, safeSize);
    long total = userMapper.countByKeyword(kw, mobileKeyword, orgUnitId, departmentId, status, startTime, endTime);
    List<UserListItem> list = new ArrayList<>();
    for (UserEntity u : rows) {
      UserListItem item = toListItem(u);
      item.setRoles(authDao.findRoleNamesByUserId(Objects.requireNonNull(u.getId())));
      item.setOrgUnitNames(orgUnitMapper.selectNamesByUserId(u.getId()));
      item.setOrgUnitIds(orgUnitMapper.selectIdsByUserId(u.getId()));
      item.setDepartmentNames(userDepartmentMapper.selectDepartmentNamesByUserId(u.getId()));
      item.setDepartmentIds(userDepartmentMapper.selectDepartmentIdsByUserId(u.getId()));
      list.add(item);
    }
    return new PageResult<>(list, total);
  }

  @AiFunction(
    name = "getUserDetail",
    description = "获取特定用户的详细信息",
    requiredPermissions = { "system:SystemUser:query" }
  )
  public UserListItem get(long id) {
    UserEntity u = Optional.ofNullable(userMapper.selectById(id)).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    UserListItem item = toListItem(u);
    item.setRoles(authDao.findRoleNamesByUserId(id));
    item.setOrgUnitNames(orgUnitMapper.selectNamesByUserId(id));
    item.setOrgUnitIds(orgUnitMapper.selectIdsByUserId(id));
    item.setDepartmentNames(userDepartmentMapper.selectDepartmentNamesByUserId(id));
    item.setDepartmentIds(userDepartmentMapper.selectDepartmentIdsByUserId(id));
    return item;
  }

  @Transactional
  public UserListItem create(UserCreateRequest req) {
    if (userMapper.countByAccount(req.getAccount()) > 0) {
      throw new IllegalArgumentException("账号已存在");
    }
    UserEntity u = new UserEntity();
    u.setAccount(req.getAccount());
    u.setGuid(java.util.UUID.randomUUID().toString());
    u.setName(req.getName());
    u.setMobile(req.getMobile());
    u.setPhone(req.getPhone());
    u.setEmail(req.getEmail());
    u.setIdCard(req.getIdCard());
    u.setSeat(req.getSeat());
    u.setEntity(req.getEntity());
    u.setLeader(req.getLeader());
    u.setPosition(req.getPosition());
    u.setJoinDay(req.getJoinDay());
    u.setTeam(req.getTeam());
    u.setStatus(req.getStatus() == null ? 1 : req.getStatus());

    String pwd = (req.getPassword() == null || req.getPassword().isBlank()) ? "123456" : req.getPassword();
    // 创建账号时也使用统一密码规范校验
    passwordPolicyService.validate(pwd);
    u.setPasswordHash(BCrypt.hashpw(pwd, BCrypt.gensalt()));
    u = saveUser(u);

    List<String> normalizedRoles = normalizeRoles(req.getRoles());
    List<String> roles = normalizedRoles.isEmpty() ? List.of("user") : normalizedRoles;
    ensureRolesExist(roles);
    authDao.replaceUserRoles(u.getId(), roles);
    replaceOrgUnits(u.getId(), req.getOrgUnitIds());
    replaceDepartments(u.getId(), req.getDepartmentIds());

    operationLogService.log("CREATE", "用户管理", "创建用户: " + u.getAccount());
    return get(u.getId());
  }

  @Transactional
  public UserListItem update(long id, UserUpdateRequest req) {
    UserEntity u = Optional.ofNullable(userMapper.selectById(id)).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    ensureManageableTarget(u, true);
    if (req.getName() != null) u.setName(req.getName());
    if (req.getMobile() != null && !SensitiveMaskUtil.isMasked(req.getMobile())) u.setMobile(req.getMobile());
    if (req.getPhone() != null && !SensitiveMaskUtil.isMasked(req.getPhone())) u.setPhone(req.getPhone());
    if (req.getEmail() != null && !SensitiveMaskUtil.isMasked(req.getEmail())) u.setEmail(req.getEmail());
    if (req.getIdCard() != null && !SensitiveMaskUtil.isMasked(req.getIdCard())) u.setIdCard(req.getIdCard());
    if (req.getSeat() != null) u.setSeat(req.getSeat());
    if (req.getEntity() != null) u.setEntity(req.getEntity());
    if (req.getLeader() != null) u.setLeader(req.getLeader());
    if (req.getPosition() != null) u.setPosition(req.getPosition());
    if (req.getJoinDay() != null) u.setJoinDay(req.getJoinDay());
    if (req.getTeam() != null) u.setTeam(req.getTeam());
    if (req.getStatus() != null) u.setStatus(req.getStatus());
    ensureGuid(u);
    saveUser(u);

    if (req.getRoles() != null) {
      List<String> roles = normalizeRoles(req.getRoles());
      if (roles.isEmpty()) {
        throw new IllegalArgumentException("用户必须至少拥有一个角色");
      }
      ensureRolesExist(roles);
      authDao.replaceUserRoles(id, roles);
    }
    if (req.getOrgUnitIds() != null) {
      replaceOrgUnits(id, req.getOrgUnitIds());
    }
    if (req.getDepartmentIds() != null) {
      replaceDepartments(id, req.getDepartmentIds());
    }
    operationLogService.log("UPDATE", "用户管理", "更新用户: " + u.getAccount());
    return get(id);
  }

  @Transactional
  public boolean delete(long id) {
    UserEntity u = userMapper.selectById(id);
    if (u == null) return true;
    ensureManageableTarget(u);
    userMapper.deleteById(id);
    operationLogService.log("DELETE", "用户管理", "删除用户: " + u.getAccount());
    return true;
  }

  @Transactional
  public boolean resetPassword(long id, String newPassword) {
    UserEntity u = Optional.ofNullable(userMapper.selectById(id)).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    ensureManageableTarget(u);
    String pwd = (newPassword == null || newPassword.isBlank()) ? "123456" : newPassword;
    // 重置密码也遵循统一的密码规范
    passwordPolicyService.validate(pwd);
    u.setPasswordHash(BCrypt.hashpw(pwd, BCrypt.gensalt()));
    saveUser(u);
    operationLogService.log("UPDATE", "用户管理", "重置密码: " + u.getAccount());
    return true;
  }

  private List<String> normalizeRoles(List<String> roles) {
    if (roles == null) return List.of();
    List<String> normalized = new ArrayList<>();
    for (String role : roles) {
      if (role == null) continue;
      String value = role.trim();
      if (!value.isEmpty()) normalized.add(value);
    }
    return normalized;
  }

  private void ensureRolesExist(List<String> roles) {
    for (String role : roles) {
      if (role == null || role.isBlank()) continue;
      if (roleMapper.countByName(role) == 0) {
        throw new IllegalArgumentException("角色不存在: " + role);
      }
    }
  }

  static void validateManageableTarget(
    long currentUserId,
    long targetUserId,
    String targetAccount,
    boolean targetIsAdminRole,
    boolean currentIsAdminRole,
    boolean allowRootAdmin
  ) {
    if (!allowRootAdmin && targetAccount != null && targetAccount.equalsIgnoreCase(ROOT_ADMIN_ACCOUNT)) {
      throw new IllegalArgumentException("禁止操作系统管理员账号");
    }
    if (currentUserId == targetUserId) return;
    if (targetIsAdminRole && !currentIsAdminRole) {
      throw new IllegalArgumentException("禁止操作管理员账号");
    }
  }

  private void ensureManageableTarget(UserEntity targetUser, boolean allowRootAdmin) {
    long currentUserId = authContext.requireUserId();
    long targetUserId = Objects.requireNonNull(targetUser.getId());
    boolean targetIsAdmin = permissionFacade.isAdminAccount(targetUserId);
    boolean currentIsAdmin = permissionFacade.isAdminAccount(currentUserId);
    validateManageableTarget(currentUserId, targetUserId, targetUser.getAccount(), targetIsAdmin, currentIsAdmin, allowRootAdmin);
  }

  private void ensureManageableTarget(UserEntity targetUser) {
    ensureManageableTarget(targetUser, false);
  }

  private UserListItem toListItem(UserEntity u) {
    ensureGuid(u);
    UserListItem item = new UserListItem();
    item.setId(u.getId());
    item.setGuid(u.getGuid());
    item.setAccount(u.getAccount());
    item.setName(u.getName());
    item.setMobile(u.getMobile());
    item.setPhone(u.getPhone());
    item.setEmail(u.getEmail());
    item.setIdCard(u.getIdCard());
    item.setSeat(u.getSeat());
    item.setEntity(u.getEntity());
    item.setLeader(u.getLeader());
    item.setPosition(u.getPosition());
    item.setJoinDay(u.getJoinDay());
    item.setTeam(u.getTeam());
    item.setStatus(u.getStatus());
    item.setCreatedAt(u.getCreatedAt());
    return item;
  }

  private void replaceOrgUnits(Long userId, List<Long> orgUnitIds) {
    userOrgUnitMapper.deleteByUserId(userId);
    if (orgUnitIds == null || orgUnitIds.isEmpty()) return;
    List<Long> cleaned = orgUnitIds.stream().filter(Objects::nonNull).distinct().toList();
    if (!cleaned.isEmpty()) {
      userOrgUnitMapper.insertUserOrgUnits(userId, cleaned);
    }
  }

  private void replaceDepartments(Long userId, List<Long> departmentIds) {
    userDepartmentMapper.deleteByUserId(userId);
    if (departmentIds == null || departmentIds.isEmpty()) return;
    List<Long> cleaned = departmentIds.stream().filter(Objects::nonNull).distinct().toList();
    if (!cleaned.isEmpty()) {
      userDepartmentMapper.insertUserDepartments(userId, cleaned);
    }
  }

  private UserEntity saveUser(UserEntity user) {
    if (user.getId() == null) {
      userMapper.insert(user);
    } else {
      userMapper.update(user);
    }
    return user;
  }

  private void ensureGuid(UserEntity user) {
    if (user.getGuid() != null && !user.getGuid().isBlank()) return;
    user.setGuid(java.util.UUID.randomUUID().toString());
    saveUser(user);
  }
}
