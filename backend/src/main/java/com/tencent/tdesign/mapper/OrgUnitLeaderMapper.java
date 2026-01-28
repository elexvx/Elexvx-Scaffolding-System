package com.tencent.tdesign.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface OrgUnitLeaderMapper {
  List<Long> selectLeaderIds(@Param("orgUnitId") Long orgUnitId);
  int deleteByOrgUnitId(@Param("orgUnitId") Long orgUnitId);
  int insertLeaders(@Param("orgUnitId") Long orgUnitId, @Param("userIds") List<Long> userIds);
}
