package com.tencent.tdesign.print;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class PdfBoxLayoutRendererTest {
  @Test
  void shouldRenderPdf() {
    PdfBoxLayoutRenderer renderer = new PdfBoxLayoutRenderer();
    byte[] bytes = renderer.render(Map.of("title", "Inbound"));
    assertTrue(bytes.length > 100);
  }
}
