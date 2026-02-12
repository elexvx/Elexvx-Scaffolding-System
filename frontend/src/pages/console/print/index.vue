<template>
  <div class="print-center">
    <t-card title="打印中心" :bordered="false">
      <t-space direction="vertical" style="width: 100%">
        <t-alert theme="info" message="选择模板后生成打印任务，并自动下载 PDF 文件。" />

        <t-form :data="form" :rules="rules" layout="vertical" @submit="onSubmit">
          <t-row :gutter="[24, 24]">
            <t-col :xs="24" :sm="12">
              <t-form-item label="打印模板" name="definitionId">
                <t-select v-model="form.definitionId" :loading="loadingDefs" placeholder="请选择打印模板">
                  <t-option
                    v-for="item in definitions"
                    :key="item.definitionId"
                    :label="item.name"
                    :value="item.definitionId"
                  />
                </t-select>
              </t-form-item>
            </t-col>
            <t-col :xs="24" :sm="12">
              <t-form-item label="业务单号" name="businessRef">
                <t-input v-model="form.businessRef" placeholder="例如 INBOUND-20260212-001" />
              </t-form-item>
            </t-col>
            <t-col :xs="24" :sm="12">
              <t-form-item label="模式" name="mode">
                <t-select v-model="form.mode">
                  <t-option label="预览" value="preview" />
                  <t-option label="打印" value="print" />
                </t-select>
              </t-form-item>
            </t-col>
            <t-col :xs="24" :sm="12">
              <t-form-item label="扩展参数（JSON）" name="paramsText">
                <t-textarea
                  v-model="form.paramsText"
                  :autosize="{ minRows: 4, maxRows: 6 }"
                  class="params-editor"
                  placeholder='例如 {"warehouse":"WH-001"}'
                />
              </t-form-item>
            </t-col>
            <t-col :span="24">
              <t-form-item>
                <div class="tdesign-starter-action-bar">
                  <t-button theme="primary" type="submit" :loading="submitting">生成并下载</t-button>
                  <t-button variant="outline" @click="loadDefinitions">刷新模板</t-button>
                </div>
              </t-form-item>
            </t-col>
          </t-row>
        </t-form>

        <t-card title="可用模板" size="small">
          <t-table row-key="definitionId" :columns="columns" :data="definitions" :loading="loadingDefs" bordered />
        </t-card>
      </t-space>
    </t-card>
  </div>
</template>

<script setup lang="ts">
import type { FormRule, PrimaryTableCol, SubmitContext } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { onMounted, reactive, ref } from 'vue';

import { createPrintJob, downloadPrintJobPdf, fetchPrintDefinitions, type PrintDefinition } from '@/api/print';

interface PrintFormModel {
  businessRef: string;
  definitionId: string;
  mode: string;
  paramsText: string;
}

const definitions = ref<PrintDefinition[]>([]);
const loadingDefs = ref(false);
const submitting = ref(false);

const form = reactive<PrintFormModel>({
  businessRef: '',
  definitionId: '',
  mode: 'preview',
  paramsText: '{}',
});

const rules: Record<string, FormRule[]> = {
  definitionId: [{ required: true, message: '请选择打印模板', trigger: 'change' }],
  businessRef: [{ required: true, message: '请输入业务单号', trigger: 'blur' }],
  paramsText: [{ required: true, message: '请输入 JSON 参数，至少为 {}', trigger: 'blur' }],
};

const columns: PrimaryTableCol[] = [
  { colKey: 'name', title: '模板名称', minWidth: 180 },
  { colKey: 'definitionId', title: '定义 ID', minWidth: 220 },
  { colKey: 'templateType', title: '模板类型', minWidth: 160 },
  { colKey: 'permission', title: '权限标识', minWidth: 220 },
];

const loadDefinitions = async () => {
  loadingDefs.value = true;
  try {
    const list = await fetchPrintDefinitions();
    definitions.value = list || [];
    if (!form.definitionId && definitions.value.length > 0) {
      form.definitionId = definitions.value[0].definitionId;
    }
  } catch (error) {
    const msg = error instanceof Error ? error.message : '加载打印模板失败';
    MessagePlugin.error(msg || '加载打印模板失败');
  } finally {
    loadingDefs.value = false;
  }
};

const parseParams = (value: string): Record<string, unknown> => {
  const source = String(value || '').trim();
  if (!source) return {};
  let parsed: unknown;
  try {
    parsed = JSON.parse(source);
  } catch {
    throw new Error('扩展参数必须是合法 JSON');
  }
  if (!parsed || Array.isArray(parsed) || typeof parsed !== 'object') {
    throw new Error('扩展参数必须是 JSON 对象');
  }
  return parsed as Record<string, unknown>;
};

const onSubmit = async (ctx: SubmitContext) => {
  if (ctx.validateResult !== true) return;
  submitting.value = true;
  try {
    const params = parseParams(form.paramsText);
    const result = await createPrintJob({
      definitionId: form.definitionId,
      businessRef: form.businessRef.trim(),
      mode: form.mode,
      params,
    });
    await downloadPrintJobPdf(result.jobId);
    MessagePlugin.success('打印文件已生成并开始下载');
  } catch (error) {
    const msg = error instanceof Error ? error.message : '生成打印任务失败';
    MessagePlugin.error(msg || '生成打印任务失败');
  } finally {
    submitting.value = false;
  }
};

onMounted(loadDefinitions);
</script>

<style scoped lang="less">
.print-center {
  :deep(.t-card + .t-card) {
    margin-top: 16px;
  }
}

.params-editor {
  :deep(textarea) {
    font-family: 'Consolas', 'Courier New', monospace;
  }
}
</style>
