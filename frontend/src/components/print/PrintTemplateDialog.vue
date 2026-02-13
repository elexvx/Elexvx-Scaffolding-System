<template>
  <t-dialog v-model:visible="visible" header="打印" width="980px" :footer="false">
    <div style="margin-bottom:8px;display:flex;gap:8px">
      <t-button size="small" :theme="viewMode === 'design' ? 'primary' : 'default'" @click="viewMode = 'design'">模板打印</t-button>
      <t-button size="small" :theme="viewMode === 'jobs' ? 'primary' : 'default'" @click="loadJobs">历史记录</t-button>
    </div>

    <div v-if="viewMode === 'design'" style="display:flex;gap:12px">
      <div style="width:35%">
        <t-select v-model="selected" :options="options" placeholder="选择模板" @change="onTemplateChange" />
        <t-select v-model="selectedVersion" :options="versionOptions" placeholder="选择版本（默认已发布）" style="margin-top:8px" />
        <div style="margin-top:8px;color:#666;font-size:12px">{{ templateDesc }}</div>
        <t-button block theme="primary" style="margin-top:8px" @click="doPreview" :disabled="!selected">预览</t-button>
        <t-button block style="margin-top:8px" @click="doPrint" :disabled="!previewHtml">直接打印</t-button>
        <t-button block style="margin-top:8px" @click="doPdf" :disabled="!selected">生成PDF留档</t-button>
      </div>
      <div style="width:65%">
        <iframe v-if="previewHtml" :srcdoc="previewHtml" style="width:100%;height:420px;border:1px solid #ddd" />
      </div>
    </div>

    <div v-else>
      <t-table :data="jobs" :columns="jobColumns" row-key="id" size="small" />
      <div style="margin-top:8px;display:flex;justify-content:flex-end">
        <t-pagination v-model:current="jobPage" v-model:page-size="jobPageSize" :total="jobTotal" @change="loadJobs" />
      </div>
    </div>
  </t-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { fetchPrintJobs, fetchPrintTemplates, fetchTemplateVersions, renderPrintHtml, renderPrintPdf } from '@/api/print';

const props = defineProps<{ modelValue: boolean; bizType: string; bizId?: string; data: Record<string, unknown> }>();
const emit = defineEmits<{ (e: 'update:modelValue', v: boolean): void; (e: 'pdf-success', v: { fileUrl: string; jobId: number }): void }>();

const visible = computed({ get: () => props.modelValue, set: (v) => emit('update:modelValue', v) });
const viewMode = ref<'design' | 'jobs'>('design');
const templates = ref<any[]>([]);
const selected = ref<number>();
const selectedVersion = ref<number>();
const versions = ref<Array<{ version: number; published: number }>>([]);
const previewHtml = ref('');
const jobs = ref<any[]>([]);
const jobPage = ref(1);
const jobPageSize = ref(20);
const jobTotal = ref(0);

const options = computed(() => templates.value.map((t) => ({ label: t.name, value: t.id })));
const versionOptions = computed(() => versions.value.map((v) => ({ label: `v${v.version}${v.published ? '（已发布）' : ''}`, value: v.version })));
const templateDesc = computed(() => {
  const item = templates.value.find((t) => t.id === selected.value);
  if (!item) return '';
  return `业务类型: ${item.biz_type}，当前版本: v${item.current_version}`;
});

const jobColumns = [
  { colKey: 'id', title: '任务ID', width: 90 },
  { colKey: 'template_id', title: '模板ID', width: 90 },
  { colKey: 'status', title: '状态', width: 90 },
  { colKey: 'created_at', title: '创建时间' },
];

watch(visible, async (v) => {
  if (!v) return;
  viewMode.value = 'design';
  templates.value = await fetchPrintTemplates({ bizType: props.bizType, enabled: 1 });
});

const onTemplateChange = async () => {
  if (!selected.value) return;
  versions.value = await fetchTemplateVersions(Number(selected.value));
  selectedVersion.value = versions.value.find((v) => Number(v.published) === 1)?.version;
};

const doPreview = async () => {
  const r = await renderPrintHtml({ templateId: Number(selected.value), version: selectedVersion.value, data: props.data });
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
  const r = await renderPrintPdf({ templateId: Number(selected.value), version: selectedVersion.value, data: props.data, bizType: props.bizType, bizId: props.bizId });
  emit('pdf-success', r);
};

const loadJobs = async () => {
  viewMode.value = 'jobs';
  const resp = await fetchPrintJobs({
    page: jobPage.value,
    pageSize: jobPageSize.value,
    bizType: props.bizType,
    bizId: props.bizId,
  });
  jobs.value = resp.items;
  jobTotal.value = resp.total;
};
</script>
