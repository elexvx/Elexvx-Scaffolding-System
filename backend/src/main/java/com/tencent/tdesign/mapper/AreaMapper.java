package com.tencent.tdesign.mapper;

import com.tencent.tdesign.entity.AreaEntity;
import com.tencent.tdesign.vo.AreaNodeRow;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AreaMapper {
  AreaEntity selectById(@Param("id") Integer id);

  List<AreaEntity> selectByParentAndName(@Param("parentId") Integer parentId, @Param("name") String name);

  List<AreaNodeRow> selectChildren(@Param("parentId") Integer parentId);
}
