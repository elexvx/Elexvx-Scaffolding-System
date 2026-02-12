package com.tencent.tdesign.controller.print;

import com.tencent.tdesign.print.PrintDefinition;
import com.tencent.tdesign.print.PrintJobRequest;
import com.tencent.tdesign.print.PrintService;
import com.tencent.tdesign.vo.ApiResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/print")
public class PrintController {
  private final PrintService printService;

  public PrintController(PrintService printService) {
    this.printService = printService;
  }

  @GetMapping("/definitions")
  public ApiResponse<List<PrintDefinition>> definitions() {
    return ApiResponse.success(printService.listDefinitions());
  }

  @PostMapping("/jobs")
  public ApiResponse<Map<String, String>> create(@RequestBody PrintJobRequest request) {
    return ApiResponse.success(Map.of("jobId", printService.createJob(request)));
  }

  @GetMapping("/jobs/{jobId}/file")
  public ResponseEntity<ByteArrayResource> download(@PathVariable String jobId) {
    byte[] file = printService.load(jobId);
    HttpHeaders headers = new HttpHeaders();
    headers.setCacheControl("no-store");
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDisposition(ContentDisposition.attachment().filename(jobId + ".pdf", StandardCharsets.UTF_8).build());
    return ResponseEntity.ok().headers(headers).body(new ByteArrayResource(file));
  }
}
