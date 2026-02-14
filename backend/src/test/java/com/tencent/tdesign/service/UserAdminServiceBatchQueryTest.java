package com.tencent.tdesign.service;

import com.tencent.tdesign.dao.AuthQueryDao;
import com.tencent.tdesign.dto.UserIdLongValue;
import com.tencent.tdesign.dto.UserIdStringValue;
import com.tencent.tdesign.entity.UserEntity;
import com.tencent.tdesign.mapper.OrgUnitMapper;
import com.tencent.tdesign.mapper.RoleMapper;
import com.tencent.tdesign.mapper.UserDepartmentMapper;
import com.tencent.tdesign.mapper.UserMapper;
import com.tencent.tdesign.mapper.UserOrgUnitMapper;
import com.tencent.tdesign.security.AuthContext;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserAdminServiceBatchQueryTest {
  @Test
  void pageShouldUseBatchQueriesForRelatedData() {
    UserMapper userMapper = mock(UserMapper.class);
    RoleMapper roleMapper = mock(RoleMapper.class);
    AuthQueryDao authDao = mock(AuthQueryDao.class);
    OrgUnitMapper orgUnitMapper = mock(OrgUnitMapper.class);
    UserOrgUnitMapper userOrgUnitMapper = mock(UserOrgUnitMapper.class);
    UserDepartmentMapper userDepartmentMapper = mock(UserDepartmentMapper.class);
    OperationLogService operationLogService = mock(OperationLogService.class);
    PermissionFacade permissionFacade = mock(PermissionFacade.class);
    PasswordPolicyService passwordPolicyService = mock(PasswordPolicyService.class);
    AuthContext authContext = mock(AuthContext.class);
    AuthTokenService authTokenService = mock(AuthTokenService.class);

    UserAdminService service = new UserAdminService(
      userMapper,
      roleMapper,
      authDao,
      orgUnitMapper,
      userOrgUnitMapper,
      userDepartmentMapper,
      operationLogService,
      permissionFacade,
      passwordPolicyService,
      authContext,
      authTokenService,
      false
    );

    UserEntity u = new UserEntity();
    u.setId(100L);
    u.setAccount("u100");
    when(userMapper.selectPage(null, null, null, null, null, null, null, 0, 20)).thenReturn(List.of(u));
    when(userMapper.countByKeyword(null, null, null, null, null, null, null)).thenReturn(1L);

    UserIdStringValue role = new UserIdStringValue();
    role.setUserId(100L);
    role.setValue("user");
    when(authDao.findRoleNamesByUserIds(anyCollection())).thenReturn(List.of(role));

    when(orgUnitMapper.selectNamesByUserIds(anyCollection())).thenReturn(List.of());
    UserIdLongValue orgId = new UserIdLongValue();
    orgId.setUserId(100L);
    orgId.setValue(7L);
    when(orgUnitMapper.selectIdsByUserIds(anyCollection())).thenReturn(List.of(orgId));
    when(userDepartmentMapper.selectDepartmentNamesByUserIds(anyCollection())).thenReturn(List.of());
    when(userDepartmentMapper.selectDepartmentIdsByUserIds(anyCollection())).thenReturn(List.of());

    var result = service.page(null, null, null, null, null, null, null, 0, 20);

    assertEquals(1, result.getList().size());
    assertEquals(List.of("user"), result.getList().get(0).getRoles());
    assertEquals(List.of(7L), result.getList().get(0).getOrgUnitIds());

    verify(authDao).findRoleNamesByUserIds(anyCollection());
    verify(orgUnitMapper).selectNamesByUserIds(anyCollection());
    verify(orgUnitMapper).selectIdsByUserIds(anyCollection());
    verify(userDepartmentMapper).selectDepartmentNamesByUserIds(anyCollection());
    verify(userDepartmentMapper).selectDepartmentIdsByUserIds(anyCollection());

    verify(authDao, never()).findRoleNamesByUserId(anyLong());
    verify(orgUnitMapper, never()).selectNamesByUserId(anyLong());
    verify(orgUnitMapper, never()).selectIdsByUserId(anyLong());
    verify(userDepartmentMapper, never()).selectDepartmentNamesByUserId(anyLong());
    verify(userDepartmentMapper, never()).selectDepartmentIdsByUserId(anyLong());
  }
}
