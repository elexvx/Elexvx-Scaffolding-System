package com.tencent.tdesign.mapper;

import com.tencent.tdesign.entity.MessageEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MessageMapper {
  List<MessageEntity> listByUserId(@Param("userId") Long userId);
  MessageEntity selectById(@Param("id") String id);
  int insert(MessageEntity entity);
  int insertBatch(@Param("list") List<MessageEntity> list);
  int update(MessageEntity entity);
  int deleteById(@Param("id") String id);
}
