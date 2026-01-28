package com.tencent.tdesign.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserDepartmentMapper {
  List<Long> selectDepartmentIdsByUserId(@Param("userId") Long userId);
  List<String> selectDepartmentNamesByUserId(@Param("userId") Long userId);
  int deleteByUserId(@Param("userId") Long userId);
  int insertUserDepartments(@Param("userId") Long userId, @Param("departmentIds") List<Long> departmentIds);
}
