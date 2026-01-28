package com.tencent.tdesign.mapper;

import com.tencent.tdesign.entity.OrgUnitEntity;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface OrgUnitMapper {
  OrgUnitEntity selectById(@Param("id") Long id);
  List<OrgUnitEntity> selectAll();
  List<OrgUnitEntity> selectByUserId(@Param("userId") Long userId);
  List<String> selectNamesByUserId(@Param("userId") Long userId);
  List<Long> selectIdsByUserId(@Param("userId") Long userId);
  List<OrgUnitEntity> selectByIds(@Param("ids") Collection<Long> ids);
  int insert(OrgUnitEntity entity);
  int update(OrgUnitEntity entity);
  int deleteById(@Param("id") Long id);
  long countChildren(@Param("id") Long id);
}
