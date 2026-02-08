package com.tencent.tdesign.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tdesign.module.ExternalScriptModuleDefinition;
import com.tencent.tdesign.module.ModuleDefinition;
import com.tencent.tdesign.module.ModulePackageManifest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ModulePackageService {
  private static final List<String> SUPPORTED_DATABASES = List.of("mysql", "postgresql", "oracle", "sqlserver");
  private final ObjectMapper objectMapper;
  private final ResourceLoader resourceLoader;
  private final Path externalRoot;
  private final Path manifestDir;

  public ModulePackageService(ObjectMapper objectMapper, ResourceLoader resourceLoader, Environment environment) {
    this.objectMapper = objectMapper;
    this.resourceLoader = resourceLoader;
    String dir = environment.getProperty("tdesign.modules.packageDir", "data/module-packages");
    this.externalRoot = Paths.get(dir).toAbsolutePath().normalize();
    this.manifestDir = externalRoot.resolve("manifests");
    ensureDirs();
  }

  public Path getExternalRoot() {
    return externalRoot;
  }

  public List<ModuleDefinition> loadExternalDefinitions() {
    List<ModuleDefinition> defs = new ArrayList<>();
    if (!Files.isDirectory(manifestDir)) return defs;
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(manifestDir, "*.json")) {
      for (Path file : stream) {
        ModulePackageManifest manifest = readManifest(file);
        if (manifest == null || manifest.getKey() == null || manifest.getKey().isBlank()) continue;
        defs.add(new ExternalScriptModuleDefinition(manifest));
      }
    } catch (IOException ignored) {
      return defs;
    }
    return defs;
  }

  public byte[] buildPackage(ModuleDefinition definition) {
    if (definition == null || definition.getKey() == null || definition.getKey().isBlank()) {
      throw new IllegalArgumentException("模块不存在");
    }
    String key = definition.getKey().trim().toLowerCase(Locale.ROOT);
    ModulePackageManifest manifest = new ModulePackageManifest();
    manifest.setKey(key);
    manifest.setName(definition.getName());
    manifest.setVersion(definition.getVersion());
    manifest.setEnabledByDefault(definition.isEnabledByDefault());
    manifest.setRequiredTables(definition.getRequiredTables());

    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ZipOutputStream zos = new ZipOutputStream(bos, StandardCharsets.UTF_8)) {
      byte[] manifestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(manifest);
      writeZipEntry(zos, "module.json", manifestBytes);

      for (String db : SUPPORTED_DATABASES) {
        writeSqlIfExists(zos, key, db, "install");
        writeSqlIfExists(zos, key, db, "uninstall");
      }
      zos.finish();
      return bos.toByteArray();
    } catch (IOException e) {
      throw new IllegalArgumentException("生成模块包失败: " + e.getMessage());
    }
  }

  public ModulePackageManifest saveAndExtract(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("模块包不能为空");
    }
    ModulePackageManifest manifest = null;
    List<ZipEntryData> sqlEntries = new ArrayList<>();

    try (ZipInputStream zis = new ZipInputStream(file.getInputStream(), StandardCharsets.UTF_8)) {
      java.util.zip.ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        if (entry.isDirectory()) continue;
        String name = normalizeZipEntryName(entry.getName());
        if (name == null) continue;
        byte[] bytes = zis.readAllBytes();
        if ("module.json".equalsIgnoreCase(name)) {
          manifest = objectMapper.readValue(bytes, ModulePackageManifest.class);
          continue;
        }
        if (name.toLowerCase(Locale.ROOT).startsWith("modules/") && name.toLowerCase(Locale.ROOT).endsWith(".sql")) {
          sqlEntries.add(new ZipEntryData(name, bytes));
        }
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("解析模块包失败: " + e.getMessage());
    }

    if (manifest == null || manifest.getKey() == null || manifest.getKey().isBlank()) {
      throw new IllegalArgumentException("模块包缺少 module.json 或 key");
    }
    String key = manifest.getKey().trim().toLowerCase(Locale.ROOT);
    manifest.setKey(key);

    Path manifestFile = manifestDir.resolve(key + ".json").toAbsolutePath().normalize();
    if (!manifestFile.startsWith(manifestDir)) {
      throw new IllegalArgumentException("模块 key 非法");
    }

    try {
      Files.createDirectories(manifestDir);
      Files.writeString(manifestFile, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(manifest), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalArgumentException("保存模块清单失败: " + e.getMessage());
    }

    int written = 0;
    for (ZipEntryData data : sqlEntries) {
      String normalized = data.name.toLowerCase(Locale.ROOT);
      String prefix = "modules/" + key + "/";
      if (!normalized.startsWith(prefix)) continue;
      String[] parts = normalized.split("/");
      if (parts.length != 4) continue;
      String db = parts[2];
      if (!SUPPORTED_DATABASES.contains(db)) continue;
      String filename = parts[3];
      if (!"install.sql".equals(filename) && !"uninstall.sql".equals(filename)) continue;
      Path target = externalRoot
        .resolve("modules")
        .resolve(key)
        .resolve(db)
        .resolve(filename)
        .toAbsolutePath()
        .normalize();
      try {
        Files.createDirectories(target.getParent());
        Files.write(target, data.bytes);
        written++;
      } catch (IOException e) {
        throw new IllegalArgumentException("保存模块脚本失败: " + e.getMessage());
      }
    }
    if (written == 0) {
      throw new IllegalArgumentException("模块包不包含可识别的 SQL 脚本");
    }
    return manifest;
  }

  private void writeSqlIfExists(ZipOutputStream zos, String key, String db, String action) throws IOException {
    String zipPath = String.format("modules/%s/%s/%s.sql", key, db, action);
    byte[] bytes = readSqlBytes(key, db, action);
    if (bytes == null || bytes.length == 0) return;
    writeZipEntry(zos, zipPath, bytes);
  }

  private byte[] readSqlBytes(String key, String db, String action) throws IOException {
    Path file = externalRoot.resolve("modules").resolve(key).resolve(db).resolve(action + ".sql");
    if (Files.exists(file)) {
      return Files.readAllBytes(file);
    }
    Resource resource = resourceLoader.getResource(String.format("classpath:modules/%s/%s/%s.sql", key, db, action));
    if (!resource.exists()) return null;
    try (InputStream in = resource.getInputStream()) {
      return in.readAllBytes();
    }
  }

  private void writeZipEntry(ZipOutputStream zos, String name, byte[] bytes) throws IOException {
    ZipEntry entry = new ZipEntry(name);
    zos.putNextEntry(entry);
    zos.write(bytes);
    zos.closeEntry();
  }

  private String normalizeZipEntryName(String name) {
    if (name == null) return null;
    String n = name.replace('\\', '/').trim();
    while (n.startsWith("/")) n = n.substring(1);
    if (n.contains("..")) return null;
    return n;
  }

  private ModulePackageManifest readManifest(Path file) {
    try {
      byte[] bytes = Files.readAllBytes(file);
      ModulePackageManifest manifest = objectMapper.readValue(bytes, ModulePackageManifest.class);
      if (manifest == null) return null;
      if (manifest.getKey() != null) manifest.setKey(manifest.getKey().trim().toLowerCase(Locale.ROOT));
      return manifest;
    } catch (Exception ignored) {
      return null;
    }
  }

  private void ensureDirs() {
    try {
      Files.createDirectories(externalRoot.resolve("modules"));
      Files.createDirectories(manifestDir);
    } catch (IOException ignored) {}
  }

  private record ZipEntryData(String name, byte[] bytes) {}
}
