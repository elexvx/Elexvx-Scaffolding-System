<template>
  <t-dialog v-model:visible="visible" header="打印" width="900px" :footer="false">
    <div style="display:flex;gap:12px">
      <div style="width:35%">
        <t-select v-model="selected" :options="options" placeholder="选择模板" />
        <t-button block theme="primary" style="margin-top:8px" @click="doPreview" :disabled="!selected">预览</t-button>
        <t-button block style="margin-top:8px" @click="doPrint" :disabled="!previewHtml">直接打印</t-button>
        <t-button block style="margin-top:8px" @click="doPdf" :disabled="!selected">生成PDF留档</t-button>
      </div>
      <div style="width:65%">
        <iframe v-if="previewHtml" :srcdoc="previewHtml" style="width:100%;height:360px;border:1px solid #ddd" />
      </div>
    </div>
  </t-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { fetchPrintTemplates, renderPrintHtml, renderPrintPdf } from '@/api/print';

const props = defineProps<{ modelValue: boolean; bizType: string; bizId?: string; data: Record<string, unknown> }>();
const emit = defineEmits<{ (e: 'update:modelValue', v: boolean): void; (e: 'pdf-success', v: { fileUrl: string; jobId: number }): void }>();

const visible = computed({ get: () => props.modelValue, set: (v) => emit('update:modelValue', v) });
const templates = ref<any[]>([]);
const selected = ref<number>();
const previewHtml = ref('');
const options = computed(() => templates.value.map((t) => ({ label: t.name, value: t.id })));

watch(visible, async (v) => {
  if (!v) return;
  templates.value = await fetchPrintTemplates({ bizType: props.bizType, enabled: 1 });
});

const doPreview = async () => {
  const r = await renderPrintHtml({ templateId: Number(selected.value), data: props.data });
  previewHtml.value = r.computedHtml;
};

const doPrint = () => {
  const win = window.open('', '_blank');
  if (!win) return;
  win.document.write(previewHtml.value);
  win.document.close();
  win.focus();
  win.print();
};

const doPdf = async () => {
  const r = await renderPrintPdf({ templateId: Number(selected.value), data: props.data, bizType: props.bizType, bizId: props.bizId });
  emit('pdf-success', r);
};
</script>
