package com.tencent.tdesign.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tdesign.dto.FileUploadInitRequest;
import com.tencent.tdesign.security.AuthContext;
import com.tencent.tdesign.vo.FileUploadSessionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;
import java.util.regex.Pattern;

@Service
public class FileChunkUploadService {
  private static final int DEFAULT_CHUNK_SIZE = 5 * 1024 * 1024;
  private static final int MIN_CHUNK_SIZE = 1 * 1024;
  private static final int MAX_CHUNK_SIZE = 20 * 1024 * 1024;
  private static final String DEFAULT_FOLDER = "business";
  private static final String META_FILE = "session.json";
  private static final int MAX_FINGERPRINT_LENGTH = 512;
  private static final Pattern UPLOAD_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{20,100}$");

  private final ObjectStorageService storageService;
  private final ObjectMapper objectMapper;
  private final Path chunkRoot;
  private final ConcurrentMap<String, Object> sessionLocks = new ConcurrentHashMap<>();
  private final AuthContext authContext;
  private final byte[] uploadIdSalt;

  public FileChunkUploadService(
    ObjectStorageService storageService,
    ObjectMapper objectMapper,
    AuthContext authContext,
    @Value("${tdesign.file.token-secret:}") String secret
  ) {
    this.storageService = storageService;
    this.objectMapper = objectMapper;
    this.authContext = authContext;
    String effective = secret == null ? "" : secret.trim();
    if (effective.isEmpty()) {
      effective = "tdesign-file-token-secret";
    }
    this.uploadIdSalt = effective.getBytes(StandardCharsets.UTF_8);
    this.chunkRoot = Paths.get(System.getProperty("user.dir"), "runtime", "upload-chunks").toAbsolutePath().normalize();
    try {
      Files.createDirectories(chunkRoot);
    } catch (IOException e) {
      throw new IllegalStateException("无法创建分片缓存目录", e);
    }
  }

  public FileUploadSessionResponse initSession(FileUploadInitRequest request) {
    long userId = authContext.requireUserId();
    UploadSession session = ensureSession(request, userId);
    return FileUploadSessionResponse.from(
      session.getUploadId(),
      session.getFileName(),
      session.getFileSize(),
      session.getChunkSize(),
      session.getTotalChunks(),
      session.getUploadedChunkList()
    );
  }

  public FileUploadSessionResponse getStatus(String uploadId) {
    UploadSession session = loadSession(uploadId);
    return FileUploadSessionResponse.from(
      session.getUploadId(),
      session.getFileName(),
      session.getFileSize(),
      session.getChunkSize(),
      session.getTotalChunks(),
      session.getUploadedChunkList()
    );
  }

