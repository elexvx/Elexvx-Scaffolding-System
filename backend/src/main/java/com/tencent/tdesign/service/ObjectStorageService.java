package com.tencent.tdesign.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.tencent.tdesign.dto.StorageSettingRequest;
import com.tencent.tdesign.entity.StorageSetting;
import com.tencent.tdesign.mapper.StorageSettingMapper;
import com.tencent.tdesign.vo.StorageSettingResponse;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ObjectStorageService {
  private static final Logger log = LoggerFactory.getLogger(ObjectStorageService.class);

  private final StorageSettingMapper mapper;
  private final FileTokenService fileTokenService;

  public ObjectStorageService(StorageSettingMapper mapper, FileTokenService fileTokenService) {
    this.mapper = mapper;
    this.fileTokenService = fileTokenService;
  }

  public StorageSettingResponse currentSetting() {
    return StorageSettingResponse.from(loadSetting());
  }

  public StorageSetting save(StorageSettingRequest req) {
    StorageSetting setting = loadSetting();
    apply(setting, req, true);
    validate(setting, true);
    StorageSetting saved = saveSetting(setting);
    log.info("Object storage settings updated. provider={}, bucket={}", saved.getProvider(), saved.getBucket());
    return saved;
  }

  public void test(StorageSettingRequest req) {
    StorageSetting tmp = buildFromRequest(req, true);
    validate(tmp, false);
    switch (tmp.getProviderEnum()) {
      case LOCAL -> testLocal(tmp);
      case ALIYUN -> testAliyun(tmp);
      case TENCENT -> testTencent(tmp);
    }
  }

  public String upload(MultipartFile file, String folder, String page) throws IOException {
    if (file == null || file.isEmpty()) throw new IllegalArgumentException("上传文件不能为空");
    StorageSetting setting = loadSetting();
    if (setting.getProvider() == null) {
      setting.setProvider(StorageSetting.Provider.LOCAL.name());
    }
    validate(setting, false);
    String safeFolder = normalizeFolder(folder);
    String safePage = normalizePage(page);
    if (safePage == null) safePage = "general";
    String objectKey = buildObjectKey(setting, safeFolder, safePage, file.getOriginalFilename());
    try (InputStream in = file.getInputStream()) {
      uploadWithStream(setting, objectKey, in, file.getSize());
    }
    return fileTokenService.buildAccessUrl(setting.getProviderEnum(), objectKey);
  }

  public String uploadStream(InputStream stream, long size, String originalName, String folder, String page) throws IOException {
    if (stream == null) throw new IllegalArgumentException("上传流不能为空");
    StorageSetting setting = loadSetting();
    if (setting.getProvider() == null) {
      setting.setProvider(StorageSetting.Provider.LOCAL.name());
    }
    validate(setting, false);
    String safeFolder = normalizeFolder(folder);
    String safePage = normalizePage(page);
    if (safePage == null) safePage = "general";
    String objectKey = buildObjectKey(setting, safeFolder, safePage, originalName);
    try (InputStream in = stream) {
      uploadWithStream(setting, objectKey, in, size);
    }
    return fileTokenService.buildAccessUrl(setting.getProviderEnum(), objectKey);
  }

  private StorageSetting buildFromRequest(StorageSettingRequest req, boolean reuseSecret) {
    StorageSetting setting = loadSetting();
    apply(setting, req, reuseSecret);
    return setting;
  }

  private StorageSetting loadSetting() {
    StorageSetting setting = mapper.selectTop();
    return setting != null ? setting : new StorageSetting();
  }

  private StorageSetting saveSetting(StorageSetting setting) {
    if (setting.getId() == null) {
      mapper.insert(setting);
    } else {
      mapper.update(setting);
    }
    return setting;
  }

  private void apply(StorageSetting target, StorageSettingRequest req, boolean reuseSecret) {
    if (req.getProvider() != null) target.setProvider(req.getProvider().trim());
    if (req.getBucket() != null) target.setBucket(blankToNull(req.getBucket()));
    if (req.getRegion() != null) target.setRegion(blankToNull(req.getRegion()));
    if (req.getEndpoint() != null) target.setEndpoint(blankToNull(req.getEndpoint()));
    if (req.getAccessKey() != null) target.setAccessKey(blankToNull(req.getAccessKey()));
    if (req.getCustomDomain() != null) target.setCustomDomain(trimSlash(req.getCustomDomain()));
    if (req.getPathPrefix() != null) target.setPathPrefix(normalizePrefix(req.getPathPrefix()));
    if (req.getPublicRead() != null) target.setPublicRead(req.getPublicRead());

    if (req.getSecretKey() != null && !req.getSecretKey().isBlank()) {
      target.setSecretKey(req.getSecretKey().trim());
    } else if (!Boolean.TRUE.equals(req.getReuseSecret())) {
      target.setSecretKey(null);
    }
  }

  private void validate(StorageSetting setting, boolean forSave) {
    StorageSetting.Provider provider = setting.getProviderEnum();
    if (provider == StorageSetting.Provider.LOCAL) return;
    if (provider == StorageSetting.Provider.ALIYUN) {
      require(setting.getBucket(), "bucket");
      require(setting.getEndpoint(), "endpoint");
      require(setting.getAccessKey(), "accessKey");
      require(setting.getSecretKey(), "secretKey");
    } else if (provider == StorageSetting.Provider.TENCENT) {
      require(setting.getBucket(), "bucket");
      require(setting.getRegion(), "region");
      require(setting.getAccessKey(), "accessKey");
      require(setting.getSecretKey(), "secretKey");
    }
  }

  private void require(String value, String field) {
    if (value == null || value.isBlank()) throw new IllegalArgumentException("字段 " + field + " 不能为空");
  }

  private String normalizeFolder(String raw) {
    String safe = (raw == null ? "" : raw.trim());
    safe = safe.replaceAll("[^a-zA-Z0-9_-]", "");
    return safe.isEmpty() ? "business" : safe;
  }

  private String normalizePrefix(String raw) {
    if (raw == null) return null;
    String cleaned = raw.trim().replace("\\", "/");
    cleaned = cleaned.replaceAll("^/+|/+$", "");
    return cleaned.isEmpty() ? null : cleaned;
  }

  private String trimSlash(String value) {
    if (value == null) return null;
    String v = value.trim();
    if (v.endsWith("/")) v = v.substring(0, v.length() - 1);
    return v.isEmpty() ? null : v;
  }

  private String normalizePage(String raw) {
    if (raw == null) return null;
    String cleaned = raw.trim().replace("\\", "/");
    if (cleaned.isEmpty()) return null;
    String[] parts = cleaned.split("/");
    StringBuilder sb = new StringBuilder();
    for (String part : parts) {
      String safe = part.replaceAll("[^a-zA-Z0-9_-]", "");
      if (safe.isEmpty()) continue;
      if (sb.length() > 0) sb.append('/');
      sb.append(safe);
    }
    return sb.length() == 0 ? null : sb.toString();
  }

  private void uploadWithStream(StorageSetting setting, String objectKey, InputStream stream, long size) throws IOException {
    switch (setting.getProviderEnum()) {
      case LOCAL -> uploadLocalStream(setting, stream, objectKey);
      case ALIYUN -> uploadAliyunStream(setting, stream, objectKey);
      case TENCENT -> uploadTencentStream(setting, stream, objectKey, size);
    }
  }

  private String buildObjectKey(StorageSetting setting, String folder, String page, String originalName) {
    String ext = "";
    if (originalName != null) {
      int idx = originalName.lastIndexOf('.') ;
      if (idx > -1) ext = originalName.substring(idx);
    }
    String datePath = LocalDate.now().toString();
    String name = UUID.randomUUID().toString().replaceAll("-", "");
    String prefix = setting.getPathPrefix();
    StringBuilder sb = new StringBuilder();
    if (isNotBlank(prefix)) sb.append(prefix).append('/');
    sb.append(folder);
    if (isNotBlank(page)) sb.append('/').append(page);
    sb.append('/').append(datePath).append('/').append(name).append(ext);
    return sb.toString().replaceAll("/+", "/");
  }

  private void uploadLocalStream(StorageSetting setting, InputStream stream, String objectKey) throws IOException {
    Path uploadRoot = Paths.get(System.getProperty("user.dir"), "uploads").toAbsolutePath();
    Path target = uploadRoot.resolve(objectKey).normalize();
    if (!target.startsWith(uploadRoot)) throw new IllegalArgumentException("非法路径");
    Files.createDirectories(target.getParent());
    Files.copy(stream, target, StandardCopyOption.REPLACE_EXISTING);
  }

  private void uploadAliyunStream(StorageSetting setting, InputStream stream, String objectKey) throws IOException {
    String endpoint = formatEndpoint(setting.getEndpoint());
    OSS client = new OSSClientBuilder().build(endpoint, setting.getAccessKey(), setting.getSecretKey());
    try {
      client.putObject(setting.getBucket(), objectKey, stream);
    } finally {
      safeShutdown(client);
    }
  }

  private void uploadTencentStream(StorageSetting setting, InputStream stream, String objectKey, long size) throws IOException {
    ClientConfig config = new ClientConfig(new Region(setting.getRegion()));
    config.setHttpProtocol(HttpProtocol.https);
    COSClient client = new COSClient(new com.qcloud.cos.auth.BasicCOSCredentials(setting.getAccessKey(), setting.getSecretKey()), config);
    try {
      ObjectMetadata metadata = new ObjectMetadata();
      if (size >= 0) {
        metadata.setContentLength(size);
      }
      PutObjectRequest request = new PutObjectRequest(setting.getBucket(), objectKey, stream, metadata);
      client.putObject(request);
    } finally {
      client.shutdown();
    }
  }

  public FileStream openStream(FileTokenService.TokenPayload payload) throws IOException {
    return openStream(payload, null, null);
  }

  public FileMeta stat(FileTokenService.TokenPayload payload) throws IOException {
    if (payload == null) throw new IllegalArgumentException("Invalid file token");
    StorageSetting.Provider provider = payload.getProvider();
    StorageSetting setting = loadSetting();
    if (setting.getProvider() == null) {
      setting.setProvider(StorageSetting.Provider.LOCAL.name());
    }
    if (provider != setting.getProviderEnum()) {
      log.warn("File token provider mismatch. token={}, current={}", provider, setting.getProviderEnum());
    }
    return switch (provider) {
      case LOCAL -> statLocal(payload.getObjectKey());
      case ALIYUN -> statAliyun(setting, payload.getObjectKey());
      case TENCENT -> statTencent(setting, payload.getObjectKey());
    };
  }

  public FileStream openStream(FileTokenService.TokenPayload payload, Long start, Long end) throws IOException {
    if (payload == null) throw new IllegalArgumentException("Invalid file token");
    StorageSetting.Provider provider = payload.getProvider();
    StorageSetting setting = loadSetting();
    if (setting.getProvider() == null) {
      setting.setProvider(StorageSetting.Provider.LOCAL.name());
    }
    if (provider != setting.getProviderEnum()) {
      log.warn("File token provider mismatch. token={}, current={}", provider, setting.getProviderEnum());
    }
    return switch (provider) {
      case LOCAL -> openLocalStream(payload.getObjectKey(), start, end);
      case ALIYUN -> openAliyunStream(setting, payload.getObjectKey(), start, end);
      case TENCENT -> openTencentStream(setting, payload.getObjectKey(), start, end);
    };
  }

  public void deleteByUrl(String url) {
    if (url == null || url.isBlank()) return;
    String token = fileTokenService.extractToken(url);
    if (token != null) {
      try {
        FileTokenService.TokenPayload payload = fileTokenService.decrypt(token);
        if (payload.getProvider() != StorageSetting.Provider.LOCAL) return;
        deleteLocalByKey(payload.getObjectKey());
        return;
      } catch (Exception e) {
        log.debug("Failed to delete file by token", e);
        return;
      }
    }
    deleteLocalByUrl(url);
  }

  private FileStream openLocalStream(String objectKey, Long start, Long end) throws IOException {
    Path uploadRoot = Paths.get(System.getProperty("user.dir"), "uploads").toAbsolutePath().normalize();
    Path target = uploadRoot.resolve(objectKey).normalize();
    if (!target.startsWith(uploadRoot)) throw new IllegalArgumentException("Illegal path");
    if (!Files.exists(target)) throw new IllegalArgumentException("File not found");
    long size = Files.size(target);
    String contentType = detectContentType(target.getFileName().toString());
    if (start == null && end == null) {
      return FileStream.full(Files.newInputStream(target), size, contentType);
    }
    Range range = normalizeRange(start, end, size);
    SeekableByteChannel channel = Files.newByteChannel(target, StandardOpenOption.READ);
    channel.position(range.start());
    InputStream input = new BoundedInputStream(Channels.newInputStream(channel), range.length());
    return FileStream.range(input, range.length(), size, contentType);
  }

  private FileStream openAliyunStream(StorageSetting setting, String objectKey, Long start, Long end) throws IOException {
    String endpoint = formatEndpoint(setting.getEndpoint());
    OSS client = new OSSClientBuilder().build(endpoint, setting.getAccessKey(), setting.getSecretKey());
    if (start == null && end == null) {
      OSSObject object = client.getObject(setting.getBucket(), objectKey);
      long length = object.getObjectMetadata() == null ? -1 : object.getObjectMetadata().getContentLength();
      String contentType = object.getObjectMetadata() == null ? null : object.getObjectMetadata().getContentType();
      InputStream input = new ManagedInputStream(object.getObjectContent(), () -> {
        try {
          object.close();
        } catch (Exception ignore) {
        }
        safeShutdown(client);
      });
      return FileStream.full(input, length, defaultContentType(contentType, objectKey));
    }

    com.aliyun.oss.model.ObjectMetadata meta = client.getObjectMetadata(setting.getBucket(), objectKey);
    long total = meta == null ? -1 : meta.getContentLength();
    Range range = normalizeRange(start, end, total);
    GetObjectRequest req = new GetObjectRequest(setting.getBucket(), objectKey);
    req.setRange(range.start(), range.end());
    OSSObject object = client.getObject(req);
    String contentType = meta == null ? null : meta.getContentType();
    InputStream input = new ManagedInputStream(object.getObjectContent(), () -> {
      try {
        object.close();
      } catch (Exception ignore) {
      }
      safeShutdown(client);
    });
    return FileStream.range(input, range.length(), total, defaultContentType(contentType, objectKey));
  }

  private FileStream openTencentStream(StorageSetting setting, String objectKey, Long start, Long end) throws IOException {
    ClientConfig config = new ClientConfig(new Region(setting.getRegion()));
    config.setHttpProtocol(HttpProtocol.https);
    COSClient client = new COSClient(new com.qcloud.cos.auth.BasicCOSCredentials(setting.getAccessKey(), setting.getSecretKey()), config);
    if (start == null && end == null) {
      COSObject object = client.getObject(setting.getBucket(), objectKey);
      long length = object.getObjectMetadata() == null ? -1 : object.getObjectMetadata().getContentLength();
      String contentType = object.getObjectMetadata() == null ? null : object.getObjectMetadata().getContentType();
      InputStream input = new ManagedInputStream(object.getObjectContent(), () -> {
        try {
          object.close();
        } catch (Exception ignore) {
        }
        client.shutdown();
      });
      return FileStream.full(input, length, defaultContentType(contentType, objectKey));
    }

    ObjectMetadata meta = client.getObjectMetadata(setting.getBucket(), objectKey);
    long total = meta == null ? -1 : meta.getContentLength();
    Range range = normalizeRange(start, end, total);
    com.qcloud.cos.model.GetObjectRequest req = new com.qcloud.cos.model.GetObjectRequest(setting.getBucket(), objectKey);
    req.setRange(range.start(), range.end());
    COSObject object = client.getObject(req);
    String contentType = meta == null ? null : meta.getContentType();
    InputStream input = new ManagedInputStream(object.getObjectContent(), () -> {
      try {
        object.close();
      } catch (Exception ignore) {
      }
      client.shutdown();
    });
    return FileStream.range(input, range.length(), total, defaultContentType(contentType, objectKey));
  }

  private FileMeta statLocal(String objectKey) throws IOException {
    Path uploadRoot = Paths.get(System.getProperty("user.dir"), "uploads").toAbsolutePath().normalize();
    Path target = uploadRoot.resolve(objectKey).normalize();
    if (!target.startsWith(uploadRoot)) throw new IllegalArgumentException("Illegal path");
    if (!Files.exists(target)) throw new IllegalArgumentException("File not found");
    long size = Files.size(target);
    String contentType = detectContentType(target.getFileName().toString());
    return new FileMeta(size, contentType);
  }

  private FileMeta statAliyun(StorageSetting setting, String objectKey) {
    String endpoint = formatEndpoint(setting.getEndpoint());
    OSS client = new OSSClientBuilder().build(endpoint, setting.getAccessKey(), setting.getSecretKey());
    try {
      com.aliyun.oss.model.ObjectMetadata meta = client.getObjectMetadata(setting.getBucket(), objectKey);
      long size = meta == null ? -1 : meta.getContentLength();
      String contentType = meta == null ? null : meta.getContentType();
      return new FileMeta(size, defaultContentType(contentType, objectKey));
    } finally {
      safeShutdown(client);
    }
  }

  private FileMeta statTencent(StorageSetting setting, String objectKey) {
    ClientConfig config = new ClientConfig(new Region(setting.getRegion()));
    config.setHttpProtocol(HttpProtocol.https);
    COSClient client = new COSClient(new com.qcloud.cos.auth.BasicCOSCredentials(setting.getAccessKey(), setting.getSecretKey()), config);
    try {
      ObjectMetadata meta = client.getObjectMetadata(setting.getBucket(), objectKey);
      long size = meta == null ? -1 : meta.getContentLength();
      String contentType = meta == null ? null : meta.getContentType();
      return new FileMeta(size, defaultContentType(contentType, objectKey));
    } finally {
      client.shutdown();
    }
  }

  private Range normalizeRange(Long start, Long end, long totalLength) {
    if (totalLength < 0) throw new IllegalArgumentException("Unknown content length");
    long s = start == null ? 0 : start;
    long e = end == null ? (totalLength - 1) : end;
    if (s < 0) s = 0;
    if (e < 0) e = 0;
    if (s >= totalLength) throw new IllegalArgumentException("Range start out of bounds");
    if (e >= totalLength) e = totalLength - 1;
    if (e < s) throw new IllegalArgumentException("Invalid range");
    return new Range(s, e);
  }

  private void deleteLocalByKey(String objectKey) {
    if (objectKey == null || objectKey.isBlank()) return;
    Path uploadRoot = Paths.get(System.getProperty("user.dir"), "uploads").toAbsolutePath().normalize();
    Path target = uploadRoot.resolve(objectKey).normalize();
    if (!target.startsWith(uploadRoot)) return;
    try {
      if (Files.isRegularFile(target)) {
        Files.deleteIfExists(target);
      }
    } catch (Exception ignored) {
    }
  }

  private void deleteLocalByUrl(String url) {
    String raw = String.valueOf(url).trim();
    if (raw.isEmpty()) return;
    if (raw.startsWith("http://") || raw.startsWith("https://")) return;
    String clean = raw.split("\\?")[0];
    if (clean.startsWith("/api/")) {
      clean = clean.substring(4);
    }
    if (!clean.startsWith("/uploads/")) return;
    String relative = clean.substring("/uploads/".length());
    if (relative.isEmpty()) return;
    deleteLocalByKey(relative);
  }

  private String detectContentType(String filename) {
    String type = null;
    try {
      type = Files.probeContentType(Paths.get(filename));
    } catch (Exception ignore) {
    }
    if (type == null) {
      type = URLConnection.guessContentTypeFromName(filename);
    }
    return defaultContentType(type, filename);
  }

  private String defaultContentType(String value, String fallbackKey) {
    if (value != null && !value.isBlank()) return value;
    String guess = URLConnection.guessContentTypeFromName(fallbackKey);
    return guess == null ? "application/octet-stream" : guess;
  }

  public static class FileMeta {
    private final long contentLength;
    private final String contentType;

    public FileMeta(long contentLength, String contentType) {
      this.contentLength = contentLength;
      this.contentType = contentType;
    }

    public long getContentLength() {
      return contentLength;
    }

    public String getContentType() {
      return contentType;
    }
  }

  private record Range(long start, long end) {
    long length() {
      return end - start + 1;
    }
  }

  public static class FileStream {
    private final InputStream inputStream;
    private final long contentLength;
    private final long totalLength;
    private final String contentType;

    private FileStream(InputStream inputStream, long contentLength, long totalLength, String contentType) {
      this.inputStream = inputStream;
      this.contentLength = contentLength;
      this.totalLength = totalLength;
      this.contentType = contentType;
    }

    public static FileStream full(InputStream inputStream, long totalLength, String contentType) {
      return new FileStream(inputStream, totalLength, totalLength, contentType);
    }

    public static FileStream range(InputStream inputStream, long contentLength, long totalLength, String contentType) {
      return new FileStream(inputStream, contentLength, totalLength, contentType);
    }

    public InputStream getInputStream() {
      return inputStream;
    }

    public long getContentLength() {
      return contentLength;
    }

    public long getTotalLength() {
      return totalLength;
    }

    public String getContentType() {
      return contentType;
    }
  }

  private static class ManagedInputStream extends FilterInputStream {
    private final AutoCloseable closeable;

    protected ManagedInputStream(InputStream in, AutoCloseable closeable) {
      super(in);
      this.closeable = closeable;
    }

    @Override
    public void close() throws IOException {
      try {
        super.close();
      } finally {
        if (closeable != null) {
          try {
            closeable.close();
          } catch (Exception ignore) {
          }
        }
      }
    }
  }

  private void testLocal(StorageSetting setting) {
    try {
      Path root = Paths.get(System.getProperty("user.dir"), "uploads");
      Files.createDirectories(root);
      Files.createDirectories(root.resolve("health-check"));
    } catch (IOException e) {
      log.warn("Local storage test failed", e);
      throw new IllegalArgumentException("本地存储测试失败: " + e.getMessage());
    }
  }

  private void testAliyun(StorageSetting setting) {
    String endpoint = formatEndpoint(setting.getEndpoint());
    OSS client = new OSSClientBuilder().build(endpoint, setting.getAccessKey(), setting.getSecretKey());
    try {
      if (!client.doesBucketExist(setting.getBucket())) {
        throw new IllegalArgumentException("阿里云 OSS Bucket 不存在: " + setting.getBucket());
      }
    } catch (Exception e) {
      log.warn("Object storage service A test failed", e);
      throw new IllegalArgumentException("阿里云 OSS 连接失败: " + e.getMessage());
    } finally {
      safeShutdown(client);
    }
  }

  private void testTencent(StorageSetting setting) {
    ClientConfig config = new ClientConfig(new Region(setting.getRegion()));
    config.setHttpProtocol(HttpProtocol.https);
    COSClient client = new COSClient(new com.qcloud.cos.auth.BasicCOSCredentials(setting.getAccessKey(), setting.getSecretKey()), config);
    try {
      if (!client.doesBucketExist(setting.getBucket())) {
        throw new IllegalArgumentException("腾讯云 COS Bucket 不存在: " + setting.getBucket());
      }
    } catch (Exception e) {
      log.warn("Object storage service B test failed", e);
      throw new IllegalArgumentException("腾讯云 COS 连接失败: " + e.getMessage());
    } finally {
      client.shutdown();
    }
  }

  private String formatEndpoint(String endpoint) {
    if (endpoint == null) return null;
    String value = endpoint.trim();
    if (!value.startsWith("http")) {
      value = "https://" + value;
    }
    return value;
  }

  private void safeShutdown(OSS client) {
    try {
      client.shutdown();
    } catch (Exception ignore) {
    }
  }

  private static class BoundedInputStream extends FilterInputStream {
    private long remaining;

    protected BoundedInputStream(InputStream in, long remaining) {
      super(in);
      this.remaining = remaining;
    }

    @Override
    public int read() throws IOException {
      if (remaining <= 0) return -1;
      int b = super.read();
      if (b >= 0) remaining--;
      return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      if (remaining <= 0) return -1;
      int allowed = (int) Math.min(len, remaining);
      int n = super.read(b, off, allowed);
      if (n > 0) remaining -= n;
      return n;
    }
  }

  private String blankToNull(String val) {
    if (val == null) return null;
    String v = val.trim();
    return v.isEmpty() ? null : v;
  }

  private boolean isNotBlank(String value) {
    return value != null && !value.trim().isEmpty();
  }
}
