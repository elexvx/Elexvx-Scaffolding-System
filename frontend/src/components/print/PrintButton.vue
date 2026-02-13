<template>
  <div>
    <t-button :disabled="disabled" @click="open">打印</t-button>
    <PrintTemplateDialog v-model="visible" :biz-type="bizType" :biz-id="bizId" :data="resolvedData" @pdf-success="onPdfSuccess" />
  </div>
</template>

<script setup lang="ts">
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, ref } from 'vue';
import { fetchModules } from '@/api/system/module';
import PrintTemplateDialog from './PrintTemplateDialog.vue';

const props = defineProps<{ bizType: string; bizId?: string; data: Record<string, unknown> | (() => Promise<Record<string, unknown>>); defaultTemplateId?: number; mode?: 'print' | 'pdf' | 'both' }>();

const visible = ref(false);
const moduleEnabled = ref(false);
const resolvedData = ref<Record<string, unknown>>({});

const disabled = computed(() => !moduleEnabled.value);

const open = async () => {
  if (!moduleEnabled.value) {
    MessagePlugin.warning('请先在【系统管理-模块管理】安装并启用打印中心模块');
    return;
  }
  resolvedData.value = typeof props.data === 'function' ? await props.data() : props.data;
  visible.value = true;
};

fetchModules().then((modules) => {
  const print = modules.find((m) => m.moduleKey === 'print');
  moduleEnabled.value = Boolean(print?.enabled);
});

const onPdfSuccess = (v: { fileUrl: string }) => {
  MessagePlugin.success('PDF 已生成');
  if (v.fileUrl) window.open(v.fileUrl, '_blank');
};
</script>
