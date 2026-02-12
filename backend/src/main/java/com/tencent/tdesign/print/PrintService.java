package com.tencent.tdesign.print;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class PrintService {
  private final PdfBoxLayoutRenderer pdfRenderer;
  private final XslFoRenderer foRenderer;
  private final XlsxRenderer xlsxRenderer;
  private final Map<String, byte[]> fileStore = new ConcurrentHashMap<>();

  public PrintService(PdfBoxLayoutRenderer pdfRenderer, XslFoRenderer foRenderer, XlsxRenderer xlsxRenderer) {
    this.pdfRenderer = pdfRenderer;
    this.foRenderer = foRenderer;
    this.xlsxRenderer = xlsxRenderer;
  }

  public List<PrintDefinition> listDefinitions() {
    PrintDefinition warehouse = new PrintDefinition();
    warehouse.setDefinitionId("warehouse.inbound.note");
    warehouse.setName("仓储-入库单");
    warehouse.setTemplateType(TemplateType.PDFBOX_LAYOUT);
    warehouse.setPermission("plugin:warehouse:print");
    return List.of(warehouse);
  }

  public String createJob(PrintJobRequest request) {
    String jobId = UUID.randomUUID().toString();
    byte[] payload;
    if ("report.fo".equals(request.getDefinitionId())) {
      payload = foRenderer.render(foRenderer.inlineFo("<fo:root xmlns:fo='http://www.w3.org/1999/XSL/Format'><fo:layout-master-set><fo:simple-page-master master-name='A4' page-width='210mm' page-height='297mm' margin='10mm'><fo:region-body/></fo:simple-page-master></fo:layout-master-set><fo:page-sequence master-reference='A4'><fo:flow flow-name='xsl-region-body'><fo:block>Report</fo:block></fo:flow></fo:page-sequence></fo:root>"));
    } else if ("report.xlsx".equals(request.getDefinitionId())) {
      payload = xlsxRenderer.render(List.of(Map.of("businessRef", request.getBusinessRef())));
    } else {
      payload = pdfRenderer.render(Map.of("title", request.getDefinitionId(), "businessRef", request.getBusinessRef()));
    }
    fileStore.put(jobId, payload);
    return jobId;
  }

  public byte[] load(String jobId) {
    byte[] content = fileStore.get(jobId);
    if (content == null) throw new IllegalArgumentException("打印任务不存在");
    return content;
  }
}
