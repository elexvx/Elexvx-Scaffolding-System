package com.tencent.tdesign.plugin.examples.warehouse;

import com.tencent.tdesign.print.PrintDataProvider;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class WarehousePrintDataProvider implements PrintDataProvider {
  @Override
  public String definitionId() {
    return "warehouse.inbound.note";
  }

  @Override
  public Map<String, Object> load(String businessRef, Map<String, Object> params) {
    return Map.of(
      "title", "入库单",
      "inboundNo", businessRef,
      "warehouse", params == null ? "默认仓" : params.getOrDefault("warehouse", "默认仓")
    );
  }
}
