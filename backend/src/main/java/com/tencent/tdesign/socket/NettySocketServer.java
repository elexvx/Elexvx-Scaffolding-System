package com.tencent.tdesign.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettySocketServer {
  private static final Logger log = LoggerFactory.getLogger(NettySocketServer.class);
  private final NettyServerProperties properties;
  private final NettyChannelRegistry registry;
  private final NettyAuthService authService;
  private final ObjectMapper objectMapper;
  private EventLoopGroup bossGroup;
  private EventLoopGroup workerGroup;
  private Channel serverChannel;

  public NettySocketServer(
    NettyServerProperties properties,
    NettyChannelRegistry registry,
    NettyAuthService authService,
    ObjectMapper objectMapper
  ) {
    this.properties = properties;
    this.registry = registry;
    this.authService = authService;
    this.objectMapper = objectMapper;
  }

  public void start() {
    if (!properties.isEnabled()) {
      log.info("Netty socket server disabled.");
      return;
    }
    bossGroup = new NioEventLoopGroup(1);
    workerGroup = new NioEventLoopGroup();
    ServerBootstrap bootstrap = new ServerBootstrap();
    bootstrap
      .group(bossGroup, workerGroup)
      .channel(NioServerSocketChannel.class)
      .childOption(ChannelOption.SO_KEEPALIVE, true)
      .childHandler(new ChannelInitializer<SocketChannel>() {
        @Override
        protected void initChannel(SocketChannel ch) {
          ch.pipeline()
            .addLast(new HttpServerCodec())
            .addLast(new HttpObjectAggregator(65536))
            .addLast(new IdleStateHandler(60, 0, 0))
            .addLast(new WebSocketServerProtocolHandler("/ws", null, true))
            .addLast(new NettyServerHandler(objectMapper, registry, authService));
        }
      });
    try {
      serverChannel = bootstrap.bind(properties.getPort()).sync().channel();
      log.info("Netty socket server started on port {}", properties.getPort());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("Netty socket server start interrupted.");
    }
  }

  public void stop() {
    if (serverChannel != null) {
      serverChannel.close();
    }
    if (bossGroup != null) {
      bossGroup.shutdownGracefully(1, 3, TimeUnit.SECONDS);
    }
    if (workerGroup != null) {
      workerGroup.shutdownGracefully(1, 3, TimeUnit.SECONDS);
    }
    log.info("Netty socket server stopped.");
  }
}
