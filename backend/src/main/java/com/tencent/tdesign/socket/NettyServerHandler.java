package com.tencent.tdesign.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
  private static final Logger log = LoggerFactory.getLogger(NettyServerHandler.class);
  private final ObjectMapper objectMapper;
  private final NettyChannelRegistry registry;
  private final NettyAuthService authService;

  public NettyServerHandler(ObjectMapper objectMapper, NettyChannelRegistry registry, NettyAuthService authService) {
    this.objectMapper = objectMapper;
    this.registry = registry;
    this.authService = authService;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    registry.register(ctx.channel(), null);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) {
    registry.unregister(ctx.channel());
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
    NettyPayload payload = objectMapper.readValue(msg.text(), NettyPayload.class);
    if (payload == null || payload.getType() == null) {
      return;
    }
    switch (payload.getType()) {
      case "auth" -> handleAuth(ctx.channel(), payload);
      case "ping" -> handlePing(ctx.channel());
      default -> log.debug("Unhandled netty payload type: {}", payload.getType());
    }
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
    if (evt instanceof IdleStateEvent event) {
      if (event.state() == IdleState.READER_IDLE) {
        ctx.close();
      }
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    log.warn("Netty socket error: {}", cause.getMessage());
    ctx.close();
  }

  private void handleAuth(Channel channel, NettyPayload payload) {
    Optional<Long> userId = authService.authenticate(payload.getToken());
    if (userId.isPresent()) {
      registry.register(channel, userId.get());
      send(channel, "auth_ok", "authorized");
    } else {
      send(channel, "auth_fail", "unauthorized");
      channel.close();
    }
  }

  private void handlePing(Channel channel) {
    send(channel, "pong", null);
  }

  private void send(Channel channel, String type, String message) {
    NettyPayload payload = new NettyPayload();
    payload.setType(type);
    payload.setMessage(message);
    payload.setTimestamp(Instant.now().toEpochMilli());
    try {
      channel.writeAndFlush(new TextWebSocketFrame(objectMapper.writeValueAsString(payload)));
    } catch (Exception e) {
      log.debug("Failed to write netty payload: {}", e.getMessage());
    }
  }
}
