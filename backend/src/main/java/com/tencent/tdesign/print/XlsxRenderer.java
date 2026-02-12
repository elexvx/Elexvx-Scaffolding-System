package com.tencent.tdesign.print;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class XlsxRenderer {
  public byte[] render(List<Map<String, Object>> rows) {
    try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      var sheet = wb.createSheet("report");
      for (int i = 0; i < rows.size(); i++) {
        var row = sheet.createRow(i);
        row.createCell(0).setCellValue(String.valueOf(rows.get(i)));
      }
      wb.write(out);
      return out.toByteArray();
    } catch (Exception ex) {
      throw new IllegalStateException("XLSX 渲染失败", ex);
    }
  }
}
