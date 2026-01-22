package com.tencent.tdesign.service;

import com.tencent.tdesign.config.RedisProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

  private final RedisProperties redisProperties;
  
  @Autowired(required = false)
  private RedisTemplate<String, Object> redisTemplate;
  private volatile String lastRedisError = "";

  public HealthService(RedisProperties redisProperties) {
    this.redisProperties = redisProperties;
  }

  public boolean isRedisEnabled() {
    return redisProperties.isEnabled();
  }

  public boolean isRedisAvailable() {
    // 只有当 Redis 启用且 RedisTemplate 存在时才检查可用性
    if (!redisProperties.isEnabled() || redisTemplate == null) {
      lastRedisError = "Redis 未启用或未配置连接";
      return false;
    }
    
    try {
      // 尝试执行一个简单的命令来检查连接
      var factory = redisTemplate.getConnectionFactory();
      if (factory == null) {
        lastRedisError = "Redis 连接工厂未初始化";
        return false;
      }
      factory.getConnection().ping();
      lastRedisError = "";
      return true;
    } catch (Exception e) {
      lastRedisError = e.getMessage();
      return false;
    }
  }

  public String getRedisMessage() {
    return lastRedisError;
  }
}
