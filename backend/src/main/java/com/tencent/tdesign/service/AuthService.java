package com.tencent.tdesign.service;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.tencent.tdesign.dao.AuthQueryDao;
import com.tencent.tdesign.dto.LoginRequest;
import com.tencent.tdesign.dto.SmsLoginRequest;
import com.tencent.tdesign.dto.SmsSendRequest;
import com.tencent.tdesign.dto.EmailLoginRequest;
import com.tencent.tdesign.dto.EmailSendRequest;
import com.tencent.tdesign.dto.RegisterRequest;
import com.tencent.tdesign.dto.RoleSwitchRequest;
import com.tencent.tdesign.dto.UserProfileUpdateRequest;
import com.tencent.tdesign.dto.ChangePasswordRequest;
import com.tencent.tdesign.dto.ForgotPasswordRequest;
import com.tencent.tdesign.entity.UserEntity;
import com.tencent.tdesign.entity.VerificationSetting;
import com.tencent.tdesign.mapper.OrgUnitMapper;
import com.tencent.tdesign.mapper.UserMapper;
import com.tencent.tdesign.mapper.RoleMapper;
import com.tencent.tdesign.vo.LoginResponse;
import com.tencent.tdesign.vo.SmsSendResponse;
import com.tencent.tdesign.vo.UserInfoResponse;
import com.tencent.tdesign.vo.UserProfileResponse;
import com.tencent.tdesign.util.SensitiveMaskUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private final UserMapper userMapper;
  private final OrgUnitMapper orgUnitMapper;
  private final UiSettingService uiSettingService;
  private final RoleMapper roleMapper;
  private final AuthQueryDao authDao;
  private final CaptchaService captchaService;
  private final SmsCodeService smsCodeService;
  private final HttpServletRequest request;
  private final ConcurrentLoginService concurrentLoginService;
  private final OperationLogService operationLogService;
  private final PermissionFacade permissionFacade;
  private final PasswordPolicyService passwordPolicyService;
  private final ObjectStorageService storageService;
  private final SmsSenderService smsSenderService;
  private final EmailSenderService emailSenderService;
  private final EmailCodeService emailCodeService;
  private final VerificationSettingService verificationSettingService;
  private final SecuritySettingService securitySettingService;

  public AuthService(
    UserMapper userMapper,
    OrgUnitMapper orgUnitMapper,
      UiSettingService uiSettingService,
      RoleMapper roleMapper,
      AuthQueryDao authDao,
      CaptchaService captchaService,
      SmsCodeService smsCodeService,
      HttpServletRequest request,
      ConcurrentLoginService concurrentLoginService,
      OperationLogService operationLogService,
      PermissionFacade permissionFacade,
      PasswordPolicyService passwordPolicyService,
      ObjectStorageService storageService,
      SmsSenderService smsSenderService,
      EmailSenderService emailSenderService,
      EmailCodeService emailCodeService,
      VerificationSettingService verificationSettingService,
      SecuritySettingService securitySettingService) {
    this.userMapper = userMapper;
    this.orgUnitMapper = orgUnitMapper;
    this.uiSettingService = uiSettingService;
    this.roleMapper = roleMapper;
    this.authDao = authDao;
    this.captchaService = captchaService;
    this.smsCodeService = smsCodeService;
    this.request = request;
    this.concurrentLoginService = concurrentLoginService;
    this.operationLogService = operationLogService;
    this.permissionFacade = permissionFacade;
    this.passwordPolicyService = passwordPolicyService;
    this.storageService = storageService;
    this.smsSenderService = smsSenderService;
    this.emailSenderService = emailSenderService;
    this.emailCodeService = emailCodeService;
    this.verificationSettingService = verificationSettingService;
    this.securitySettingService = securitySettingService;
  }

  public LoginResponse login(LoginRequest req) {
    Boolean captchaEnabled = securitySettingService.getOrCreate().getCaptchaEnabled();
    if (Boolean.TRUE.equals(captchaEnabled)) {
      boolean ok = captchaService.verify(req.getCaptchaId(), req.getCaptchaCode());
      if (!ok)
        throw new IllegalArgumentException("验证码错误或已过期");
    }

    UserEntity user = userMapper.selectByAccount(req.getAccount());
    if (user == null)
      throw new IllegalArgumentException("账号或密码错误");

    // 账号区分大小写
    if (!user.getAccount().equals(req.getAccount())) {
      throw new IllegalArgumentException("账号或密码错误");
    }

    if (!BCrypt.checkpw(req.getPassword(), user.getPasswordHash())) {
      throw new IllegalArgumentException("账号或密码错误");
    }

    return completeLogin(user, req.getForce());
  }

  public SmsSendResponse sendSmsCode(SmsSendRequest req) {
    VerificationSetting setting = verificationSettingService.getDecryptedCopy();
    ensureSmsEnabled(setting);
    ensureSmsConfig(setting);

    String phone = normalizePhone(req.getPhone());
    UserEntity user = findUserByPhone(phone);
    if (user == null) {
      throw new IllegalArgumentException("手机号未注册");
    }

    String code = smsCodeService.generateCode(phone);
    try {
      smsSenderService.sendCode(setting, phone, code, getClientIp(), req.getProvider());
    } catch (Exception e) {
      smsCodeService.invalidate(phone);
      throw new IllegalArgumentException("短信发送失败: " + e.getMessage());
    }
    return new SmsSendResponse(smsCodeService.getExpiresInSeconds());
  }

  public LoginResponse loginBySms(SmsLoginRequest req) {
    VerificationSetting setting = verificationSettingService.getDecryptedCopy();
    ensureSmsEnabled(setting);

    String phone = normalizePhone(req.getPhone());
    boolean ok = smsCodeService.verify(phone, req.getCode());
    if (!ok)
      throw new IllegalArgumentException("验证码错误");

    UserEntity user = findUserByPhone(phone);
    if (user == null) {
      throw new IllegalArgumentException("手机号未注册");
    }
    return completeLogin(user, req.getForce());
  }

  public SmsSendResponse sendEmailCode(EmailSendRequest req) {
    VerificationSetting setting = verificationSettingService.getDecryptedCopy();
    ensureEmailEnabled(setting);
    ensureEmailConfig(setting);

    String email = req.getEmail();
    UserEntity user = findUserByEmail(email);
    if (user == null) {
      throw new IllegalArgumentException("邮箱未注册");
    }

    String code = emailCodeService.generateCode(email);
    try {
      emailSenderService.sendLoginCode(setting, email, code, emailCodeService.getExpiresInSeconds());
    } catch (Exception e) {
      emailCodeService.invalidate(email);
      throw new IllegalArgumentException("邮件发送失败: " + e.getMessage());
    }
    return new SmsSendResponse(emailCodeService.getExpiresInSeconds());
  }

  public LoginResponse loginByEmail(EmailLoginRequest req) {
    VerificationSetting setting = verificationSettingService.getDecryptedCopy();
    ensureEmailEnabled(setting);

    String email = req.getEmail();
    boolean ok = emailCodeService.verify(email, req.getCode());
    if (!ok)
      throw new IllegalArgumentException("验证码错误");

    UserEntity user = findUserByEmail(email);
    if (user == null) {
      throw new IllegalArgumentException("邮箱未注册");
    }
    return completeLogin(user, req.getForce());
  }

  private LoginResponse completeLogin(UserEntity user, Boolean force) {
    boolean allowMultiDeviceLogin = isAllowMultiDeviceLogin();
    SaManager.getConfig().setIsConcurrent(allowMultiDeviceLogin);

    DeviceSnapshot snapshot = buildDeviceSnapshot();

    // 检查是否已经在其他设备登录
    if (!allowMultiDeviceLogin && StpUtil.isLogin(user.getId())) {
      if (!Boolean.TRUE.equals(force)) {
        String deviceInfo = buildDeviceInfo(snapshot.deviceModel, snapshot.os, snapshot.browser);
        ConcurrentLoginService.PendingLogin pending = concurrentLoginService.createPending(
            user.getId(),
            snapshot.deviceModel,
            snapshot.os,
            snapshot.browser,
            deviceInfo,
            snapshot.ipAddress,
            snapshot.loginLocation);
        return LoginResponse.pending(pending.getRequestId(), pending.getRequestKey());
      }
      // 如果 force=true，注销之前的登录
      StpUtil.replaced(user.getId(), null);
    }

    ensureUserGuid(user);
    long expiresInSeconds = resolveTokenTimeoutSeconds();
    SaLoginModel loginModel = new SaLoginModel();
    loginModel.setTimeout(expiresInSeconds);
    StpUtil.login(user.getId(), loginModel);
    permissionFacade.clearAssumedRoles(user.getId());
    initSession(user, snapshot);
    operationLogService.logLogin(user, snapshot.deviceModel, snapshot.os, snapshot.browser, snapshot.ipAddress);
    return LoginResponse.success(StpUtil.getTokenValue(), expiresInSeconds);
  }

  private boolean isAllowMultiDeviceLogin() {
    return uiSettingService.isMultiDeviceLoginAllowed();
  }

  private void ensureSmsEnabled(VerificationSetting setting) {
    if (setting == null || !Boolean.TRUE.equals(setting.getSmsEnabled())) {
      throw new IllegalArgumentException("短信验证已禁用");
    }
  }

  private void ensureEmailEnabled(VerificationSetting setting) {
    if (setting == null || !Boolean.TRUE.equals(setting.getEmailEnabled())) {
      throw new IllegalArgumentException("邮箱验证已禁用");
    }
  }

  private void ensureEmailConfig(VerificationSetting setting) {
    if (setting == null
        || isBlank(setting.getEmailHost())
        || setting.getEmailPort() == null
        || setting.getEmailPort() <= 0
        || isBlank(setting.getEmailUsername())
        || isBlank(setting.getEmailPassword())) {
      throw new IllegalArgumentException("邮箱配置不完整");
    }
  }

  private void ensureSmsConfig(VerificationSetting setting) {
    if (setting == null)
      throw new IllegalArgumentException("短信配置缺失");
    boolean aliyunEnabled = smsSenderService.isAliyunEnabled(setting);
    boolean tencentEnabled = smsSenderService.isTencentEnabled(setting);
    if (!aliyunEnabled && !tencentEnabled) {
      throw new IllegalArgumentException("短信配置不完整");
    }
    if (aliyunEnabled) {
      boolean accessKeyMissing = isBlank(setting.getSmsAliyunAccessKeyId());
      boolean secretMissing = isBlank(setting.getSmsAliyunAccessKeySecret());
      boolean signMissing = isBlank(setting.getSmsAliyunSignName());
      boolean templateMissing = isBlank(setting.getSmsAliyunTemplateCode());
      boolean regionMissing = isBlank(setting.getSmsAliyunRegionId());
      if (accessKeyMissing || secretMissing || signMissing || templateMissing || regionMissing) {
        throw new IllegalArgumentException("短信配置不完整");
      }
    }
    if (tencentEnabled) {
      if (isBlank(setting.getSmsTencentSecretId())
          || isBlank(setting.getSmsTencentSecretKey())
          || isBlank(setting.getSmsTencentSignName())
          || isBlank(setting.getSmsTencentTemplateId())
          || isBlank(setting.getSmsTencentRegion())
          || isBlank(setting.getSmsSdkAppId())) {
        throw new IllegalArgumentException("短信配置不完整");
      }
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  private String normalizePhone(String phone) {
    if (phone == null)
      return "";
    String cleaned = phone.trim().replaceAll("[\s-]", "");
    if (cleaned.startsWith("+86"))
      cleaned = cleaned.substring(3);
    if (cleaned.startsWith("86") && cleaned.length() > 11)
      cleaned = cleaned.substring(2);
    return cleaned;
  }

  private String normalizeIdCard(String idCard) {
    if (idCard == null)
      return "";
    return idCard.trim().toUpperCase();
  }

  private UserEntity findUserByPhone(String phone) {
    String normalized = normalizePhone(phone);
    if (normalized.isBlank())
      return null;

    String[] candidates = new String[] {
        normalized,
        "+86" + normalized,
        "+86 " + normalized,
        "86" + normalized,
        "86 " + normalized,
    };

    for (String candidate : candidates) {
      UserEntity user = userMapper.selectByMobile(candidate);
      if (user != null)
        return user;
      user = userMapper.selectByPhone(candidate);
      if (user != null)
        return user;
    }
    return null;
  }

  private UserEntity findUserByEmail(String email) {
    if (email == null || email.isBlank())
      return null;
    return userMapper.selectByEmail(email);
  }

  private String getClientIp() {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("X-Real-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    // 如果是 IPv6 的 localhost，转换为 IPv4
    if ("0:0:0:0:0:0:0:1".equals(ip)) {
      ip = "127.0.0.1";
    }
    return ip;
  }

  private String getLocationByIp(String ip) {
    // 简化处理，实际项目应该集成 IP 地址查询服务
    if (ip.startsWith("127.") || ip.startsWith("192.168.") || ip.equals("localhost") || ip.equals("127.0.0.1")) {
      return "内网IP";
    }
    return "未知地点";
  }

  private String parseBrowser(String userAgent) {
    if (userAgent == null)
      return "Unknown";
    if (userAgent.contains("Edg"))
      return "Edge";
    if (userAgent.contains("Chrome"))
      return "Chrome";
    if (userAgent.contains("Firefox"))
      return "Firefox";
    if (userAgent.contains("Safari") && !userAgent.contains("Chrome"))
      return "Safari";
    return "Unknown";
  }

  private String parseOS(String userAgent) {
    if (userAgent == null)
      return "Unknown";
    if (userAgent.contains("Windows NT 10"))
      return "Windows 10";
    if (userAgent.contains("Windows NT 6.3"))
      return "Windows 8.1";
    if (userAgent.contains("Windows NT 6.2"))
      return "Windows 8";
    if (userAgent.contains("Windows NT 6.1"))
      return "Windows 7";
    if (userAgent.contains("Windows"))
      return "Windows";
    if (userAgent.contains("Mac OS X"))
      return "Mac OS";
    if (userAgent.contains("Linux"))
      return "Linux";
    if (userAgent.contains("Android"))
      return "Android";
    if (userAgent.contains("iPhone") || userAgent.contains("iPad"))
      return "iOS";
    return "Unknown";
  }

  private String parseDeviceModel(String userAgent) {
    if (userAgent == null || userAgent.isBlank())
      return "未知设备";
    if (userAgent.contains("Android")) {
      String model = extractAndroidModel(userAgent);
      return model != null ? model : "Android 设备";
    }
    if (userAgent.contains("iPhone"))
      return "iPhone";
    if (userAgent.contains("iPad"))
      return "iPad";
    if (userAgent.contains("Macintosh") || userAgent.contains("Mac OS X"))
      return "Mac";
    if (userAgent.contains("Windows"))
      return "Windows 设备";
    if (userAgent.contains("Linux"))
      return "Linux 设备";
    return "未知设备";
  }

  private String extractAndroidModel(String userAgent) {
    int androidIndex = userAgent.indexOf("Android");
    if (androidIndex < 0)
      return null;
    int firstSemi = userAgent.indexOf(';', androidIndex);
    if (firstSemi < 0)
      return null;
    int secondSemi = userAgent.indexOf(';', firstSemi + 1);
    int endParen = userAgent.indexOf(')', firstSemi + 1);
    int endIndex = secondSemi;
    if (endIndex == -1 || (endParen != -1 && endParen < endIndex)) {
      endIndex = endParen;
    }
    if (endIndex == -1)
      return null;
    String model = userAgent.substring(firstSemi + 1, endIndex).trim();
    if (model.isEmpty())
      return null;
    int buildIndex = model.indexOf("Build/");
    if (buildIndex > 0) {
      model = model.substring(0, buildIndex).trim();
    }
    return model.isEmpty() ? null : model;
  }

  private String buildDeviceInfo(String deviceModel, String os, String browser) {
    String safeDevice = (deviceModel == null || deviceModel.isBlank()) ? "未知设备" : deviceModel;
    String safeOs = (os == null || os.isBlank()) ? "未知系统" : os;
    String safeBrowser = (browser == null || browser.isBlank()) ? "未知浏览器" : browser;
    return safeDevice + " / " + safeOs + " / " + safeBrowser;
  }

  private static class DeviceSnapshot {
    private final String deviceModel;
    private final String os;
    private final String browser;
    private final String ipAddress;
    private final String loginLocation;

    private DeviceSnapshot(String deviceModel, String os, String browser, String ipAddress, String loginLocation) {
      this.deviceModel = deviceModel;
      this.os = os;
      this.browser = browser;
      this.ipAddress = ipAddress;
      this.loginLocation = loginLocation;
    }
  }

  public LoginResponse confirmLogin(String requestId, String requestKey) {
    ConcurrentLoginService.PendingLogin pending = concurrentLoginService.consumeApproved(requestId, requestKey);
    UserEntity user = Optional.ofNullable(userMapper.selectById(pending.getLoginId()))
        .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    ensureUserGuid(user);
    long expiresInSeconds = resolveTokenTimeoutSeconds();
    SaLoginModel loginModel = new SaLoginModel();
    loginModel.setTimeout(expiresInSeconds);
    StpUtil.login(user.getId(), loginModel);
    permissionFacade.clearAssumedRoles(user.getId());
    DeviceSnapshot snapshot = new DeviceSnapshot(
        pending.getDeviceModel(),
        pending.getOs(),
        pending.getBrowser(),
        pending.getIpAddress(),
        pending.getLoginLocation());
    initSession(user, snapshot);
    operationLogService.logLogin(user, snapshot.deviceModel, snapshot.os, snapshot.browser, snapshot.ipAddress);
    return LoginResponse.success(StpUtil.getTokenValue(), expiresInSeconds);
  }

  private void initSession(UserEntity user, DeviceSnapshot snapshot) {
    StpUtil.getSession().set("loginId", user.getId());
    StpUtil.getSession().set("userName", user.getName());
    StpUtil.getSession().set("account", user.getAccount());
    StpUtil.getSession().set("userGuid", user.getGuid());
    StpUtil.getSession().set("ipAddress", snapshot.ipAddress);
    StpUtil.getSession().set("loginLocation", snapshot.loginLocation);
    StpUtil.getSession().set("browser", snapshot.browser);
    StpUtil.getSession().set("os", snapshot.os);
    StpUtil.getSession().set("deviceModel", snapshot.deviceModel);
  }

  private DeviceSnapshot buildDeviceSnapshot() {
    String userAgent = request.getHeader("User-Agent");
    String browser = parseBrowser(userAgent);
    String os = parseOS(userAgent);
    String deviceModel = parseDeviceModel(userAgent);
    String ipAddress = getClientIp();
    String loginLocation = getLocationByIp(ipAddress);
    return new DeviceSnapshot(deviceModel, os, browser, ipAddress, loginLocation);
  }

  public UserInfoResponse currentUserInfo() {
    long userId = StpUtil.getLoginIdAsLong();
    UserEntity user = Optional.ofNullable(userMapper.selectById(userId))
        .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    ensureUserGuid(user);
    List<String> roles = permissionFacade.getEffectiveRoles(userId);
    List<String> perms = permissionFacade.getEffectivePermissions(userId);
    String avatar = normalizeAvatar(user.getAvatar());
    UserInfoResponse resp = new UserInfoResponse(user.getName(), avatar, roles, perms);
    resp.setId(user.getId());
    resp.setGuid(user.getGuid());
    resp.setAssignedRoles(permissionFacade.getAssignedRoles(userId));
    resp.setRoleSimulated(permissionFacade.isAssumed(userId));
    return resp;
  }

  public UserProfileResponse currentUserProfile() {
    long userId = StpUtil.getLoginIdAsLong();
    UserEntity user = Optional.ofNullable(userMapper.selectById(userId))
        .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    return enrichProfile(toProfile(user), userId);
  }

  @Transactional
  public UserProfileResponse updateCurrentUserProfile(UserProfileUpdateRequest req) {
    long userId = StpUtil.getLoginIdAsLong();
    UserEntity u = Optional.ofNullable(userMapper.selectById(userId))
        .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    String oldAvatar = u.getAvatar();
    if (req.getName() != null)
      u.setName(req.getName());
    if (req.getMobile() != null && !SensitiveMaskUtil.isMasked(req.getMobile()))
      u.setMobile(req.getMobile());
    if (req.getPhone() != null && !SensitiveMaskUtil.isMasked(req.getPhone()))
      u.setPhone(req.getPhone());
    if (req.getEmail() != null && !SensitiveMaskUtil.isMasked(req.getEmail()))
      u.setEmail(req.getEmail());
    if (req.getIdCard() != null && !SensitiveMaskUtil.isMasked(req.getIdCard()))
      u.setIdCard(req.getIdCard());
    if (req.getSeat() != null)
      u.setSeat(req.getSeat());
    if (req.getEntity() != null)
      u.setEntity(req.getEntity());
    if (req.getLeader() != null)
      u.setLeader(req.getLeader());
    if (req.getPosition() != null)
      u.setPosition(req.getPosition());
    if (req.getJoinDay() != null)
      u.setJoinDay(req.getJoinDay());
    if (req.getTeam() != null)
      u.setTeam(req.getTeam());
    if (req.getGender() != null)
      u.setGender(req.getGender());
    if (req.getNickname() != null)
      u.setNickname(req.getNickname());
    if (req.getProvince() != null)
      u.setProvince(req.getProvince());
    if (req.getCity() != null)
      u.setCity(req.getCity());
    if (req.getDistrict() != null)
      u.setDistrict(req.getDistrict());
    if (req.getAddress() != null)
      u.setAddress(req.getAddress());
    if (req.getIntroduction() != null)
      u.setIntroduction(req.getIntroduction());
    if (req.getAvatar() != null)
      u.setAvatar(req.getAvatar());
    if (req.getTags() != null)
      u.setTags(req.getTags());
    ensureUserGuid(u);
    saveUser(u);

    if (StpUtil.isLogin()) {
      StpUtil.getSession().set("userName", u.getName());
    }
    if (req.getAvatar() != null && oldAvatar != null && !oldAvatar.equals(req.getAvatar())) {
      deleteUploadFile(oldAvatar);
    }
    operationLogService.log("UPDATE", "个人资料", "更新个人资料");

    return enrichProfile(toProfile(u), userId);
  }

  private UserProfileResponse enrichProfile(UserProfileResponse profile, long userId) {
    profile.setRoles(permissionFacade.getEffectiveRoles(userId));
    profile.setOrgUnitNames(orgUnitMapper.selectNamesByUserId(userId));
    return profile;
  }

  private UserProfileResponse toProfile(UserEntity u) {
    UserProfileResponse r = new UserProfileResponse();
    r.setId(u.getId());
    r.setAccount(u.getAccount());
    r.setName(u.getName());
    r.setMobile(u.getMobile());
    r.setPhone(u.getPhone());
    r.setEmail(u.getEmail());
    r.setIdCard(u.getIdCard());
    r.setSeat(u.getSeat());
    r.setEntity(u.getEntity());
    r.setLeader(u.getLeader());
    r.setPosition(u.getPosition());
    r.setJoinDay(u.getJoinDay());
    r.setTeam(u.getTeam());
    r.setGender(u.getGender());
    r.setNickname(u.getNickname());
    r.setProvince(u.getProvince());
    r.setCity(u.getCity());
    r.setDistrict(u.getDistrict());
    r.setAddress(u.getAddress());
    r.setIntroduction(u.getIntroduction());
    r.setAvatar(normalizeAvatar(u.getAvatar()));
    r.setTags(u.getTags());
    return r;
  }

  private UserEntity saveUser(UserEntity user) {
    if (user.getId() == null) {
      userMapper.insert(user);
    } else {
      userMapper.update(user);
    }
    return user;
  }

  private void deleteUploadFile(String url) {
    try {
      storageService.deleteByUrl(url);
    } catch (Exception ignored) {
      // Best-effort delete; avatar update should not fail because of cleanup.
    }
  }

  private String normalizeAvatar(String url) {
    if (url == null || url.isBlank()) return null;
    String original = url.trim();
    if (original.startsWith("http://") || original.startsWith("https://")) {
      return original;
    }
    String clean = original;
    if (clean.startsWith("/api/")) {
      clean = clean.substring(4);
    }
    String pathOnly = clean;
    int queryIndex = pathOnly.indexOf('?');
    if (queryIndex >= 0) {
      pathOnly = pathOnly.substring(0, queryIndex);
    }
    int fragmentIndex = pathOnly.indexOf('#');
    if (fragmentIndex >= 0) {
      pathOnly = pathOnly.substring(0, fragmentIndex);
    }
    if (!pathOnly.startsWith("/uploads/")) {
      return original;
    }
    String relative = pathOnly.substring("/uploads/".length());
    if (relative.isEmpty()) return null;
    try {
      Path root = Paths.get(System.getProperty("user.dir"), "uploads").toAbsolutePath().normalize();
      Path target = root.resolve(relative).normalize();
      if (!target.startsWith(root)) return null;
      if (!Files.exists(target)) return null;
      return original;
    } catch (Exception e) {
      return original;
    }
  }

  @Transactional
  public boolean changePassword(ChangePasswordRequest req) {
    if (!req.getNewPassword().equals(req.getConfirmPassword())) {
      throw new IllegalArgumentException("两次输入的新密码不一致");
    }
    // 根据系统设置校验新密码规范
    passwordPolicyService.validate(req.getNewPassword());

    long userId = StpUtil.getLoginIdAsLong();
    UserEntity u = Optional.ofNullable(userMapper.selectById(userId))
        .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

    if (!BCrypt.checkpw(req.getOldPassword(), u.getPasswordHash())) {
      throw new IllegalArgumentException("当前密码错误");
    }

    u.setPasswordHash(BCrypt.hashpw(req.getNewPassword(), BCrypt.gensalt()));
    ensureUserGuid(u);
    saveUser(u);

    operationLogService.log("UPDATE", "修改密码", "用户修改了登录密码");
    return true;
  }

  @Transactional
  public boolean resetPassword(ForgotPasswordRequest req) {
    if (!req.getNewPassword().equals(req.getConfirmPassword())) {
      throw new IllegalArgumentException("两次输入的新密码不一致");
    }
    // 根据系统设置校验新密码规范
    passwordPolicyService.validate(req.getNewPassword());
    VerificationSetting setting = verificationSettingService.getDecryptedCopy();
    ensureSmsEnabled(setting);

    String phone = normalizePhone(req.getPhone());
    boolean verified = smsCodeService.verify(phone, req.getCode());
    if (!verified) {
      throw new IllegalArgumentException("验证码错误或已过期");
    }

    UserEntity user = Optional.ofNullable(userMapper.selectByAccount(req.getAccount()))
        .orElseThrow(() -> new IllegalArgumentException("账号不存在"));
    String normalizedPhone = normalizePhone(user.getMobile() != null ? user.getMobile() : user.getPhone());
    if (!phone.equals(normalizedPhone)) {
      throw new IllegalArgumentException("手机号与账号不匹配");
    }

    user.setPasswordHash(BCrypt.hashpw(req.getNewPassword(), BCrypt.gensalt()));
    ensureUserGuid(user);
    saveUser(user);
    operationLogService.log("UPDATE", "重置密码", "用户通过短信验证码重置了密码");
    return true;
  }

  @Transactional
  public UserInfoResponse switchRoles(RoleSwitchRequest req) {
    long userId = StpUtil.getLoginIdAsLong();
    if (!permissionFacade.isAdminAccount(userId)) {
      throw new IllegalArgumentException("仅 admin 账号可以切换演示权限");
    }
    if (req.getRoles() == null || req.getRoles().isEmpty()) {
      permissionFacade.clearAssumedRoles(userId);
      return currentUserInfo();
    }
    permissionFacade.assumeRoles(userId, req.getRoles());
    return currentUserInfo();
  }

  public List<String> listAllRoleNames() {
    long userId = StpUtil.getLoginIdAsLong();
    if (!permissionFacade.isAdminAccount(userId)) {
      throw new IllegalArgumentException("仅 admin 账号可查看角色列表");
    }
    return roleMapper.selectAll().stream().map(r -> r.getName()).toList();
  }

  public boolean logout() {
    if (StpUtil.isLogin()) {
      long userId = StpUtil.getLoginIdAsLong();
      permissionFacade.clearAssumedRoles(userId);
      StpUtil.logout();
    }
    return true;
  }

  @Transactional
  public boolean register(RegisterRequest req) {
    Boolean captchaEnabled = securitySettingService.getOrCreate().getCaptchaEnabled();
    if (Boolean.TRUE.equals(captchaEnabled)) {
      boolean ok = captchaService.verify(req.getCaptchaId(), req.getCaptchaCode());
      if (!ok)
        throw new IllegalArgumentException("验证码错误或已过期");
    }
    if (!req.getPassword().equals(req.getConfirmPassword())) {
      throw new IllegalArgumentException("两次密码输入不一致");
    }
    // 根据系统设置校验注册密码规范
    passwordPolicyService.validate(req.getPassword());
    String account = req.getAccount().trim();
    String mobile = normalizePhone(req.getMobile());
    String email = req.getEmail() == null ? "" : req.getEmail().trim();
    String idCard = normalizeIdCard(req.getIdCard());
    if (!mobile.isBlank() && findUserByPhone(mobile) != null) {
      throw new IllegalArgumentException("手机号已注册");
    }
    if (!email.isBlank() && userMapper.countByEmailIgnoreCase(email) > 0) {
      throw new IllegalArgumentException("邮箱已注册");
    }
    if (!idCard.isBlank() && userMapper.countByIdCard(idCard) > 0) {
      throw new IllegalArgumentException("身份证号已注册");
    }
    if (userMapper.countByAccount(account) > 0) {
      if (!mobile.isBlank() && account.equals(mobile)) {
        throw new IllegalArgumentException("手机号已注册");
      }
      throw new IllegalArgumentException("账号已存在");
    }

    UserEntity e = new UserEntity();
    e.setAccount(account);
    e.setGuid(UUID.randomUUID().toString());
    e.setName((req.getName() == null || req.getName().isBlank()) ? account : req.getName().trim());
    e.setPasswordHash(BCrypt.hashpw(req.getPassword(), BCrypt.gensalt()));
    e.setEmail(email.isBlank() ? null : email);
    e.setMobile(mobile.isBlank() ? null : mobile);
    e.setIdCard(idCard.isBlank() ? null : idCard);

    try {
      e = saveUser(e);
    } catch (DataIntegrityViolationException ex) {
      throw new IllegalArgumentException("数据唯一性冲突，注册失败");
    }

    authDao.replaceUserRoles(e.getId(), List.of("user"));
    return true;
  }

  private void ensureUserGuid(UserEntity user) {
    if (user.getGuid() != null && !user.getGuid().isBlank())
      return;
    user.setGuid(UUID.randomUUID().toString());
    saveUser(user);
  }

  private long resolveTokenTimeoutSeconds() {
    Integer minutes = securitySettingService.getOrCreate().getTokenTimeoutMinutes();
    if (minutes != null && minutes > 0) {
      return minutes * 60L;
    }
    return SaManager.getConfig().getTimeout();
  }

}
