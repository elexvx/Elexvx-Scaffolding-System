package com.tencent.tdesign.print;

import java.util.Map;

public interface PrintDataProvider {
  String definitionId();
  Map<String, Object> load(String businessRef, Map<String, Object> params);
}
