<template>
  <t-form :data="form" label-width="120px" layout="vertical" label-align="right" @submit="onSubmit">
    <t-row :gutter="[24, 16]">
      <t-col :xs="24" :sm="12">
        <t-form-item label="版本号" name="appVersion">
          <t-input
            v-model="form.appVersion"
            placeholder="如：v1.2.3 或 2025.12"
            style="max-width: 500px; width: 100%"
          />
        </t-form-item>
      </t-col>
      <t-col :span="24">
        <t-form-item>
          <t-space :size="12">
            <t-button theme="primary" :disabled="!isAdmin" @click="onSubmit">保存</t-button>
          </t-space>
        </t-form-item>
      </t-col>
    </t-row>
  </t-form>

  <t-divider />

  <t-space direction="vertical" size="small">
    <t-space align="center" size="small">
      <span class="version-label">系统版本：</span>
      <span>{{ updateInfo.currentVersion || '-' }}</span>
      <t-button variant="outline" size="small" :loading="checking" @click="checkUpdate">检查更新</t-button>
    </t-space>
    <t-space align="center" size="small">
      <span class="version-label">最新版本：</span>
      <span>{{ updateInfo.latestVersion || '-' }}</span>
      <span v-if="updateInfo.updateAvailable" class="version-tag">可更新</span>
      <span v-else class="version-tag version-tag--muted">已是最新</span>
      <t-link v-if="updateInfo.updateUrl" :href="updateInfo.updateUrl" target="_blank">更新说明</t-link>
    </t-space>
    <div v-if="updateInfo.message" class="version-message">{{ updateInfo.message }}</div>
  </t-space>
</template>
<script setup lang="ts">
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted, reactive, ref } from 'vue';

import { useSettingStore, useUserStore } from '@/store';
import { request } from '@/utils/request';

const form = reactive({ appVersion: '' });
const updateInfo = reactive({
  currentVersion: '',
  latestVersion: '',
  updateAvailable: false,
  updateUrl: '',
  message: '',
});
const checking = ref(false);

const load = async () => {
  const s = await request.get<any>({ url: '/system/ui' });
  form.appVersion = s.appVersion || '';
};

const settingStore = useSettingStore();
const userStore = useUserStore();
const isAdmin = computed(() => (userStore.userInfo?.roles || []).includes('admin'));

const onSubmit = async () => {
  if (!isAdmin.value) {
    MessagePlugin.warning('Only admins can update system settings.');
    return;
  }
  await request.post({ url: '/system/ui', data: { appVersion: form.appVersion } });
  await settingStore.loadUiSetting();
  MessagePlugin.success('保存成功');
};

const checkUpdate = async () => {
  checking.value = true;
  try {
    const res = await request.get<any>({ url: '/system/update/check' });
    updateInfo.currentVersion = res.currentVersion || '';
    updateInfo.latestVersion = res.latestVersion || '';
    updateInfo.updateAvailable = !!res.updateAvailable;
    updateInfo.updateUrl = res.updateUrl || '';
    updateInfo.message = res.message || '';
  } catch (error) {
    console.error(error);
    MessagePlugin.error('更新检测失败');
  } finally {
    checking.value = false;
  }
};

onMounted(() => {
  load();
  checkUpdate();
});
</script>
<style scoped>
.version-label {
  color: var(--td-text-color-secondary);
}

.version-tag {
  padding: 0 6px;
  border-radius: 10px;
  background: var(--td-success-color-1);
  color: var(--td-success-color);
  font-size: 12px;
}

.version-tag--muted {
  background: var(--td-bg-color-container);
  color: var(--td-text-color-secondary);
}

.version-message {
  color: var(--td-text-color-secondary);
  font-size: 12px;
}
</style>
