package com.tencent.tdesign.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.region.Region;
import com.tencent.tdesign.entity.StorageSetting;
import jakarta.annotation.PreDestroy;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

@Component
public class ObjectStorageClientManager {
  private final AtomicReference<AliyunHolder> aliyunRef = new AtomicReference<>();
  private final AtomicReference<TencentHolder> tencentRef = new AtomicReference<>();

  public OSS getAliyunClient(StorageSetting setting, String endpoint) {
    String fingerprint = fingerprint(setting, endpoint);
    AliyunHolder existing = aliyunRef.get();
    if (existing != null && Objects.equals(existing.fingerprint, fingerprint)) {
      return existing.client;
    }
    OSS created = new OSSClientBuilder().build(endpoint, setting.getAccessKey(), setting.getSecretKey());
    AliyunHolder updated = new AliyunHolder(fingerprint, created);
    AliyunHolder previous = aliyunRef.getAndSet(updated);
    if (previous != null) {
      try { previous.client.shutdown(); } catch (Exception ignore) {}
    }
    return created;
  }

  public COSClient getTencentClient(StorageSetting setting) {
    String fingerprint = fingerprint(setting, setting.getRegion());
    TencentHolder existing = tencentRef.get();
    if (existing != null && Objects.equals(existing.fingerprint, fingerprint)) {
      return existing.client;
    }
    ClientConfig config = new ClientConfig(new Region(setting.getRegion()));
    config.setHttpProtocol(HttpProtocol.https);
    COSClient created = new COSClient(new com.qcloud.cos.auth.BasicCOSCredentials(setting.getAccessKey(), setting.getSecretKey()), config);
    TencentHolder updated = new TencentHolder(fingerprint, created);
    TencentHolder previous = tencentRef.getAndSet(updated);
    if (previous != null) {
      try { previous.client.shutdown(); } catch (Exception ignore) {}
    }
    return created;
  }

  public void invalidateAll() {
    AliyunHolder aliyun = aliyunRef.getAndSet(null);
    if (aliyun != null) {
      try { aliyun.client.shutdown(); } catch (Exception ignore) {}
    }
    TencentHolder tencent = tencentRef.getAndSet(null);
    if (tencent != null) {
      try { tencent.client.shutdown(); } catch (Exception ignore) {}
    }
  }

  @PreDestroy
  public void destroy() {
    invalidateAll();
  }

  private String fingerprint(StorageSetting setting, String ext) {
    return String.join("|",
      String.valueOf(setting.getProvider()),
      String.valueOf(setting.getAccessKey()),
      String.valueOf(setting.getSecretKey()),
      String.valueOf(setting.getBucket()),
      String.valueOf(ext));
  }

  private record AliyunHolder(String fingerprint, OSS client) {}
  private record TencentHolder(String fingerprint, COSClient client) {}
}
