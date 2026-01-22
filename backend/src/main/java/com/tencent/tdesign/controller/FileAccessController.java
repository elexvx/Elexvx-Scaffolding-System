package com.tencent.tdesign.controller;

import com.tencent.tdesign.service.FileTokenService;
import com.tencent.tdesign.service.ObjectStorageService;
import java.io.InputStream;
import java.util.Objects;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/files")
public class FileAccessController {
  private final FileTokenService tokenService;
  private final ObjectStorageService storageService;

  public FileAccessController(FileTokenService tokenService, ObjectStorageService storageService) {
    this.tokenService = tokenService;
    this.storageService = storageService;
  }

  @RequestMapping(value = "/{token}", method = RequestMethod.HEAD)
  public ResponseEntity<Void> head(@PathVariable("token") String token) {
    try {
      FileTokenService.TokenPayload payload = tokenService.decrypt(token);
      ObjectStorageService.FileMeta meta = storageService.stat(payload);
      HttpHeaders headers = new HttpHeaders();
      if (meta.getContentType() != null && !meta.getContentType().isBlank()) {
        headers.setContentType(MediaType.parseMediaType(meta.getContentType()));
      }
      if (meta.getContentLength() >= 0) {
        headers.setContentLength(meta.getContentLength());
      }
      headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
      headers.setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
      return new ResponseEntity<>(headers, HttpStatus.OK);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @GetMapping("/{token}")
  public ResponseEntity<InputStreamResource> preview(
    @PathVariable("token") String token,
    @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader
  ) {
    try {
      FileTokenService.TokenPayload payload = tokenService.decrypt(token);

      if (rangeHeader != null && !rangeHeader.isBlank() && rangeHeader.startsWith("bytes=")) {
        ObjectStorageService.FileMeta meta = storageService.stat(payload);
        long total = meta.getContentLength();
        if (total > 0) {
          ByteRange range = parseRange(rangeHeader, total);
          if (range == null) {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
            headers.set(HttpHeaders.CONTENT_RANGE, "bytes */" + total);
            return new ResponseEntity<>(null, headers, HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
          }
          ObjectStorageService.FileStream stream = storageService.openStream(payload, range.start(), range.end());
          HttpHeaders headers = new HttpHeaders();
          String contentType = stream.getContentType();
          if (contentType != null && !contentType.isBlank()) {
            headers.setContentType(MediaType.parseMediaType(contentType));
          }
          headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
          headers.set(HttpHeaders.CONTENT_RANGE, "bytes " + range.start() + "-" + range.end() + "/" + total);
          headers.setContentLength(stream.getContentLength());
          headers.setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
          InputStream inputStream = stream.getInputStream();
          return new ResponseEntity<>(
            new InputStreamResource(Objects.requireNonNull(inputStream)),
            headers,
            HttpStatus.PARTIAL_CONTENT
          );
        }
      }

      ObjectStorageService.FileStream stream = storageService.openStream(payload);
      HttpHeaders headers = new HttpHeaders();
      String contentType = stream.getContentType();
      if (contentType != null && !contentType.isBlank()) {
        headers.setContentType(MediaType.parseMediaType(contentType));
      }
      if (stream.getContentLength() >= 0) {
        headers.setContentLength(stream.getContentLength());
      }
      headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
      headers.setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
      InputStream inputStream = stream.getInputStream();
      return new ResponseEntity<>(
        new InputStreamResource(Objects.requireNonNull(inputStream)),
        headers,
        HttpStatus.OK
      );
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  private record ByteRange(long start, long end) {
  }

  private ByteRange parseRange(String header, long total) {
    String raw = header.trim();
    if (!raw.startsWith("bytes=")) return null;
    String spec = raw.substring("bytes=".length());
    int comma = spec.indexOf(',');
    if (comma >= 0) spec = spec.substring(0, comma);
    spec = spec.trim();
    int dash = spec.indexOf('-');
    if (dash < 0) return null;
    String left = spec.substring(0, dash).trim();
    String right = spec.substring(dash + 1).trim();

    try {
      if (left.isEmpty()) {
        if (right.isEmpty()) return null;
        long suffix = Long.parseLong(right);
        if (suffix <= 0) return null;
        if (suffix >= total) return new ByteRange(0, total - 1);
        return new ByteRange(total - suffix, total - 1);
      }

      long start = Long.parseLong(left);
      long end = right.isEmpty() ? (total - 1) : Long.parseLong(right);
      if (start < 0) return null;
      if (end < start) return null;
      if (start >= total) return null;
      if (end >= total) end = total - 1;
      return new ByteRange(start, end);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
