package com.tencent.tdesign.print;

import java.io.ByteArrayOutputStream;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.springframework.stereotype.Component;

@Component
public class XslFoRenderer {
  public byte[] render(Source foTemplate) {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      FopFactory fopFactory = FopFactory.newDefaultInstance();
      FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
      Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      Result result = new StreamResult(fop.getDefaultHandler());
      transformer.transform(foTemplate, result);
      return out.toByteArray();
    } catch (Exception ex) {
      throw new IllegalStateException("FOP 渲染失败", ex);
    }
  }

  public Source inlineFo(String fo) {
    return new StreamSource(new java.io.StringReader(fo));
  }
}
