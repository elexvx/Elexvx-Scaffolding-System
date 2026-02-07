<template>
  <div class="module-management">
    <t-card title="模块管理" :bordered="false">
      <t-space direction="vertical" style="width: 100%" size="large">
        <t-space>
          <t-button variant="outline" :loading="loading" @click="loadModules">刷新</t-button>
        </t-space>

        <t-table row-key="moduleKey" :data="modules" :columns="columns" :loading="loading">
          <template #installState="{ row }">
            <t-tag :theme="stateTheme(row.installState)" variant="light">
              {{ stateLabel(row.installState) }}
            </t-tag>
          </template>
          <template #installedAt="{ row }">
            <span>{{ formatDate(row.installedAt) }}</span>
          </template>
          <template #op="{ row }">
            <t-space>
              <t-button
                size="small"
                theme="primary"
                variant="outline"
                :loading="isActionLoading(row.moduleKey, 'install')"
                :disabled="isInstalled(row.installState)"
                @click="install(row)"
              >
                安装
              </t-button>
              <t-button
                size="small"
                theme="danger"
                variant="outline"
                :loading="isActionLoading(row.moduleKey, 'uninstall')"
                :disabled="!isInstalled(row.installState)"
                @click="uninstall(row)"
              >
                卸载
              </t-button>
            </t-space>
          </template>
        </t-table>
      </t-space>
    </t-card>
  </div>
</template>
<script setup lang="ts">
import dayjs from 'dayjs';
import type { PrimaryTableCol } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { onMounted, ref } from 'vue';

import type { ModuleRegistryItem } from '@/api/system/module';
import { fetchModules, installModule, uninstallModule } from '@/api/system/module';

const loading = ref(false);
const modules = ref<ModuleRegistryItem[]>([]);
const actionLoading = ref<Record<string, boolean>>({});

const columns: PrimaryTableCol[] = [
  {
    colKey: 'serial-number',
    title: '序号',
    width: 80,
    cell: (_h, { rowIndex }) => String(rowIndex + 1),
  },
  { colKey: 'name', title: '模块名称', minWidth: 160 },
  { colKey: 'moduleKey', title: '模块标识', width: 160 },
  { colKey: 'version', title: '版本', width: 110 },
  { colKey: 'installState', title: '安装状态', width: 120 },
  { colKey: 'installedAt', title: '安装时间', width: 180 },
  { colKey: 'op', title: '操作', width: 200, fixed: 'right' },
];

const normalizeInstallState = (state?: string) =>
  String(state || '')
    .trim()
    .toUpperCase();
const isInstalled = (state?: string) => normalizeInstallState(state) === 'INSTALLED';

const stateLabel = (state?: string) => {
  const normalized = normalizeInstallState(state);
  if (normalized === 'INSTALLED') return '已安装';
  if (normalized === 'FAILED') return '安装失败';
  if (normalized === 'UNINSTALLED') return '已卸载';
  return '待安装';
};

const stateTheme = (state?: string) => {
  const normalized = normalizeInstallState(state);
  if (normalized === 'INSTALLED') return 'success';
  if (normalized === 'FAILED') return 'danger';
  if (normalized === 'UNINSTALLED') return 'default';
  return 'warning';
};

const formatDate = (value?: string) => {
  if (!value) return '-';
  return dayjs(value).format('YYYY-MM-DD HH:mm');
};

const setActionLoading = (key: string, action: string, value: boolean) => {
  actionLoading.value[`${key}-${action}`] = value;
};

const isActionLoading = (key: string, action: string) => !!actionLoading.value[`${key}-${action}`];

const loadModules = async () => {
  loading.value = true;
  try {
    const data = await fetchModules();
    modules.value = data;
  } catch (error: any) {
    MessagePlugin.error(error?.message || '模块加载失败');
  } finally {
    loading.value = false;
  }
};

const install = async (row: ModuleRegistryItem) => {
  setActionLoading(row.moduleKey, 'install', true);
  try {
    await installModule(row.moduleKey);
    MessagePlugin.success('模块安装完成');
  } catch (error: any) {
    MessagePlugin.error(error?.message || '模块安装失败');
  } finally {
    setActionLoading(row.moduleKey, 'install', false);
    await loadModules();
  }
};

const uninstall = async (row: ModuleRegistryItem) => {
  setActionLoading(row.moduleKey, 'uninstall', true);
  try {
    await uninstallModule(row.moduleKey);
    MessagePlugin.success('模块已卸载');
  } catch (error: any) {
    MessagePlugin.error(error?.message || '模块卸载失败');
  } finally {
    setActionLoading(row.moduleKey, 'uninstall', false);
    await loadModules();
  }
};

onMounted(() => {
  loadModules();
});
</script>
<style lang="less" scoped>
.module-management {
  :deep(.t-card__body) {
    padding: 16px 24px 24px;
  }
}
</style>
