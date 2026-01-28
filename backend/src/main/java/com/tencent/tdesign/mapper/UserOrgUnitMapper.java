package com.tencent.tdesign.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserOrgUnitMapper {
  List<Long> selectOrgUnitIdsByUserId(@Param("userId") Long userId);
  int deleteByUserId(@Param("userId") Long userId);
  int insertUserOrgUnits(@Param("userId") Long userId, @Param("orgUnitIds") List<Long> orgUnitIds);
}
