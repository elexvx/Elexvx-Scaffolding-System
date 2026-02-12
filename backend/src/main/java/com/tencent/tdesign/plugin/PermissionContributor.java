package com.tencent.tdesign.plugin;

import java.util.List;

public interface PermissionContributor {
  List<String> providePermissions();
}
