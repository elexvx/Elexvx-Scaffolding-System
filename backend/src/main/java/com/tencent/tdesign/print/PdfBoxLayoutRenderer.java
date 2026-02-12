package com.tencent.tdesign.print;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Component;

@Component
public class PdfBoxLayoutRenderer {
  public byte[] render(Map<String, Object> data) {
    try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      PDPage page = new PDPage();
      document.addPage(page);
      try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 12);
        cs.newLineAtOffset(50, 750);
        cs.showText("Print Preview - " + data.getOrDefault("title", "Untitled"));
        cs.endText();
      }
      document.save(out);
      return out.toByteArray();
    } catch (IOException ex) {
      throw new IllegalStateException("PDFBox 渲染失败", ex);
    }
  }
}
