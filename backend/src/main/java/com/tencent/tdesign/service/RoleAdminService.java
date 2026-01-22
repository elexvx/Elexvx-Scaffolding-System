package com.tencent.tdesign.service;

import com.tencent.tdesign.dao.AuthQueryDao;
import com.tencent.tdesign.dto.RoleUpsertRequest;
import com.tencent.tdesign.entity.RoleEntity;
import com.tencent.tdesign.mapper.RoleMapper;
import com.tencent.tdesign.vo.RoleResponse;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class RoleAdminService {
  private final RoleMapper roleMapper;
  private final AuthQueryDao authDao;
  private final OperationLogService operationLogService;

  public RoleAdminService(RoleMapper roleMapper, AuthQueryDao authDao, OperationLogService operationLogService) {
    this.roleMapper = roleMapper;
    this.authDao = authDao;
    this.operationLogService = operationLogService;
  }

  public List<RoleResponse> list() {
    List<RoleEntity> roles = roleMapper.selectAll();
    List<RoleResponse> out = new ArrayList<>();
    for (RoleEntity r : roles) {
      RoleResponse rr = new RoleResponse();
      rr.setId(r.getId());
      rr.setName(r.getName());
      rr.setDescription(r.getDescription());
      rr.setPermissions(authDao.findPermissionsByRoleId(r.getId()));
      rr.setMenuIds(authDao.findMenuIdsByRoleId(r.getId()));
      out.add(rr);
    }
    return out;
  }

  public RoleResponse get(long id) {
    RoleEntity r = Optional.ofNullable(roleMapper.selectById(id)).orElseThrow(() -> new IllegalArgumentException("角色不存在"));
    RoleResponse rr = new RoleResponse();
    rr.setId(r.getId());
    rr.setName(r.getName());
    rr.setDescription(r.getDescription());
    rr.setPermissions(authDao.findPermissionsByRoleId(r.getId()));
    rr.setMenuIds(authDao.findMenuIdsByRoleId(r.getId()));
    return rr;
  }

  @Transactional
  public RoleResponse create(RoleUpsertRequest req) {
    if (roleMapper.countByName(req.getName()) > 0) throw new IllegalArgumentException("角色已存在");
    RoleEntity r = new RoleEntity();
    r.setName(req.getName());
    r.setDescription(req.getDescription());
    r = saveRole(r);
    authDao.replaceRolePermissions(r.getId(), req.getPermissions());
    authDao.replaceRoleMenus(r.getId(), req.getMenuIds());
    operationLogService.log("CREATE", "角色管理", "创建角色: " + r.getName());
    return get(r.getId());
  }

  @Transactional
  public RoleResponse update(long id, RoleUpsertRequest req) {
    RoleEntity r = Optional.ofNullable(roleMapper.selectById(id)).orElseThrow(() -> new IllegalArgumentException("角色不存在"));
    if ("admin".equals(r.getName()) && req.getName() != null && !"admin".equals(req.getName())) {
      throw new IllegalArgumentException("不允许修改 admin 角色名");
    }
    if (req.getName() != null && !req.getName().equals(r.getName()) && roleMapper.countByName(req.getName()) > 0) {
      throw new IllegalArgumentException("角色名已存在");
    }
    if (req.getName() != null) r.setName(req.getName());
    if (req.getDescription() != null) r.setDescription(req.getDescription());
    saveRole(r);
    if (req.getPermissions() != null) {
      authDao.replaceRolePermissions(r.getId(), req.getPermissions());
    }
    if (req.getMenuIds() != null) {
      authDao.replaceRoleMenus(r.getId(), req.getMenuIds());
    }
    operationLogService.log("UPDATE", "角色管理", "更新角色: " + r.getName());
    return get(id);
  }

  @Transactional
  public boolean delete(long id) {
    RoleEntity r = Optional.ofNullable(roleMapper.selectById(id)).orElseThrow(() -> new IllegalArgumentException("角色不存在"));
    if ("admin".equals(r.getName())) throw new IllegalArgumentException("不允许删除 admin 角色");
    roleMapper.deleteById(id);
    operationLogService.log("DELETE", "角色管理", "删除角色: " + r.getName());
    return true;
  }


  private RoleEntity saveRole(RoleEntity role) {
    if (role.getId() == null) {
      roleMapper.insert(role);
    } else {
      roleMapper.update(role);
    }
    return role;
  }

  @Transactional
  public void ensureAdminHasAllMenus() {
    authDao.ensureAdminHasAllMenus();
  }
}
