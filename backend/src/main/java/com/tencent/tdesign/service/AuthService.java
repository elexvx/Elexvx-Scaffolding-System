package com.tencent.tdesign.service;

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
import com.tencent.tdesign.security.AuthContext;
import com.tencent.tdesign.security.AuthSession;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private static final String MSG_ACCOUNT_REQUIRED = "请输入账号";
  private static final String MSG_ACCOUNT_EXISTS = "账号已存在，请更换后重试";
  private static final String MSG_REGISTER_RETRY = "注册失败，请稍后重试";
  private static final String DOC_TYPE_RESIDENT_ID_CARD = "resident_id_card";
  private static final String DOC_TYPE_PASSPORT = "passport";
  private static final Set<String> DOC_TYPE_RESIDENT_ID_CARD_ALIASES =
      Set.of("resident_id_card", "id_card", "identity_card", "china_id_card", "居民身份证");
  private static final Set<String> DOC_TYPE_PASSPORT_ALIASES = Set.of("passport", "护照");
  private static final int[] RESIDENT_ID_CARD_WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
  private static final char[] RESIDENT_ID_CARD_CHECKSUM_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

  private final UserMapper userMapper;
  private final OrgUnitMapper orgUnitMapper;
  private final UiSettingService uiSettingService;
  private final RoleMapper roleMapper;
  private final AuthQueryDao authDao;
  private final CaptchaService captchaService;
  private final HttpServletRequest request;
  private final ConcurrentLoginService concurrentLoginService;
  private final OperationLogService operationLogService;
  private final PermissionFacade permissionFacade;
  private final PasswordPolicyService passwordPolicyService;
  private final ObjectStorageService storageService;
  private final Optional<SmsSenderService> smsSenderService;
  private final Optional<EmailSenderService> emailSenderService;
  private final SmsCodeService smsCodeService;
  private final EmailCodeService emailCodeService;
  private final VerificationSettingService verificationSettingService;
  private final ModuleRegistryService moduleRegistryService;
  private final com.tencent.tdesign.verification.VerificationProviderRegistry verificationProviderRegistry;
  private final SecuritySettingService securitySettingService;
  private final AuthTokenService authTokenService;
  private final AuthContext authContext;

  public AuthService(
    UserMapper userMapper,
    OrgUnitMapper orgUnitMapper,
      UiSettingService uiSettingService,
      RoleMapper roleMapper,
      AuthQueryDao authDao,
      CaptchaService captchaService,
      HttpServletRequest request,
      ConcurrentLoginService concurrentLoginService,
      OperationLogService operationLogService,
      PermissionFacade permissionFacade,
      PasswordPolicyService passwordPolicyService,
      ObjectStorageService storageService,
      Optional<SmsSenderService> smsSenderService,
      Optional<EmailSenderService> emailSenderService,
      SmsCodeService smsCodeService,
      EmailCodeService emailCodeService,
      VerificationSettingService verificationSettingService,
      ModuleRegistryService moduleRegistryService,
      com.tencent.tdesign.verification.VerificationProviderRegistry verificationProviderRegistry,
      SecuritySettingService securitySettingService,
      AuthTokenService authTokenService,
      AuthContext authContext) {
    this.userMapper = userMapper;
    this.orgUnitMapper = orgUnitMapper;
    this.uiSettingService = uiSettingService;
    this.roleMapper = roleMapper;
    this.authDao = authDao;
    this.captchaService = captchaService;
    this.request = request;
    this.concurrentLoginService = concurrentLoginService;
    this.operationLogService = operationLogService;
    this.permissionFacade = permissionFacade;
    this.passwordPolicyService = passwordPolicyService;
    this.storageService = storageService;
    this.smsSenderService = smsSenderService;
    this.emailSenderService = emailSenderService;
    this.smsCodeService = smsCodeService;
    this.emailCodeService = emailCodeService;
    this.verificationSettingService = verificationSettingService;
    this.moduleRegistryService = moduleRegistryService;
    this.verificationProviderRegistry = verificationProviderRegistry;
    this.securitySettingService = securitySettingService;
    this.authTokenService = authTokenService;
    this.authContext = authContext;
  }

  public LoginResponse login(LoginRequest req) {
    String account = normalizeAccount(req.getAccount());
    String captchaCode = normalizeCode(req.getCaptchaCode());
    Boolean captchaEnabled = securitySettingService.getOrCreate().getCaptchaEnabled();
    if (Boolean.TRUE.equals(captchaEnabled)) {
      boolean ok = captchaService.verify(req.getCaptchaId(), captchaCode);
      if (!ok)
        throw new IllegalArgumentException("Verification code invalid or expired");
    }

    UserEntity user = userMapper.selectByAccount(account);
    if (user == null)
      throw new IllegalArgumentException("Invalid account or password");

    // 闂傚倷娴囧畷鍨叏閻㈢绀夌憸蹇曞垝婵犳艾绠ｉ柨婵嗗暕濮规姊虹粔鍡楀濞堟梻绱掗埀顒€鐣濋崟顒傚幈濠电娀娼уΛ妤咁敂閳哄懏鐓熼幖娣€ら崵娆愩亜椤撶偞鍋ョ€规洖銈稿畷鍗炍旈埀顒勫汲閻斿憡鍙忛柨婵嗘噽婢э箓鏌?
    if (!user.getAccount().equals(account)) {
      throw new IllegalArgumentException("Invalid account or password");
    }

    if (!BCrypt.checkpw(req.getPassword(), user.getPasswordHash())) {
      throw new IllegalArgumentException("Invalid account or password");
    }

    return completeLogin(user, req.getForce());
  }

  public SmsSendResponse sendSmsCode(SmsSendRequest req) {
    moduleRegistryService.assertModuleAvailable("sms");
    VerificationSetting setting = verificationSettingService.getDecryptedCopy();
    ensureSmsEnabled(setting);
    ensureSmsConfig(setting);

    String phone = normalizePhone(req.getPhone());
    UserEntity user = findUserByPhone(phone);
    if (user == null) {
      throw new IllegalArgumentException("Phone not registered");
    }

    com.tencent.tdesign.verification.VerificationProvider provider = verificationProviderRegistry.require("sms");
    try {
      provider.sendCode(setting, phone, getClientIp(), req.getProvider());
    } catch (Exception e) {
      throw new IllegalArgumentException("闂傚倸鍊烽悞锕€顪冮崸妤€鐭楅幖娣妼閸屻劑鏌ｉ幋鐑嗙劷闁崇懓绉撮埞鎴︽偐閸欏鎮欓梺娲诲幗椤ㄥ﹪寮婚悢鍛婄秶闁硅鍔曢幃鍛存⒑缁嬫鍎愰柟绋款煼楠炲繘宕ㄩ弶鎴狀啇闂佺粯鍔楁晶妤€顭块埀顒勬⒒? " + e.getMessage());
    }
    return new SmsSendResponse(provider.getExpiresInSeconds());
  }

  public LoginResponse loginBySms(SmsLoginRequest req) {
    moduleRegistryService.assertModuleAvailable("sms");
    VerificationSetting setting = verificationSettingService.getDecryptedCopy();
    ensureSmsEnabled(setting);

    String phone = normalizePhone(req.getPhone());
    String code = normalizeCode(req.getCode());
    boolean ok = smsCodeService.verify(phone, code);
    if (!ok)
      throw new IllegalArgumentException("Invalid verification code");

    UserEntity user = findUserByPhone(phone);
    if (user == null) {
      throw new IllegalArgumentException("Phone not registered");
    }
    return completeLogin(user, req.getForce());
  }

  public SmsSendResponse sendEmailCode(EmailSendRequest req) {
    moduleRegistryService.assertModuleAvailable("email");
    VerificationSetting setting = verificationSettingService.getDecryptedCopy();
    ensureEmailEnabled(setting);
    ensureEmailConfig(setting);

    String email = normalizeEmail(req.getEmail());
    UserEntity user = findUserByEmail(email);
    if (user == null) {
      throw new IllegalArgumentException("Email not registered");
    }

    com.tencent.tdesign.verification.VerificationProvider provider = verificationProviderRegistry.require("email");
    try {
      provider.sendCode(setting, email, getClientIp(), null);
    } catch (Exception e) {
      throw new IllegalArgumentException("闂傚倸鍊搁崐椋庢閿熺姴鐭楅幖娣妼缁愭鏌￠崶鈺佷汗闁哄閰ｉ弻鏇＄疀鐎ｎ亖鍋撻弴銏″剹婵°倕鎳忛悡娆撳级閸喎绀冮柛姘€块弻娑氣偓锝庡亝鐏忎即鏌熷畡鐗堝殗妤犵偞鐗犻幃娆撴偋閸喓绐旈梻? " + e.getMessage());
    }
    return new SmsSendResponse(provider.getExpiresInSeconds());
  }

  public LoginResponse loginByEmail(EmailLoginRequest req) {
    moduleRegistryService.assertModuleAvailable("email");
    VerificationSetting setting = verificationSettingService.getDecryptedCopy();
    ensureEmailEnabled(setting);

    String email = normalizeEmail(req.getEmail());
    String code = normalizeCode(req.getCode());
    boolean ok = emailCodeService.verify(email, code);
    if (!ok)
      throw new IllegalArgumentException("Invalid verification code");

    UserEntity user = findUserByEmail(email);
    if (user == null) {
      throw new IllegalArgumentException("Email not registered");
    }
    return completeLogin(user, req.getForce());
  }

  private LoginResponse completeLogin(UserEntity user, Boolean force) {
    ensureUserActive(user);
    boolean allowMultiDeviceLogin = isAllowMultiDeviceLogin();
    DeviceSnapshot snapshot = buildDeviceSnapshot();

    // 婵犵數濮烽。钘壩ｉ崨鏉戠；闁逞屽墴閺屾稓鈧綆鍋呭畷宀勬煛瀹€瀣？濞寸媴濡囬幏鐘诲箵閹烘埈娼ュ┑鐘殿暯閳ь剙鍟跨痪褔鏌熼鐓庘偓鎼佹偩閻戣棄唯闁冲搫鍊瑰▍鍥⒑闁偛鑻晶瀵糕偓瑙勬礃閻熲晠寮澶婄妞ゆ挾濯寸槐鏌ユ⒒婵犲骸浜滄繛璇х畵楠炴劖绻濆鍗炲絾濡炪倖甯掔€氼參鎮￠弴銏㈠彄闁搞儯鍔嶉埛鎰版煛鐎ｎ亜顏紒杈ㄥ浮椤㈡岸宕ㄩ鐑嗘骄闂備胶鎳撻崲鏌ヮ敄婢舵劗宓侀柛鈩冨嚬濡俱劌鈹戦檱鐏忔瑩寮繝姘摕闁挎繂鐗滃Ο浣割渻閵堝棙鑲犻柛娑卞灣椤?
    if (!allowMultiDeviceLogin && hasActiveSession(user.getId())) {
      if (!Boolean.TRUE.equals(force)) {
        if (concurrentLoginService.hasActiveSubscriber(user.getId())) {
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
        // No active approval listener; revoke old sessions and continue.
        authTokenService.removeUserTokens(user.getId());
      } else {
        // Force login: revoke previous sessions.
        authTokenService.removeUserTokens(user.getId());
      }
    }

    ensureUserGuid(user);
    long expiresInSeconds = resolveTokenTimeoutSeconds();
    AuthSession session = buildSession(user, snapshot);
    String token = authTokenService.createToken(user.getId(), session, expiresInSeconds);
    operationLogService.logLogin(user, snapshot.deviceModel, snapshot.os, snapshot.browser, snapshot.ipAddress);
    return LoginResponse.success(token, expiresInSeconds);
  }

  private void ensureUserActive(UserEntity user) {
    if (user.getStatus() != null && user.getStatus() == 0) {
      throw new IllegalArgumentException("Account disabled");
    }
  }

  private boolean isAllowMultiDeviceLogin() {
    return uiSettingService.isMultiDeviceLoginAllowed();
  }

  private void ensureSmsEnabled(VerificationSetting setting) {
    if (smsSenderService.isEmpty()) {
      throw new IllegalArgumentException("SMS module not enabled");
    }
    if (setting == null || !Boolean.TRUE.equals(setting.getSmsEnabled())) {
      throw new IllegalArgumentException("SMS verification is disabled");
    }
  }

  private void ensureEmailEnabled(VerificationSetting setting) {
    if (emailSenderService.isEmpty()) {
      throw new IllegalArgumentException("Email module not enabled");
    }
    if (setting == null || !Boolean.TRUE.equals(setting.getEmailEnabled())) {
      throw new IllegalArgumentException("Email verification is disabled");
    }
  }

  private SmsSenderService requireSmsSender() {
    return smsSenderService.orElseThrow(() -> new IllegalArgumentException("SMS module not enabled"));
  }

  private EmailSenderService requireEmailSender() {
    return emailSenderService.orElseThrow(() -> new IllegalArgumentException("Email module not enabled"));
  }

  private void ensureEmailConfig(VerificationSetting setting) {
    if (setting == null
        || isBlank(setting.getEmailHost())
        || setting.getEmailPort() == null
        || setting.getEmailPort() <= 0
        || isBlank(setting.getEmailUsername())
        || isBlank(setting.getEmailPassword())) {
      throw new IllegalArgumentException("Email config incomplete");
    }
  }

  private void ensureSmsConfig(VerificationSetting setting) {
    if (setting == null)
      throw new IllegalArgumentException("SMS config missing");
    SmsSenderService sender = requireSmsSender();
    boolean aliyunEnabled = sender.isAliyunEnabled(setting);
    boolean tencentEnabled = sender.isTencentEnabled(setting);
    if (!aliyunEnabled && !tencentEnabled) {
      throw new IllegalArgumentException("SMS config incomplete");
    }
    if (aliyunEnabled) {
      boolean accessKeyMissing = isBlank(setting.getSmsAliyunAccessKeyId());
      boolean secretMissing = isBlank(setting.getSmsAliyunAccessKeySecret());
      boolean signMissing = isBlank(setting.getSmsAliyunSignName());
      boolean templateMissing = isBlank(setting.getSmsAliyunTemplateCode());
      boolean regionMissing = isBlank(setting.getSmsAliyunRegionId());
      if (accessKeyMissing || secretMissing || signMissing || templateMissing || regionMissing) {
        throw new IllegalArgumentException("SMS config incomplete");
      }
    }
    if (tencentEnabled) {
      if (isBlank(setting.getSmsTencentSecretId())
          || isBlank(setting.getSmsTencentSecretKey())
          || isBlank(setting.getSmsTencentSignName())
          || isBlank(setting.getSmsTencentTemplateId())
          || isBlank(setting.getSmsTencentRegion())
          || isBlank(setting.getSmsSdkAppId())) {
        throw new IllegalArgumentException("SMS config incomplete");
      }
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  private String normalizePhone(String phone) {
    if (phone == null)
      return "";
    String cleaned = phone.trim().replaceAll("[\\s-]", "");
    if (cleaned.startsWith("+86"))
      cleaned = cleaned.substring(3);
    if (cleaned.startsWith("86") && cleaned.length() > 11)
      cleaned = cleaned.substring(2);
    return cleaned;
  }

  private String normalizeEmail(String email) {
    if (email == null)
      return "";
    return email.trim().replaceAll("\\s+", "");
  }

  private String normalizeAccount(String account) {
    if (account == null)
      return "";
    return account.trim();
  }

  private String normalizeCode(String code) {
    if (code == null)
      return "";
    return code.trim().replaceAll("\\s+", "");
  }

  private String normalizeIdCard(String idCard) {
    if (idCard == null)
      return "";
    return idCard.trim().toUpperCase();
  }

  private String normalizeDocumentType(String idType) {
    if (idType == null) return null;
    String normalized = idType.trim();
    if (normalized.isEmpty()) return null;
    String lower = normalized.toLowerCase(Locale.ROOT);
    if (DOC_TYPE_RESIDENT_ID_CARD_ALIASES.contains(lower) || DOC_TYPE_RESIDENT_ID_CARD_ALIASES.contains(normalized)) {
      return DOC_TYPE_RESIDENT_ID_CARD;
    }
    if (DOC_TYPE_PASSPORT_ALIASES.contains(lower) || DOC_TYPE_PASSPORT_ALIASES.contains(normalized)) {
      return DOC_TYPE_PASSPORT;
    }
    return lower;
  }

  private boolean hasText(String value) {
    return value != null && !value.trim().isEmpty();
  }

  private int percent(int complete, int total) {
    if (total <= 0) return 0;
    return Math.round((complete * 100f) / total);
  }

  private void validateDocumentInfo(UserEntity user) {
    String idType = normalizeDocumentType(user.getIdType());
    String idCard = normalizeIdCard(user.getIdCard());
    user.setIdType(idType);
    user.setIdCard(idCard.isBlank() ? null : idCard);

    if (idType == null && !idCard.isBlank()) {
      throw new IllegalArgumentException("证件号码已填写，请先选择证件类型");
    }

    if (!idCard.isBlank()) {
      switch (idType) {
        case DOC_TYPE_RESIDENT_ID_CARD -> validateResidentIdCard(idCard);
        case DOC_TYPE_PASSPORT -> validatePassport(idCard);
        default -> throw new IllegalArgumentException("不支持的证件类型: " + idType);
      }
    }

    LocalDate validFrom = user.getIdValidFrom();
    LocalDate validTo = user.getIdValidTo();
    if (validFrom != null && validTo != null && validTo.isBefore(validFrom)) {
      throw new IllegalArgumentException("证件有效期止不能早于证件有效期起");
    }
  }

  private void validateResidentIdCard(String idCard) {
    if (!idCard.matches("^[1-9]\\d{16}[0-9X]$")) {
      throw new IllegalArgumentException("居民身份证号码格式不正确");
    }
    String birth = idCard.substring(6, 14);
    try {
      LocalDate.parse(birth, java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
    } catch (Exception ex) {
      throw new IllegalArgumentException("居民身份证号码中的出生日期不合法");
    }
    if (!isValidResidentIdCardChecksum(idCard)) {
      throw new IllegalArgumentException("居民身份证号码校验位不正确");
    }
  }

  private boolean isValidResidentIdCardChecksum(String idCard) {
    int sum = 0;
    for (int i = 0; i < 17; i++) {
      char ch = idCard.charAt(i);
      if (ch < '0' || ch > '9') return false;
      sum += (ch - '0') * RESIDENT_ID_CARD_WEIGHTS[i];
    }
    char expected = RESIDENT_ID_CARD_CHECKSUM_CODES[sum % 11];
    return expected == idCard.charAt(17);
  }

  private void validatePassport(String passportNo) {
    if (!passportNo.matches("^[A-Z0-9]{5,17}$")) {
      throw new IllegalArgumentException("护照号码格式不正确");
    }
  }

  private void fillProfileCompleteness(UserProfileResponse profile) {
    int basicDone = 0;
    int documentDone = 0;
    List<String> incompleteItems = new ArrayList<>();

    if (hasText(profile.getName())) basicDone += 1;
    else incompleteItems.add("name");

    if (hasText(profile.getGender())) basicDone += 1;
    else incompleteItems.add("gender");

    if (hasText(profile.getMobile())) basicDone += 1;
    else incompleteItems.add("mobile");

    if (hasText(profile.getEmail())) basicDone += 1;
    else incompleteItems.add("email");

    boolean hasAddress = hasText(profile.getAddress())
      || hasText(profile.getProvince())
      || hasText(profile.getCity())
      || hasText(profile.getDistrict());
    if (hasAddress) basicDone += 1;
    else incompleteItems.add("address");

    if (hasText(profile.getIdType())) documentDone += 1;
    else incompleteItems.add("idType");

    if (hasText(profile.getIdCard())) documentDone += 1;
    else incompleteItems.add("idCard");

    if (profile.getIdValidFrom() != null) documentDone += 1;
    else incompleteItems.add("idValidFrom");

    if (profile.getIdValidTo() != null) documentDone += 1;
    else incompleteItems.add("idValidTo");

    profile.setBasicInfoScore(percent(basicDone, 5));
    profile.setDocumentInfoScore(percent(documentDone, 4));
    profile.setCompletenessScore(percent(basicDone + documentDone, 9));
    profile.setIncompleteItems(incompleteItems);
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
    // 濠电姷鏁告慨鐑姐€傛禒瀣劦妞ゆ巻鍋撻柛鐔锋健閸┾偓妞ゆ巻鍋撶紓宥咃躬楠炲啫螣鐠囪尙绐為梺褰掑亰閸撴瑥顕?IPv6 闂?localhost闂傚倸鍊烽悞锔锯偓绗涘懐鐭欓柟杈鹃檮閸庢霉閿濆牜鍤夌憸鐗堝笒楠炪垺绻涢幋鐑嗙劷妞ゆ柨顦—鍐Χ鎼粹€茶埅闂佸憡鐟㈤崑鎾剁磽娴ｆ彃浜?IPv4
    if ("0:0:0:0:0:0:0:1".equals(ip)) {
      ip = "127.0.0.1";
    }
    return ip;
  }

  private String getLocationByIp(String ip) {
    // 缂傚倸鍊搁崐鐑芥嚄閼稿灚鍙忔い鎾卞灩绾惧鏌熼崜褏甯涢柣鎾存礋閺屸€愁吋閸愩劌顬嬫繝鈷€灞肩凹闁靛洤瀚伴弫鍌滄喆閸曨剛褰嬮梻浣筋嚃閸犳牠鎮ラ悡搴ｆ殾闂侇剙绋侀弫鍥煟濡椿鍟忔俊鑼厴濮婅櫣鎷犻弻銉偓妤佺節閳ь剟鏌嗗鍛紱闂侀潧鐗嗛幏瀣倵閼哥偣浜滈柡宥冨妿閳笺倕霉濠婂嫮鐭掗柡灞剧洴閺佸倻鎷犻幓鎺旑啈缂傚倷鐒﹂崝鏍€冩繝鍥╁祦闁哄稁鍋€閸嬫挸鈽夊▍铏灴钘熼柛鈩兦滄禍婊堟煛閸モ晛鏋庨柛婵堝劋閹便劍绻濋崒娑樹淮閻庤娲栧畷顒冪亽闂佹儳绻橀埀顒佺⊕閹?IP 闂傚倸鍊风欢姘焽婵犳碍鈷旈柛鏇ㄥ亽閻斿棙淇婇娆掝劅闁绘帊绮欓弻娑㈠箛闂堟稒鐏堥梺钘夊暟閸犳牠寮婚弴鐔风窞婵☆垳鍘х敮銉╂⒑閹肩偛鈧洜鈧矮鍗冲濠氭晸閻樻煡鍞跺┑鐘绘涧閻斿棝濮€閻橆偅鏂€?
    if (ip.startsWith("127.") || ip.startsWith("192.168.") || ip.equals("localhost") || ip.equals("127.0.0.1")) {
      return "Local network IP";
    }
    return "Unknown";
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
      return "Unknown Device";
    if (userAgent.contains("Android")) {
      String model = extractAndroidModel(userAgent);
      return model != null ? model : "Android Device";
    }
    if (userAgent.contains("iPhone"))
      return "iPhone";
    if (userAgent.contains("iPad"))
      return "iPad";
    if (userAgent.contains("Macintosh") || userAgent.contains("Mac OS X"))
      return "Mac";
    if (userAgent.contains("Windows"))
      return "Windows Device";
    if (userAgent.contains("Linux"))
      return "Linux Device";
    return "Unknown Device";
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
    String safeDevice = (deviceModel == null || deviceModel.isBlank()) ? "Unknown Device" : deviceModel;
    String safeOs = (os == null || os.isBlank()) ? "Unknown OS" : os;
    String safeBrowser = (browser == null || browser.isBlank()) ? "Unknown Browser" : browser;
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
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    ensureUserGuid(user);
    long expiresInSeconds = resolveTokenTimeoutSeconds();
    DeviceSnapshot snapshot = new DeviceSnapshot(
        pending.getDeviceModel(),
        pending.getOs(),
        pending.getBrowser(),
        pending.getIpAddress(),
        pending.getLoginLocation());
    AuthSession session = buildSession(user, snapshot);
    String token = authTokenService.createToken(user.getId(), session, expiresInSeconds);
    operationLogService.logLogin(user, snapshot.deviceModel, snapshot.os, snapshot.browser, snapshot.ipAddress);
    return LoginResponse.success(token, expiresInSeconds);
  }

  private AuthSession buildSession(UserEntity user, DeviceSnapshot snapshot) {
    AuthSession session = new AuthSession();
    session.setDeviceModel(snapshot.deviceModel);
    session.setOs(snapshot.os);
    session.setBrowser(snapshot.browser);
    session.setIpAddress(snapshot.ipAddress);
    session.setLoginLocation(snapshot.loginLocation);
    session.getAttributes().put("loginId", user.getId());
    session.getAttributes().put("userName", user.getName());
    session.getAttributes().put("account", user.getAccount());
    session.getAttributes().put("userGuid", user.getGuid());
    session.getAttributes().put("loginTime", System.currentTimeMillis());
    session.setDeviceId(buildDeviceInfo(snapshot.deviceModel, snapshot.os, snapshot.browser));
    return session;
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
    long userId = authContext.requireUserId();
    UserEntity user = Optional.ofNullable(userMapper.selectById(userId))
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    ensureUserGuid(user);
    List<String> roles = permissionFacade.getEffectiveRoles(userId);
    List<String> perms = permissionFacade.getEffectivePermissions(userId);
    String avatar = normalizeAvatar(user.getAvatar());
    UserInfoResponse resp = new UserInfoResponse(user.getName(), avatar, roles, perms);
    resp.setId(user.getId());
    resp.setGuid(user.getGuid());
    resp.setAssignedRoles(permissionFacade.getAssignedRoles(userId));
    resp.setRoleSimulated(permissionFacade.isAssumed(userId));
    resp.setOrgUnitNames(orgUnitMapper.selectNamesByUserId(userId));
    return resp;
  }

  public UserProfileResponse currentUserProfile() {
    long userId = authContext.requireUserId();
    UserEntity user = Optional.ofNullable(userMapper.selectById(userId))
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    return enrichProfile(toProfile(user), userId);
  }

  @Transactional
  public UserProfileResponse updateCurrentUserProfile(UserProfileUpdateRequest req) {
    long userId = authContext.requireUserId();
    UserEntity u = Optional.ofNullable(userMapper.selectById(userId))
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    String oldAvatar = u.getAvatar();
    boolean documentFieldsTouched = false;
    if (req.getName() != null)
      u.setName(req.getName());
    if (req.getMobile() != null && !SensitiveMaskUtil.isMasked(req.getMobile()))
      u.setMobile(req.getMobile());
    if (req.getPhone() != null && !SensitiveMaskUtil.isMasked(req.getPhone()))
      u.setPhone(req.getPhone());
    if (req.getEmail() != null && !SensitiveMaskUtil.isMasked(req.getEmail()))
      u.setEmail(req.getEmail());
    if (req.getIdCard() != null && !SensitiveMaskUtil.isMasked(req.getIdCard())) {
      u.setIdCard(req.getIdCard());
      documentFieldsTouched = true;
    }
    if (req.getIdType() != null) {
      u.setIdType(req.getIdType());
      documentFieldsTouched = true;
    }
    if (req.getIdValidFrom() != null) {
      u.setIdValidFrom(req.getIdValidFrom());
      documentFieldsTouched = true;
    }
    if (req.getIdValidTo() != null) {
      u.setIdValidTo(req.getIdValidTo());
      documentFieldsTouched = true;
    }
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
    if (req.getProvinceId() != null)
      u.setProvinceId(req.getProvinceId());
    if (req.getProvince() != null)
      u.setProvince(req.getProvince());
    if (req.getCityId() != null)
      u.setCityId(req.getCityId());
    if (req.getCity() != null)
      u.setCity(req.getCity());
    if (req.getDistrictId() != null)
      u.setDistrictId(req.getDistrictId());
    if (req.getDistrict() != null)
      u.setDistrict(req.getDistrict());
    if (req.getTownId() != null)
      u.setTownId(req.getTownId());
    if (req.getTown() != null)
      u.setTown(req.getTown());
    if (req.getStreetId() != null)
      u.setStreetId(req.getStreetId());
    if (req.getStreet() != null)
      u.setStreet(req.getStreet());
    if (req.getZipCode() != null)
      u.setZipCode(req.getZipCode());
    if (req.getAddress() != null)
      u.setAddress(req.getAddress());
    if (req.getIntroduction() != null)
      u.setIntroduction(req.getIntroduction());
    if (req.getAvatar() != null)
      u.setAvatar(req.getAvatar());
    if (req.getTags() != null)
      u.setTags(req.getTags());
    if (documentFieldsTouched) {
      validateDocumentInfo(u);
    }
    ensureUserGuid(u);
    saveUser(u);

    updateSessionUserName(u.getName());
    if (req.getAvatar() != null && oldAvatar != null && !oldAvatar.equals(req.getAvatar())) {
      deleteUploadFile(oldAvatar);
    }
    operationLogService.log("UPDATE", "profile", "update profile");

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
    r.setIdType(u.getIdType());
    r.setIdValidFrom(u.getIdValidFrom());
    r.setIdValidTo(u.getIdValidTo());
    r.setSeat(u.getSeat());
    r.setEntity(u.getEntity());
    r.setLeader(u.getLeader());
    r.setPosition(u.getPosition());
    r.setJoinDay(u.getJoinDay());
    r.setTeam(u.getTeam());
    r.setGender(u.getGender());
    r.setNickname(u.getNickname());
    r.setProvinceId(u.getProvinceId());
    r.setProvince(u.getProvince());
    r.setCityId(u.getCityId());
    r.setCity(u.getCity());
    r.setDistrictId(u.getDistrictId());
    r.setDistrict(u.getDistrict());
    r.setTownId(u.getTownId());
    r.setTown(u.getTown());
    r.setStreetId(u.getStreetId());
    r.setStreet(u.getStreet());
    r.setZipCode(u.getZipCode());
    r.setAddress(u.getAddress());
    r.setIntroduction(u.getIntroduction());
    r.setAvatar(normalizeAvatar(u.getAvatar()));
    r.setTags(u.getTags());
    fillProfileCompleteness(r);
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
      throw new IllegalArgumentException("New passwords do not match");
    }
    // 闂傚倸鍊风粈渚€骞栭銈囩煋闁绘垶鏋荤紞鏍ь熆鐠虹尨鍔熼柡鍡愬€曢湁闁挎繂鎳忛幆鍫ユ倵閸偆鍙€闁哄被鍊栭幈銊╁箛椤戣棄浜炬俊銈呮噹閺勩儵鏌ｅΟ鑲╁笡闁稿绲介湁闁挎繂娴傞悞楣冩煃闁垮顥堥柡宀嬬秮婵″爼宕ㄩ鍛紗闁诲孩顔栭崰鏇犲垝濞嗘挸鏋侀柟鍓х帛閸嬫劙姊婚崼鐔恒€掗柣鎾村灴濮婄粯鎷呴崨濠冨創闂佺锕﹂幊鎾烩€﹂崹顕呮建闁逞屽墴楠炲棙绗熼埀顒勭嵁鐎ｎ喗鏅滈柦妯侯槸婢规挸鈹戦悙鑸靛涧缂佹彃娼￠幃娲棘濞嗗墽鍔烽梺瑙勫婢ф鎮?
    passwordPolicyService.validate(req.getNewPassword());

    long userId = authContext.requireUserId();
    UserEntity u = Optional.ofNullable(userMapper.selectById(userId))
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (!BCrypt.checkpw(req.getOldPassword(), u.getPasswordHash())) {
      throw new IllegalArgumentException("Current password is incorrect");
    }

    u.setPasswordHash(BCrypt.hashpw(req.getNewPassword(), BCrypt.gensalt()));
    ensureUserGuid(u);
    saveUser(u);

    operationLogService.log("UPDATE", "password", "user changed password");
    return true;
  }

  @Transactional
  public boolean resetPassword(ForgotPasswordRequest req) {
    String account = normalizeAccount(req.getAccount());
    if (!req.getNewPassword().equals(req.getConfirmPassword())) {
      throw new IllegalArgumentException("New passwords do not match");
    }
    // 闂傚倸鍊风粈渚€骞栭銈囩煋闁绘垶鏋荤紞鏍ь熆鐠虹尨鍔熼柡鍡愬€曢湁闁挎繂鎳忛幆鍫ユ倵閸偆鍙€闁哄被鍊栭幈銊╁箛椤戣棄浜炬俊銈呮噹閺勩儵鏌ｅΟ鑲╁笡闁稿绲介湁闁挎繂娴傞悞楣冩煃闁垮顥堥柡宀嬬秮婵″爼宕ㄩ鍛紗闁诲孩顔栭崰鏇犲垝濞嗘挸鏋侀柟鍓х帛閸嬫劙姊婚崼鐔恒€掗柣鎾村灴濮婄粯鎷呴崨濠冨創闂佺锕﹂幊鎾烩€﹂崹顕呮建闁逞屽墴楠炲棙绗熼埀顒勭嵁鐎ｎ喗鏅滈柦妯侯槸婢规挸鈹戦悙鑸靛涧缂佹彃娼￠幃娲棘濞嗗墽鍔烽梺瑙勫婢ф鎮?
    passwordPolicyService.validate(req.getNewPassword());
    VerificationSetting setting = verificationSettingService.getDecryptedCopy();
    ensureSmsEnabled(setting);

    String phone = normalizePhone(req.getPhone());
    String code = normalizeCode(req.getCode());
    boolean verified = smsCodeService.verify(phone, code);
    if (!verified) {
      throw new IllegalArgumentException("Verification code invalid or expired");
    }

    UserEntity user = Optional.ofNullable(userMapper.selectByAccount(account))
        .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    String normalizedPhone = normalizePhone(user.getMobile() != null ? user.getMobile() : user.getPhone());
    if (!phone.equals(normalizedPhone)) {
      throw new IllegalArgumentException("Phone does not match account");
    }

    user.setPasswordHash(BCrypt.hashpw(req.getNewPassword(), BCrypt.gensalt()));
    ensureUserGuid(user);
    saveUser(user);
    operationLogService.log("UPDATE", "password-reset", "user reset password via sms");
    return true;
  }

  @Transactional
  public UserInfoResponse switchRoles(RoleSwitchRequest req) {
    long userId = authContext.requireUserId();
    if (!permissionFacade.isAdminAccount(userId)) {
      throw new IllegalArgumentException("Only admin can switch demo roles");
    }
    if (req.getRoles() == null || req.getRoles().isEmpty()) {
      permissionFacade.clearAssumedRoles(userId);
      return currentUserInfo();
    }
    permissionFacade.assumeRoles(userId, req.getRoles());
    return currentUserInfo();
  }

  public List<String> listAllRoleNames() {
    long userId = authContext.requireUserId();
    if (!permissionFacade.isAdminAccount(userId)) {
      throw new IllegalArgumentException("Only admin can view roles");
    }
    return roleMapper.selectAll().stream().map(r -> r.getName()).toList();
  }

  public boolean logout() {
    if (authContext.isAuthenticated()) {
      long userId = authContext.requireUserId();
      permissionFacade.clearAssumedRoles(userId);
      String token = authContext.getToken();
      if (token != null) {
        authTokenService.removeToken(token);
      }
    }
    return true;
  }

  @Transactional
  public boolean register(RegisterRequest req) {
    Boolean captchaEnabled = securitySettingService.getOrCreate().getCaptchaEnabled();
    if (Boolean.TRUE.equals(captchaEnabled)) {
      if (req.getCaptchaId() == null || req.getCaptchaId().isBlank() || req.getCaptchaCode() == null || req.getCaptchaCode().isBlank()) {
        throw new IllegalArgumentException("Verification code is required");
      }
      String captchaCode = normalizeCode(req.getCaptchaCode());
      boolean ok = captchaService.verify(req.getCaptchaId(), captchaCode);
      if (!ok) {
        throw new IllegalArgumentException("Verification code invalid or expired");
      }
    }
    if (!req.getPassword().equals(req.getConfirmPassword())) {
      throw new IllegalArgumentException("Passwords do not match");
    }
    passwordPolicyService.validate(req.getPassword());

    String account = normalizeAccount(req.getAccount());
    if (account.isBlank()) {
      throw new IllegalArgumentException(MSG_ACCOUNT_REQUIRED);
    }
    if (userMapper.countByAccount(account) > 0) {
      throw new IllegalArgumentException(MSG_ACCOUNT_EXISTS);
    }

    UserEntity e = new UserEntity();
    e.setAccount(account);
    e.setGuid(UUID.randomUUID().toString());
    e.setName(account);
    e.setPasswordHash(BCrypt.hashpw(req.getPassword(), BCrypt.gensalt()));
    e.setStatus(1);

    try {
      e = saveUser(e);
    } catch (DataIntegrityViolationException ex) {
      throw new IllegalArgumentException(resolveRegisterIntegrityMessage(account, ex));
    }

    authDao.replaceUserRoles(e.getId(), List.of("user"));
    return true;
  }

  private String resolveRegisterIntegrityMessage(String account, DataIntegrityViolationException ex) {
    if (userMapper.countByAccount(account) > 0) {
      return MSG_ACCOUNT_EXISTS;
    }
    String detail = resolveRootMessage(ex).toLowerCase(Locale.ROOT);
    if (looksLikeAccountDuplicate(detail)) {
      return MSG_ACCOUNT_EXISTS;
    }
    return MSG_REGISTER_RETRY;
  }

  private String resolveRootMessage(Throwable throwable) {
    if (throwable == null) return "";
    Throwable cursor = throwable;
    while (cursor.getCause() != null) {
      cursor = cursor.getCause();
    }
    return cursor.getMessage() == null ? "" : cursor.getMessage();
  }

  private boolean looksLikeAccountDuplicate(String message) {
    if (message == null || message.isBlank()) return false;
    boolean duplicateViolation = message.contains("duplicate")
      || message.contains("unique constraint")
      || message.contains("unique index")
      || message.contains("unique key")
      || message.contains("duplicate key");
    if (!duplicateViolation) return false;
    return message.contains("account")
      || message.contains("users.account")
      || message.contains("users_account")
      || message.contains("uk_users_account")
      || message.contains("idx_users_account")
      || message.contains("uq_users_account");
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
    return 2592000L;
  }

  private boolean hasActiveSession(long userId) {
    List<String> tokens = authTokenService.listUserTokens(userId);
    long now = System.currentTimeMillis();
    long idleTimeoutMs = resolveSessionTimeoutMs();
    for (String token : tokens) {
      AuthSession session = authTokenService.getSession(token);
      if (session == null) {
        authTokenService.removeToken(token);
        continue;
      }
      if (isSessionExpired(session, now, idleTimeoutMs)) {
        authTokenService.removeToken(token);
        continue;
      }
      return true;
    }
    return false;
  }

  private boolean isSessionExpired(AuthSession session, long now, long idleTimeoutMs) {
    if (session.getExpiresAt() > 0 && session.getExpiresAt() <= now) {
      return true;
    }
    Long lastAccess = readLongAttribute(session, "lastAccessTime");
    if (lastAccess == null) {
      lastAccess = readLongAttribute(session, "loginTime");
    }
    if (idleTimeoutMs > 0 && lastAccess != null && now - lastAccess > idleTimeoutMs) {
      return true;
    }
    return false;
  }

  private long resolveSessionTimeoutMs() {
    Integer minutes = securitySettingService.getOrCreate().getSessionTimeoutMinutes();
    if (minutes != null && minutes > 0) {
      return minutes * 60_000L;
    }
    return 0L;
  }

  private Long readLongAttribute(AuthSession session, String key) {
    Object value = session.getAttributes().get(key);
    if (value instanceof Number) {
      return ((Number) value).longValue();
    }
    if (value instanceof String) {
      try {
        return Long.parseLong((String) value);
      } catch (NumberFormatException ignored) {
        return null;
      }
    }
    return null;
  }

  private void updateSessionUserName(String userName) {
    String token = authContext.getToken();
    if (token == null) return;
    AuthSession session = authTokenService.getSession(token);
    if (session == null) return;
    session.getAttributes().put("userName", userName);
    authTokenService.updateSession(token, session);
  }

}
