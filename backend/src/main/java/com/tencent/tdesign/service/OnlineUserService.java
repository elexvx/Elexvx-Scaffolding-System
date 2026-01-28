package com.tencent.tdesign.service;

import com.tencent.tdesign.security.AuthSession;
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
  private final AuthTokenService authTokenService;

  public OnlineUserService(AuthTokenService authTokenService) {
    this.authTokenService = authTokenService;
  }

  public PageResult<OnlineUserVO> getOnlineUsers(String loginAddress, String userName, int page, int size) {
    Set<String> tokenSet = new LinkedHashSet<>();
    tokenSet.addAll(authTokenService.listAllTokens());
    List<String> tokenList = new ArrayList<>(tokenSet);
    List<OnlineUserVO> allUsers = new ArrayList<>();
    logger.debug("Found {} tokens in system", tokenList.size());

    for (String token : tokenList) {
      try {
        AuthSession session = authTokenService.getSession(token);
        if (session == null) {
          logger.debug("No login ID found for token: {}", token);
          authTokenService.removeToken(token);
          continue;
        }

        // 从 session 中获取用户信息
        String userNameStr = (String) session.getAttributes().get("userName");
        String account = (String) session.getAttributes().get("account");
        String ipAddr = session.getIpAddress();
        String location = session.getLoginLocation();
        String browser = session.getBrowser();
        String os = session.getOs();
        Object loginTimeObj = session.getAttributes().get("loginTime");
        Long loginTimeMs = loginTimeObj instanceof Number ? ((Number) loginTimeObj).longValue() : null;

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
      authTokenService.removeToken(sessionId);
      return authTokenService.getSession(sessionId) == null;
    } catch (Exception e) {
      return false;
    }
  }

  public OnlineUserVO getOnlineUser(String sessionId) {
    try {
      AuthSession session = authTokenService.getSession(sessionId);
      if (session == null) return null;

      String userNameStr = (String) session.getAttributes().get("userName");
      String account = (String) session.getAttributes().get("account");
      String ipAddr = session.getIpAddress();
      String location = session.getLoginLocation();
      String browser = session.getBrowser();
      String os = session.getOs();

      OnlineUserVO user = new OnlineUserVO();
      user.setSessionId(sessionId);
      user.setLoginName(account != null ? account : "unknown");
      user.setUserName(userNameStr != null ? userNameStr : "unknown");
      user.setIpAddress(ipAddr != null ? ipAddr : "");
      user.setLoginLocation(location != null ? location : "");
      user.setBrowser(browser != null ? browser : "Unknown");
      user.setOs(os != null ? os : "Unknown");
      Object loginTimeObj = session.getAttributes().get("loginTime");
      Long loginTimeMs = loginTimeObj instanceof Number ? ((Number) loginTimeObj).longValue() : null;
      user.setLoginTime(loginTimeMs != null ? dateFormat.format(new Date(loginTimeMs)) : "");
      return user;
    } catch (Exception e) {
      return null;
    }
  }
}
