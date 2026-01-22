package com.tencent.tdesign.mapper;

import com.tencent.tdesign.entity.UserEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
  UserEntity selectById(@Param("id") Long id);
  UserEntity selectByAccount(@Param("account") String account);
  UserEntity selectByMobile(@Param("mobile") String mobile);
  UserEntity selectByPhone(@Param("phone") String phone);
  UserEntity selectByEmail(@Param("email") String email);
  List<UserEntity> selectAll();
  List<Long> selectAllIds();
  List<UserEntity> selectPage(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);
  long countByKeyword(@Param("keyword") String keyword);
  long countByAccount(@Param("account") String account);
  long countByEmailIgnoreCase(@Param("email") String email);
  long countByIdCard(@Param("idCard") String idCard);
  int insert(UserEntity entity);
  int update(UserEntity entity);
  int deleteById(@Param("id") Long id);
}
