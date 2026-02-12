package com.tencent.tdesign.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;

@Service
@ConditionalOnProperty(prefix = "tdesign.plugins", name = "enabled", havingValue = "true")
public class PluginPackageService {
  private final Path packageRoot;
  private final ObjectMapper objectMapper;

  public PluginPackageService(@Value("${tdesign.plugins.package-dir:data/plugins}") String packageDir, ObjectMapper objectMapper) {
    this.packageRoot = Paths.get(packageDir).toAbsolutePath().normalize();
    this.objectMapper = objectMapper;
  }

  public PluginInstallArtifact stage(MultipartFile file) {
    if (file == null || file.isEmpty()) throw new IllegalArgumentException("插件包不能为空");
    String traceId = UUID.randomUUID().toString();
    Path stageDir = packageRoot.resolve(".staging").resolve(traceId).toAbsolutePath().normalize();
    try {
      Files.createDirectories(stageDir);
      Path packageFile = stageDir.resolve("plugin.elexvx");
      file.transferTo(packageFile);
      String hash = sha256(packageFile);
      PluginManifest manifest = extractManifest(packageFile);
      return new PluginInstallArtifact(traceId, packageFile, manifest, hash);
    } catch (Exception ex) {
      throw new IllegalStateException("插件包暂存失败: " + ex.getMessage(), ex);
    }
  }

  private PluginManifest extractManifest(Path packageFile) throws IOException {
    try (InputStream in = Files.newInputStream(packageFile); ZipInputStream zis = new ZipInputStream(in)) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        if (entry.isDirectory()) continue;
        String name = entry.getName().replace('\\', '/').toLowerCase(Locale.ROOT);
        if ("manifest.yml".equals(name)) {
          Yaml yaml = new Yaml();
          Object parsed = yaml.load(zis);
          return objectMapper.convertValue(parsed, PluginManifest.class);
        }
      }
    }
    throw new IllegalArgumentException("插件包缺少 manifest.yml");
  }

  public static String sha256(Path file) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      digest.update(Files.readAllBytes(file));
      return HexFormat.of().formatHex(digest.digest());
    } catch (Exception ex) {
      throw new IllegalStateException("计算插件包 hash 失败", ex);
    }
  }

  public record PluginInstallArtifact(String traceId, Path packageFile, PluginManifest manifest, String hash) {}
}
