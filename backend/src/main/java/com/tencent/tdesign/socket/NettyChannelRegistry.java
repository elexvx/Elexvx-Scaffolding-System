package com.tencent.tdesign.socket;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class NettyChannelRegistry {
  private final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
  private final Map<Channel, Long> channelUsers = new ConcurrentHashMap<>();

  public void register(Channel channel, Long userId) {
    channels.add(channel);
    if (userId != null) {
      channelUsers.put(channel, userId);
    }
  }

  public void unregister(Channel channel) {
    channels.remove(channel);
    channelUsers.remove(channel);
  }

  public ChannelGroup channels() {
    return channels;
  }

  public int size() {
    return channels.size();
  }
}
