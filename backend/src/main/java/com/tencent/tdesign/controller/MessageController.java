package com.tencent.tdesign.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.tencent.tdesign.annotation.RepeatSubmit;
import com.tencent.tdesign.dto.MessageSendRequest;
import com.tencent.tdesign.service.MessageService;
import com.tencent.tdesign.vo.ApiResponse;
import com.tencent.tdesign.vo.Message;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/message")
public class MessageController {
  private final MessageService service;
  public MessageController(MessageService service) { this.service = service; }

  @GetMapping("/list")
  public ApiResponse<List<Message>> list() {
    StpUtil.checkLogin();
    long userId = StpUtil.getLoginIdAsLong();
    return ApiResponse.success(service.list(userId));
  }

  @PostMapping
  @RepeatSubmit
  public ApiResponse<Message> send(@RequestBody @Validated MessageSendRequest req) {
    StpUtil.checkLogin();
    Message m = service.send(req.getToUserId(), req.getContent(), req.getType(), req.getQuality());
    return ApiResponse.success(m);
  }

  @PostMapping("/broadcast")
  @RepeatSubmit
  public ApiResponse<Integer> broadcast(@RequestBody @Validated com.tencent.tdesign.dto.AnnouncementRequest req) {
    StpUtil.checkLogin();
    int count = service.broadcast(req.getContent(), req.getType(), req.getQuality());
    return ApiResponse.success(count);
  }

  @PostMapping("/read")
  @RepeatSubmit
  public ApiResponse<Boolean> markRead(@RequestParam String id) {
    StpUtil.checkLogin();
    long userId = StpUtil.getLoginIdAsLong();
    return ApiResponse.success(service.markRead(userId, id, true));
  }

  @PostMapping("/unread")
  @RepeatSubmit
  public ApiResponse<Boolean> markUnread(@RequestParam String id) {
    StpUtil.checkLogin();
    long userId = StpUtil.getLoginIdAsLong();
    return ApiResponse.success(service.markRead(userId, id, false));
  }

  @PostMapping("/read-all")
  @RepeatSubmit
  public ApiResponse<Integer> readAll() {
    StpUtil.checkLogin();
    long userId = StpUtil.getLoginIdAsLong();
    return ApiResponse.success(service.markAllRead(userId));
  }

  @DeleteMapping("/{id}")
  @RepeatSubmit
  public ApiResponse<Boolean> delete(@PathVariable String id) {
    StpUtil.checkLogin();
    long userId = StpUtil.getLoginIdAsLong();
    return ApiResponse.success(service.delete(userId, id));
  }
}
