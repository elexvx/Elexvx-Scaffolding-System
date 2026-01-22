package com.tencent.tdesign.service;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.tencent.tdesign.vo.OnlineUserVO;
import com.tencent.tdesign.vo.PageResult;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OnlineUserService {

  private static final Logger logger = LoggerFactory.getLogger(OnlineUserService.class);
  private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private final OnlineUserTokenRegistry tokenRegistry;

  public OnlineUserService(OnlineUserTokenRegistry tokenRegistry) {
    this.tokenRegistry = tokenRegistry;
  }

  public PageResult<OnlineUserVO> getOnlineUsers(String loginAddress, String userName, int page, int size) {
    Set<String> tokenSet = new LinkedHashSet<>();
    try {
      tokenSet.addAll(StpUtil.searchTokenValue("", 0, -1, false));
    } catch (Exception e) {
      logger.debug("searchTokenValue not available: {}", e.getMessage());
    }
    tokenSet.addAll(tokenRegistry.snapshotTokens());
    List<String> tokenList = new ArrayList<>(tokenSet);
    List<OnlineUserVO> allUsers = new ArrayList<>();
    logger.debug("Found {} tokens in system", tokenList.size());

    for (String token : tokenList) {
      try {
        // 通过 token 获取登录用户ID
        Object loginIdObj = StpUtil.getLoginIdByToken(token);
        if (loginIdObj == null) {
          logger.debug("No login ID found for token: {}", token);
          continue;
        }

        // 通过登录用户ID获取session
        SaSession session = StpUtil.getSessionByLoginId(loginIdObj, false);
        if (session == null) {
          logger.debug("No session found for loginId: {}", loginIdObj);
          continue;
        }

        // 从 session 中获取用户信息
        String userNameStr = (String) session.get("userName");
        String account = (String) session.get("account");
        String ipAddr = (String) session.get("ipAddress");
        String location = (String) session.get("loginLocation");
        String browser = (String) session.get("browser");
        String os = (String) session.get("os");
        Long loginTimeMs = session.getCreateTime();

        // 过滤条件：登录地址
        if (loginAddress != null && !loginAddress.isEmpty()) {
          if (ipAddr == null || !ipAddr.contains(loginAddress)) {
            continue;
          }
        }

        // 过滤条件：用户名称
        if (userName != null && !userName.isEmpty()) {
          if (userNameStr == null || !userNameStr.contains(userName)) {
            continue;
          }
        }

        OnlineUserVO user = new OnlineUserVO();
        user.setSessionId(token);
        user.setLoginName(account != null ? account : "unknown");
        user.setUserName(userNameStr != null ? userNameStr : "unknown");
        user.setIpAddress(ipAddr != null ? ipAddr : "");
        user.setLoginLocation(location != null ? location : "");
        user.setBrowser(browser != null ? browser : "Unknown");
        user.setOs(os != null ? os : "Unknown");
        user.setLoginTime(loginTimeMs != null ? dateFormat.format(new Date(loginTimeMs)) : "");

        allUsers.add(user);
        logger.debug("Added online user: {}", userNameStr);
      } catch (Exception e) {
        logger.warn("Error processing token {}: {}", token, e.getMessage());
      }
    }

    logger.info("Total online users found: {}", allUsers.size());

    // 分页处理
    int start = page * size;
    int end = Math.min(start + size, allUsers.size());
    List<OnlineUserVO> pageList = start < allUsers.size() ? allUsers.subList(start, end) : new ArrayList<>();

    PageResult<OnlineUserVO> result = new PageResult<>();
    result.setList(pageList);
    result.setTotal(allUsers.size());
    return result;
  }

  public boolean forceLogout(String sessionId) {
    try {
      StpUtil.kickoutByTokenValue(sessionId);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public OnlineUserVO getOnlineUser(String sessionId) {
    try {
      Object loginIdObj = StpUtil.getLoginIdByToken(sessionId);
      if (loginIdObj == null) return null;
      SaSession session = StpUtil.getSessionByLoginId(loginIdObj, false);
      if (session == null) return null;

      String userNameStr = (String) session.get("userName");
      String account = (String) session.get("account");
      String ipAddr = (String) session.get("ipAddress");
      String location = (String) session.get("loginLocation");
      String browser = (String) session.get("browser");
      String os = (String) session.get("os");

      OnlineUserVO user = new OnlineUserVO();
      user.setSessionId(sessionId);
      user.setLoginName(account != null ? account : "unknown");
      user.setUserName(userNameStr != null ? userNameStr : "unknown");
      user.setIpAddress(ipAddr != null ? ipAddr : "");
      user.setLoginLocation(location != null ? location : "");
      user.setBrowser(browser != null ? browser : "Unknown");
      user.setOs(os != null ? os : "Unknown");
      Long loginTimeMs = session.getCreateTime();
      user.setLoginTime(loginTimeMs != null ? dateFormat.format(new Date(loginTimeMs)) : "");
      return user;
    } catch (Exception e) {
      return null;
    }
  }
}
