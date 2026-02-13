package com.tencent.tdesign.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tdesign.security.AuthSession;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuthTokenService {
  private static final String TOKEN_KEY_PREFIX = "auth:token:";
  private static final String ALL_TOKENS_KEY = "auth:tokens";
  private static final String USER_TOKEN_PREFIX = "auth:user:";
  private static final String USER_TOKEN_SUFFIX = ":tokens";

  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper;

  public AuthTokenService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
  }

  public String createToken(long userId, AuthSession session, long expiresInSeconds) {
    String token = UUID.randomUUID().toString().replace("-", "");
    session.setToken(token);
    session.setUserId(userId);
    session.setIssuedAt(System.currentTimeMillis());
    session.setExpiresAt(System.currentTimeMillis() + expiresInSeconds * 1000L);
    storeSession(token, session, expiresInSeconds);
    addUserToken(userId, token, expiresInSeconds);
    return token;
  }

  public AuthSession getSession(String token) {
    if (token == null || token.isBlank()) return null;
    String payload = getValue(tokenKey(token));
    if (payload == null || payload.isBlank()) return null;
    try {
      return objectMapper.readValue(payload, AuthSession.class);
    } catch (JsonProcessingException e) {
      return null;
    }
  }

  public void updateSession(String token, AuthSession session) {
    if (token == null || token.isBlank() || session == null) return;
    Long ttl = redisTemplate.getExpire(tokenKey(token));
    long expiresIn = ttl != null && ttl > 0 ? ttl : 0L;
    if (expiresIn <= 0) return;
    storeSession(token, session, expiresIn);
  }

  public void removeToken(String token) {
    AuthSession session = getSession(token);
    if (session != null) {
      removeUserToken(session.getUserId(), token);
      return;
    }
    redisTemplate.delete(tokenKey(token));
    redisTemplate.opsForSet().remove(ALL_TOKENS_KEY, token);
  }

  public void removeUserToken(long userId, String token) {
    if (token == null || token.isBlank()) return;
    redisTemplate.delete(tokenKey(token));
    redisTemplate.opsForSet().remove(ALL_TOKENS_KEY, token);
    redisTemplate.opsForSet().remove(userKey(userId), token);
  }

  public void removeUserTokens(long userId) {
    Set<Object> tokens = redisTemplate.opsForSet().members(userKey(userId));
    if (tokens != null) {
      for (Object tokenObj : tokens) {
        if (tokenObj == null) continue;
        String token = String.valueOf(tokenObj);
        removeUserToken(userId, token);
      }
    }
    redisTemplate.delete(userKey(userId));
  }

  public List<String> listAllTokens() {
    Set<Object> tokens = redisTemplate.opsForSet().members(ALL_TOKENS_KEY);
    List<String> list = new ArrayList<>();
    if (tokens != null) {
      for (Object tokenObj : tokens) {
        if (tokenObj == null) continue;
        list.add(String.valueOf(tokenObj));
      }
    }
    return list;
  }

  public List<String> listUserTokens(long userId) {
    Set<Object> tokens = redisTemplate.opsForSet().members(userKey(userId));
    List<String> list = new ArrayList<>();
    if (tokens != null) {
      for (Object tokenObj : tokens) {
        if (tokenObj == null) continue;
        list.add(String.valueOf(tokenObj));
      }
    }
    return list;
  }

  private void storeSession(String token, AuthSession session, long expiresInSeconds) {
    try {
      String payload = objectMapper.writeValueAsString(session);
      redisTemplate.opsForValue().set(tokenKey(token), payload, Duration.ofSeconds(expiresInSeconds));
    } catch (JsonProcessingException ignored) {
    }
  }

  private void addUserToken(long userId, String token, long expiresInSeconds) {
    redisTemplate.opsForSet().add(userKey(userId), token);
    redisTemplate.opsForSet().add(ALL_TOKENS_KEY, token);
    Long currentTtl = redisTemplate.getExpire(userKey(userId));
    if (currentTtl == null || currentTtl < expiresInSeconds) {
      redisTemplate.expire(userKey(userId), Duration.ofSeconds(expiresInSeconds));
    }
  }

  private String getValue(String key) {
    Object value = redisTemplate.opsForValue().get(key);
    return value == null ? null : String.valueOf(value);
  }

  private String tokenKey(String token) {
    return TOKEN_KEY_PREFIX + token;
  }

  private String userKey(long userId) {
    return USER_TOKEN_PREFIX + userId + USER_TOKEN_SUFFIX;
  }
}
