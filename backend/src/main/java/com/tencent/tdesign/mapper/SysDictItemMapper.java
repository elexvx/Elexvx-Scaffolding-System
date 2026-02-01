package com.tencent.tdesign.mapper;

import com.tencent.tdesign.entity.SysDictItem;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SysDictItemMapper {
  List<SysDictItem> selectPage(
    @Param("dictId") Long dictId,
    @Param("keyword") String keyword,
    @Param("status") Integer status,
    @Param("offset") int offset,
    @Param("limit") int limit
  );

  long countByDictId(
    @Param("dictId") Long dictId,
    @Param("keyword") String keyword,
    @Param("status") Integer status
  );

  List<SysDictItem> selectByDictId(@Param("dictId") Long dictId);

  List<SysDictItem> selectEnabledByDictCode(@Param("code") String code);

  SysDictItem selectById(@Param("id") Long id);

  SysDictItem selectByDictValue(@Param("dictId") Long dictId, @Param("value") String value);

  int insert(SysDictItem item);

  int update(SysDictItem item);

  int deleteById(@Param("id") Long id);

  int deleteByDictId(@Param("dictId") Long dictId);
}
