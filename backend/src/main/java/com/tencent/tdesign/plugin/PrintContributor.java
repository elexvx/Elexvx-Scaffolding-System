package com.tencent.tdesign.plugin;

import com.tencent.tdesign.print.PrintDefinition;
import java.util.List;

public interface PrintContributor {
  List<PrintDefinition> provideDefinitions();
}
