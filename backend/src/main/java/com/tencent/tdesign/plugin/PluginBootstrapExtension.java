package com.tencent.tdesign.plugin;

public interface PluginBootstrapExtension {
  default void onStart(String pluginId) {}
  default void onStop(String pluginId) {}
}
