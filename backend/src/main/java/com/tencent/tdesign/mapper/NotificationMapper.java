package com.tencent.tdesign.mapper;

import com.tencent.tdesign.entity.Notification;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface NotificationMapper {
  Notification selectById(@Param("id") Long id);
  List<Notification> selectPageByStatus(@Param("statuses") List<String> statuses, @Param("offset") int offset, @Param("limit") int limit);
  long countByStatus(@Param("statuses") List<String> statuses);
  List<Notification> selectLatestPublished(@Param("limit") int limit);
  int insert(Notification entity);
  int update(Notification entity);
  int deleteById(@Param("id") Long id);
}
