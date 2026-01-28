package com.tencent.tdesign.controller;

import com.tencent.tdesign.annotation.RepeatSubmit;
import com.tencent.tdesign.dto.MessageSendRequest;
import com.tencent.tdesign.security.AuthContext;
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
  private final AuthContext authContext;

  public MessageController(MessageService service, AuthContext authContext) {
    this.service = service;
    this.authContext = authContext;
  }

  @GetMapping("/list")
  public ApiResponse<List<Message>> list() {
    long userId = authContext.requireUserId();
    return ApiResponse.success(service.list(userId));
  }

  @PostMapping
  @RepeatSubmit
  public ApiResponse<Message> send(@RequestBody @Validated MessageSendRequest req) {
    authContext.requireUserId();
    Message m = service.send(req.getToUserId(), req.getContent(), req.getType(), req.getQuality());
    return ApiResponse.success(m);
  }

  @PostMapping("/broadcast")
  @RepeatSubmit
  public ApiResponse<Integer> broadcast(@RequestBody @Validated com.tencent.tdesign.dto.AnnouncementRequest req) {
    authContext.requireUserId();
    int count = service.broadcast(req.getContent(), req.getType(), req.getQuality());
    return ApiResponse.success(count);
  }

  @PostMapping("/read")
  @RepeatSubmit
  public ApiResponse<Boolean> markRead(@RequestParam String id) {
    long userId = authContext.requireUserId();
    return ApiResponse.success(service.markRead(userId, id, true));
  }

  @PostMapping("/unread")
  @RepeatSubmit
  public ApiResponse<Boolean> markUnread(@RequestParam String id) {
    long userId = authContext.requireUserId();
    return ApiResponse.success(service.markRead(userId, id, false));
  }

  @PostMapping("/read-all")
  @RepeatSubmit
  public ApiResponse<Integer> readAll() {
    long userId = authContext.requireUserId();
    return ApiResponse.success(service.markAllRead(userId));
  }

  @DeleteMapping("/{id}")
  @RepeatSubmit
  public ApiResponse<Boolean> delete(@PathVariable String id) {
    long userId = authContext.requireUserId();
    return ApiResponse.success(service.delete(userId, id));
  }
}