  public void saveChunk(String uploadId, int chunkIndex, MultipartFile chunk) {
    Objects.requireNonNull(chunk, "chunk 不能为空");
    UploadSession session = loadSession(uploadId);
    validateChunkIndex(session, chunkIndex);
    if (chunk.isEmpty()) {
      throw new IllegalArgumentException("分片数据不能为空");
    }
    Path dir = sessionDir(uploadId);
    Path chunkFile = dir.resolve(chunkFileName(chunkIndex));
    try (InputStream in = chunk.getInputStream()) {
      Files.copy(in, chunkFile, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new IllegalStateException("保存分片失败", e);
    }
    synchronized (lockFor(uploadId)) {
      session.markChunkUploaded(chunkIndex);
      persistSession(session, dir);
    }
  }

  public String finalizeUpload(String uploadId, String folder, String page) {
    UploadSession session = loadSession(uploadId);
    if (!session.isComplete()) {
      throw new IllegalStateException("还有未上传的分片");
    }
    Path dir = sessionDir(uploadId);
    folder = StringUtils.hasText(folder) ? folder : DEFAULT_FOLDER;
    page = StringUtils.hasText(page) ? page : "console-download";
    try (InputStream stream = openCombinedStream(dir, session.totalChunks)) {
      String url = storageService.uploadStream(stream, session.fileSize, session.fileName, folder, page);
      deleteSessionDir(dir);
      sessionLocks.remove(uploadId);
      return url;
    } catch (IOException e) {
      throw new IllegalStateException("组合分片并上传失败", e);
    }
  }

  public boolean cancel(String uploadId) {
    Path dir = sessionDir(uploadId);
    boolean deleted = deleteSessionDir(dir);
    sessionLocks.remove(uploadId);
    return deleted;
  }

  private UploadSession ensureSession(FileUploadInitRequest request, long userId) {
    if (!StringUtils.hasText(request.getFingerprint())) {
      throw new IllegalArgumentException("文件指纹不能为空");
    }
    String fingerprint = request.getFingerprint().trim();
    if (fingerprint.length() > MAX_FINGERPRINT_LENGTH) {
      throw new IllegalArgumentException("文件指纹过长");
    }
    String uploadId = buildUploadId(userId, fingerprint);
    Path dir = sessionDir(uploadId);
    synchronized (lockFor(uploadId)) {
    UploadSession existing = loadSessionIfExists(dir);
    int chunkSize = normalizeChunkSize(request.getChunkSize());
      long fileSize = request.getFileSize();
      if (existing != null && (!existing.matches(request.getFileName(), fileSize, chunkSize))) {
        deleteSessionDir(dir);
        existing = null;
      }
      if (existing == null) {
        try {
          Files.createDirectories(dir);
        } catch (IOException e) {
          throw new IllegalStateException("无法创建分片目录", e);
        }
        existing = new UploadSession(uploadId, request.getFingerprint(), request.getFileName(), fileSize, chunkSize);
        persistSession(existing, dir);
      } else {
        existing.setFileName(request.getFileName());
        existing.setFileSize(fileSize);
        existing.setChunkSize(chunkSize);
        existing.recalculateTotalChunks();
        persistSession(existing, dir);
      }
      return existing;
    }
  }

  private UploadSession loadSession(String uploadId) {
    Path dir = sessionDir(uploadId);
    UploadSession session = loadSessionIfExists(dir);
    if (session == null) {
      throw new IllegalArgumentException("上传会话不存在: " + uploadId);
    }
    return session;
  }

  private UploadSession loadSessionIfExists(Path dir) {
    Path meta = dir.resolve(META_FILE);
    if (!Files.exists(meta)) {
      return null;
    }
    try {
      UploadSession session = objectMapper.readValue(meta.toFile(), UploadSession.class);
      if (session.uploadedChunks == null) {
        session.uploadedChunks = new TreeSet<>();
      }
      return session;
    } catch (IOException e) {
      throw new IllegalStateException("读取分片会话失败", e);
    }
  }

  private void persistSession(UploadSession session, Path dir) {
    Path meta = dir.resolve(META_FILE);
    try {
      objectMapper.writeValue(meta.toFile(), session);
    } catch (IOException e) {
      throw new IllegalStateException("写入分片会话失败", e);
    }
  }

  private Path sessionDir(String uploadId) {
    validateUploadId(uploadId);
    Path dir = chunkRoot.resolve(uploadId).normalize();
    if (!dir.startsWith(chunkRoot)) {
      throw new IllegalArgumentException("非法上传会话");
    }
    return dir;
  }

  private void validateUploadId(String uploadId) {
    if (!StringUtils.hasText(uploadId) || !UPLOAD_ID_PATTERN.matcher(uploadId).matches()) {
      throw new IllegalArgumentException("非法 uploadId");
    }
  }

  private InputStream openCombinedStream(Path dir, int totalChunks) throws IOException {
    List<InputStream> inputs = new ArrayList<>();
    for (int i = 0; i < totalChunks; i++) {
      Path chunkFile = dir.resolve(chunkFileName(i));
      if (!Files.exists(chunkFile)) {
        throw new IllegalStateException("缺失的分片: " + i);
      }
      inputs.add(Files.newInputStream(chunkFile));
    }
    SequenceInputStream combined = new SequenceInputStream(Collections.enumeration(inputs));
    return new InputStream() {
      @Override
      public int read() throws IOException {
        return combined.read();
      }

      @Override
      public int read(byte[] b, int off, int len) throws IOException {
        return combined.read(b, off, len);
      }

      @Override
      public void close() throws IOException {
        combined.close();
        for (InputStream in : inputs) {
          try {
            in.close();
          } catch (IOException ignored) {
          }
        }
      }
    };
  }

  private String chunkFileName(int index) {
    return "chunk-" + index;
  }

  private void validateChunkIndex(UploadSession session, int chunkIndex) {
    if (chunkIndex < 0 || chunkIndex >= session.getTotalChunks()) {
      throw new IllegalArgumentException("分片索引越界: " + chunkIndex);
    }
  }

  private int normalizeChunkSize(Integer requested) {
    if (requested == null) {
      return DEFAULT_CHUNK_SIZE;
    }
    int size = Math.max(MIN_CHUNK_SIZE, Math.min(requested, MAX_CHUNK_SIZE));
    return size;
  }

  private Object lockFor(String uploadId) {
    return sessionLocks.computeIfAbsent(uploadId, (key) -> new Object());
  }

  private boolean deleteSessionDir(Path dir) {
    if (!Files.exists(dir)) {
      return false;
    }
    try (Stream<Path> stream = Files.walk(dir)) {
      stream.sorted(Comparator.reverseOrder())
        .forEach((path) -> {
          try {
            Files.deleteIfExists(path);
          } catch (IOException ignored) {
          }
        });
      return true;
    } catch (IOException e) {
      throw new IllegalStateException("删除分片目录失败", e);
    }
  }

  private String buildUploadId(long userId, String fingerprint) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      digest.update(uploadIdSalt);
      digest.update((byte) ':');
      digest.update(String.valueOf(userId).getBytes(StandardCharsets.UTF_8));
      digest.update((byte) ':');
      digest.update(fingerprint.getBytes(StandardCharsets.UTF_8));
      byte[] hash = digest.digest();
      return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("初始化上传 ID 失败", e);
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @SuppressWarnings("unused")
  private static class UploadSession {
    private String uploadId;
    private String fingerprint;
    private String fileName;
    private long fileSize;
    private int chunkSize;
    private int totalChunks;
    private Set<Integer> uploadedChunks = new TreeSet<>();
    private long updatedAt;

    public UploadSession() {}

    public UploadSession(String uploadId, String fingerprint, String fileName, long fileSize, int chunkSize) {
      this.uploadId = uploadId;
      this.fingerprint = fingerprint;
      this.fileName = fileName;
      this.fileSize = fileSize;
      this.chunkSize = chunkSize;
      recalculateTotalChunks();
      this.updatedAt = Instant.now().toEpochMilli();
    }

    public void recalculateTotalChunks() {
      if (chunkSize <= 0) {
        chunkSize = DEFAULT_CHUNK_SIZE;
      }
      this.totalChunks = (int) Math.max(1, (fileSize + chunkSize - 1) / chunkSize);
    }

    public List<Integer> getUploadedChunkList() {
      return new ArrayList<>(uploadedChunks);
    }

    public String getUploadId() {
      return uploadId;
    }

    public String getFileName() {
      return fileName;
    }

    public long getFileSize() {
      return fileSize;
    }

    public int getChunkSize() {
      return chunkSize;
    }

    public int getTotalChunks() {
      return totalChunks;
    }

    public void setFileName(String fileName) {
      this.fileName = fileName;
    }

    public void setFileSize(long fileSize) {
      this.fileSize = fileSize;
    }

    public void setChunkSize(int chunkSize) {
      this.chunkSize = chunkSize;
    }

    public boolean matches(String fileName, long size, int chunkSize) {
      return Objects.equals(this.fileName, fileName)
        && this.fileSize == size
        && this.chunkSize == chunkSize;
    }

    public int getUploadedCount() {
      return uploadedChunks.size();
    }

    public boolean isComplete() {
      return getUploadedCount() >= totalChunks;
    }

    public void markChunkUploaded(int index) {
      uploadedChunks.add(index);
      updatedAt = Instant.now().toEpochMilli();
    }
  }
}
