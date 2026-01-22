package com.tencent.tdesign.service;

import com.tencent.tdesign.entity.MessageEntity;
import com.tencent.tdesign.mapper.MessageMapper;
import com.tencent.tdesign.mapper.UserMapper;
import com.tencent.tdesign.vo.Message;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService {
  private final MessageMapper messageMapper;
  private final UserMapper userMapper;
  private final OperationLogService operationLogService;
  private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  public MessageService(MessageMapper messageMapper, UserMapper userMapper, OperationLogService operationLogService) {
    this.messageMapper = messageMapper;
    this.userMapper = userMapper;
    this.operationLogService = operationLogService;
  }

  @Transactional
  public int broadcast(String content, String type, String quality) {
    List<Long> userIds = userMapper.selectAllIds();
    LocalDateTime now = LocalDateTime.now();
    List<MessageEntity> list = userIds.stream().map(id -> {
      MessageEntity e = new MessageEntity();
      e.setId(UUID.randomUUID().toString());
      e.setToUserId(id);
      e.setContent(content);
      e.setType(type);
      e.setStatus(true);
      e.setCollected(false);
      e.setQuality(quality);
      e.setCreatedAt(now);
      return e;
    }).collect(Collectors.toList());
    if (!list.isEmpty()) {
      messageMapper.insertBatch(list);
    }
    operationLogService.log("SEND", "消息发送", "广播消息发送成功，数量: " + list.size());
    return list.size();
  }

  @Transactional(readOnly = true)
  public List<Message> list(Long userId) {
    return messageMapper.listByUserId(userId).stream()
      .map(this::toVo)
      .collect(Collectors.toList());
  }

  @Transactional
  public Message send(Long toUserId, String content, String type, String quality) {
    MessageEntity e = new MessageEntity();
    e.setId(UUID.randomUUID().toString());
    e.setToUserId(toUserId);
    e.setContent(content);
    e.setType(type);
    e.setStatus(true);
    e.setCollected(false);
    e.setQuality(quality);
    e.setCreatedAt(LocalDateTime.now());
    messageMapper.insert(e);
    operationLogService.log("SEND", "消息发送", "发送消息给用户: " + toUserId);
    return toVo(e);
  }

  @Transactional
  public boolean markRead(Long userId, String id, boolean read) {
    MessageEntity e = messageMapper.selectById(id);
    if (e == null) return false;
    e.setStatus(!read);
    messageMapper.update(e);
    return true;
  }

  @Transactional
  public int markAllRead(Long userId) {
    List<MessageEntity> list = messageMapper.listByUserId(userId);
    int count = 0;
    for (MessageEntity e : list) {
      if (e.isStatus()) {
        e.setStatus(false);
        messageMapper.update(e);
        count++;
      }
    }
    return count;
  }

  @Transactional
  public boolean delete(Long userId, String id) {
    MessageEntity e = messageMapper.selectById(id);
    if (e == null) return false;
    messageMapper.deleteById(id);
    return true;
  }

  private Message toVo(MessageEntity e) {
    return new Message(e.getId(), e.getContent(), e.getType(), e.isStatus(), e.isCollected(), fmt.format(e.getCreatedAt()), e.getQuality());
  }
}
