<template>
  <div class="ai-setting">
    <t-card title="AI 接口设置" :bordered="false">
      <t-space direction="vertical" style="width: 100%" size="large">
        <t-alert
          theme="info"
          message="支持多家主流大模型平台，建议使用与 OpenAI 兼容的接口。保存前可先点击“连接测试”验证密钥与地址。"
          :close="false"
        />
        <t-button theme="primary" @click="openCreate">新增接入</t-button>
        <t-table :loading="loading" :data="providers" :columns="columns" row-key="id" bordered>
          <template #vendor="{ row }">
            <t-tag theme="success" variant="light-outline">{{ vendorLabel(row.vendor) }}</t-tag>
          </template>
          <template #status="{ row }">
            <t-space size="small">
              <t-tag v-if="row.enabled" theme="success" variant="light-outline">已启用</t-tag>
              <t-tag v-else theme="default" variant="light-outline">未启用</t-tag>
              <t-tag v-if="row.isDefaultProvider" theme="primary" variant="light-outline">默认</t-tag>
            </t-space>
          </template>
          <template #test="{ row }">
            <div class="status-cell">
              <div class="status-text">
                {{ row.lastTestStatus || '未测试' }}
              </div>
              <div v-if="row.lastTestMessage" class="status-tip">{{ row.lastTestMessage }}</div>
            </div>
          </template>
          <template #op="{ row }">
            <t-space size="small">
              <t-button size="small" variant="text" @click="openEdit(row)">编辑</t-button>
              <t-button size="small" variant="text" @click="handleTestSaved(row.id)">测试</t-button>
              <t-button size="small" variant="text" :disabled="row.isDefaultProvider" @click="handleSetDefault(row)">
                设为默认
              </t-button>
              <t-popconfirm theme="danger" content="确认删除该配置？" @confirm="handleDelete(row.id)">
                <t-button size="small" variant="text" theme="danger">删除</t-button>
              </t-popconfirm>
            </t-space>
          </template>
        </t-table>
      </t-space>
    </t-card>

    <t-card title="接口调试" :bordered="false" style="margin-top: 16px">
      <t-form layout="vertical" label-align="top">
        <div class="debug-split">
          <div class="debug-left">
            <t-form-item label="选择提供商">
              <t-select
                v-model="debugProviderId"
                :options="providers.map((p) => ({ label: p.name, value: p.id }))"
                placeholder="请选择已配置的 AI 提供商"
                clearable
              />
            </t-form-item>

            <t-form-item label="测试问题" style="margin-top: 16px">
              <t-textarea
                v-model="debugMessage"
                placeholder="输入一段提问，用于验证大模型是否连通"
                :maxlength="500"
                :autosize="{ minRows: 5, maxRows: 10 }"
              />
            </t-form-item>

            <div class="debug-actions">
              <t-space size="16px">
                <t-button :disabled="!debugProviderId" :loading="debugLoading" theme="primary" @click="runDebug">
                  <template #icon><t-icon name="send" /></template>
                  发送测试
                </t-button>
                <t-button variant="outline" @click="debugOutput = ''">
                  <template #icon><t-icon name="delete" /></template>
                  清空结果
                </t-button>
              </t-space>
            </div>
          </div>

          <div class="debug-right">
            <t-form-item label="返回结果" class="result-form-item">
              <div class="debug-result-wrapper">
                <t-loading v-if="debugLoading" size="small" text="AI 正在思考中..." />
                <div v-else-if="debugOutput" class="result-content">
                  <t-tag theme="success" variant="light" style="margin-bottom: 8px">HTTP 200 OK</t-tag>
                  <div class="result-text">{{ debugOutput }}</div>
                </div>
                <div v-else class="result-placeholder">
                  <t-icon name="chat" size="large" />
                  <p>点击“发送测试”后，此处将显示 AI 的回复内容</p>
                </div>
              </div>
            </t-form-item>
          </div>
        </div>
      </t-form>
    </t-card>

    <t-drawer v-model:visible="formVisible" :footer="false" :close-btn="true" size="600px" header="配置提供商">
      <t-form layout="vertical" label-align="top" :data="form" :rules="rules" @submit="submitForm">
        <t-row :gutter="[16, 24]">
          <t-col :xs="24" :sm="12">
            <t-form-item label="名称" name="name">
              <t-input v-model="form.name" placeholder="为该接入起一个名字" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="厂商" name="vendor">
              <t-select v-model="form.vendor" :options="vendorOptions" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="Base URL" name="baseUrl">
              <t-input v-model="form.baseUrl" placeholder="https://api.openai.com" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="Endpoint" name="endpointPath" help="默认 /v1/chat/completions，可按需覆盖">
              <t-input v-model="form.endpointPath" placeholder="/v1/chat/completions" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="模型" name="model">
              <t-input v-model="form.model" placeholder="gpt-4o, deepseek-chat, moonshot-v1-8k ..." />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="API Key" name="apiKey" help="留空表示沿用已保存的密钥">
              <t-input v-model="form.apiKey" type="password" placeholder="sk-xxxxx" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="6">
            <t-form-item label="Temperature">
              <t-slider v-model="form.temperature" :max="1" :step="0.1" :min="0" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="6">
            <t-form-item label="最大 Tokens">
              <t-input-number v-model="form.maxTokens" theme="normal" placeholder="可选" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="6">
            <t-form-item label="API Version">
              <t-input v-model="form.apiVersion" placeholder="Azure 需填写" />
            </t-form-item>
          </t-col>
          <t-col :xs="12" :sm="6">
            <t-form-item label="默认提供商">
              <t-switch v-model="form.isDefaultProvider" />
            </t-form-item>
          </t-col>
          <t-col :xs="12" :sm="6">
            <t-form-item label="启用">
              <t-switch v-model="form.enabled" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="自定义 Header(JSON)">
              <t-textarea
                v-model="form.extraHeaders"
                placeholder='例如 {"x-foo":"bar"}'
                :autosize="{ minRows: 2, maxRows: 4 }"
              />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="备注">
              <t-textarea v-model="form.remark" :maxcharacter="200" placeholder="为团队说明该密钥用途" />
            </t-form-item>
          </t-col>
        </t-row>

        <div class="drawer-actions">
          <t-space>
            <t-button :loading="testing" variant="outline" @click="handleTestForm">连接测试</t-button>
            <t-button :loading="saving" theme="primary" type="submit">保存</t-button>
          </t-space>
          <div v-if="formTestResult" class="test-tip">{{ formTestResult }}</div>
        </div>
      </t-form>
    </t-drawer>
  </div>
</template>
<script setup lang="ts">
import type { FormRule, PrimaryTableCol, SubmitContext } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted, reactive, ref } from 'vue';

import type { AiProvider, AiProviderPayload } from '@/api/system/ai';
import { deleteAiProvider, fetchAiProviders, saveAiProvider, testAiProvider, testSavedProvider } from '@/api/system/ai';
import { useDictionary } from '@/hooks/useDictionary';
import { buildDictOptions, resolveLabel } from '@/utils/dict';
import { request } from '@/utils/request';

const providers = ref<AiProvider[]>([]);
const loading = ref(false);
const formVisible = ref(false);
const saving = ref(false);
const testing = ref(false);
const formTestResult = ref('');
const formMode = ref<'create' | 'edit'>('create');

const form = reactive<AiProviderPayload>({
  name: '',
  vendor: 'OPENAI',
  baseUrl: 'https://api.openai.com',
  endpointPath: '/v1/chat/completions',
  model: '',
  apiKey: '',
  temperature: 0.7,
  maxTokens: undefined,
  isDefaultProvider: false,
  enabled: true,
  extraHeaders: '',
  remark: '',
  reuseApiKey: false,
});

const vendorDict = useDictionary('ai_vendor');
const fallbackVendorOptions = [
  { label: 'OpenAI / 兼容', value: 'OPENAI' },
  { label: 'Azure OpenAI', value: 'AZURE_OPENAI' },
  { label: 'DeepSeek', value: 'DEEPSEEK' },
  { label: '月之暗面 / Moonshot', value: 'MOONSHOT' },
  { label: '通义千问 (兼容模式)', value: 'QWEN' },
  { label: 'Ollama 本地部署', value: 'OLLAMA' },
];
const VENDOR_VALUES = ['OPENAI', 'AZURE_OPENAI', 'DEEPSEEK', 'MOONSHOT', 'QWEN', 'OLLAMA'];
const vendorOptions = computed(() =>
  buildDictOptions(vendorDict.items.value, fallbackVendorOptions, VENDOR_VALUES),
);

const vendorLabelMap: Record<string, string> = {
  OPENAI: 'OpenAI / 兼容',
  AZURE_OPENAI: 'Azure OpenAI',
  DEEPSEEK: 'DeepSeek',
  MOONSHOT: '月之暗面 / Moonshot',
  QWEN: '通义千问 (兼容模式)',
  OLLAMA: 'Ollama 本地部署',
};

const rules: Record<string, FormRule[]> = {
  name: [{ required: true, message: '请输入名称', type: 'error' }],
  vendor: [{ required: true, message: '请选择厂商', type: 'error' }],
  baseUrl: [{ required: true, message: '请输入 Base URL', type: 'error' }],
  model: [{ required: true, message: '请输入模型名称', type: 'error' }],
};

const columns: PrimaryTableCol[] = [
  { colKey: 'name', title: '名称', width: 160 },
  { colKey: 'vendor', title: '厂商', width: 140, cell: 'vendor' },
  { colKey: 'model', title: '模型', width: 180 },
  { colKey: 'baseUrl', title: 'Base URL', width: 220 },
  { colKey: 'status', title: '状态', width: 160, cell: 'status' },
  { colKey: 'lastTestStatus', title: '最近测试', width: 220, cell: 'test' },
  { colKey: 'op', title: '操作', width: 260, cell: 'op' },
];

const debugProviderId = ref<number>();
const debugMessage = ref('你好，我在做接口连通性测试，请回复一段 10 字以内的问候。');
const debugOutput = ref('');
const debugLoading = ref(false);

const vendorLabel = (value: string) => resolveLabel(value, vendorDict.items.value, vendorLabelMap);

const loadProviders = async () => {
  loading.value = true;
  try {
    providers.value = await fetchAiProviders();
    if (!debugProviderId.value) {
      const def = providers.value.find((p) => p.isDefaultProvider) || providers.value[0];
      debugProviderId.value = def?.id;
    }
  } finally {
    loading.value = false;
  }
};

const resetForm = () => {
  Object.assign(form, {
    id: undefined,
    name: '',
    vendor: 'OPENAI',
    baseUrl: 'https://api.openai.com',
    endpointPath: '/v1/chat/completions',
    model: '',
    apiKey: '',
    apiVersion: '',
    temperature: 0.7,
    maxTokens: undefined,
    isDefaultProvider: false,
    enabled: true,
    extraHeaders: '',
    remark: '',
    reuseApiKey: false,
  });
  formTestResult.value = '';
};

const openCreate = () => {
  resetForm();
  formMode.value = 'create';
  formVisible.value = true;
};

const openEdit = (item: AiProvider) => {
  resetForm();
  formMode.value = 'edit';
  Object.assign(form, {
    ...item,
    reuseApiKey: true,
    apiKey: '',
  });
  formVisible.value = true;
};

const handleTestForm = async () => {
  testing.value = true;
  formTestResult.value = '正在连接测试中，请稍候...';
  const msg = MessagePlugin.info('正在发起连接测试...', 0);
  try {
    const res = await testAiProvider(form);
    formTestResult.value = res.message;
    MessagePlugin.close(msg);
    MessagePlugin[res.success ? 'success' : 'error'](res.message);
  } catch (err: any) {
    MessagePlugin.close(msg);
    const errorMsg = err.message || '测试请求失败，请检查网络或配置';
    formTestResult.value = errorMsg;
    MessagePlugin.error(errorMsg);
  } finally {
    testing.value = false;
  }
};

const submitForm = async (ctx?: SubmitContext) => {
  if (ctx && ctx.validateResult !== true) return;
  saving.value = true;
  const res = await saveAiProvider(form);
  try {
    MessagePlugin.success('配置已保存');
    formVisible.value = false;
    await loadProviders();
    if (res?.id) debugProviderId.value = res.id;
  } finally {
    saving.value = false;
  }
};

const handleDelete = async (id: number) => {
  await deleteAiProvider(id);
  MessagePlugin.success('已删除');
  loadProviders();
};

const handleTestSaved = async (id: number) => {
  testing.value = true;
  const msg = MessagePlugin.info('正在发起连接测试...', 0);
  try {
    const res = await testSavedProvider(id);
    MessagePlugin.close(msg);
    MessagePlugin[res.success ? 'success' : 'error'](res.message);
    await loadProviders();
  } catch (err: any) {
    MessagePlugin.close(msg);
    MessagePlugin.error(err.message || '测试请求失败');
  } finally {
    testing.value = false;
  }
};

const handleSetDefault = async (item: AiProvider) => {
  const payload: AiProviderPayload = {
    ...item,
    apiKey: '',
    reuseApiKey: true,
    isDefaultProvider: true,
  };
  await saveAiProvider(payload);
  MessagePlugin.success('已设为默认');
  loadProviders();
};

const runDebug = async () => {
  if (!debugProviderId.value || !debugMessage.value.trim()) {
    MessagePlugin.warning('请选择提供商并输入测试内容');
    return;
  }
  debugLoading.value = true;
  try {
    const res = await request.post<{ reply: string }>({
      url: '/ai/chat',
      data: { providerId: debugProviderId.value, message: debugMessage.value },
    });
    debugOutput.value = res.reply || '无内容返回';
  } catch (err: any) {
    debugOutput.value = String(err?.message || '调试失败');
  } finally {
    debugLoading.value = false;
  }
};

onMounted(() => {
  void vendorDict.load();
  loadProviders();
});
</script>
<style scoped lang="less">
.ai-setting {
  :deep(.t-card__title) {
    font-weight: 600;
  }
}

.drawer-actions {
  margin-top: 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;

  .test-tip {
    color: var(--td-text-color-secondary);
  }
}

.status-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;

  .status-text {
    font-weight: 500;
  }

  .status-tip {
    color: var(--td-text-color-secondary);
  }
}

.debug-actions {
  display: flex;
  margin-top: 16px;
}

.debug-split {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 24px;
  align-items: stretch;
}

.debug-left {
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.debug-right {
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.debug-result-wrapper {
  background-color: var(--td-bg-color-container-hover);
  border-radius: var(--td-radius-medium);
  padding: 16px;
  min-height: 300px;
  height: 100%;
  border: 1px solid var(--td-component-border);
  display: flex;
  flex-direction: column;
  flex: 1 1 auto;
  width: 100%;

  .result-placeholder {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: var(--td-text-color-placeholder);
    gap: 8px;

    p {
      font-size: 14px;
    }
  }

  .result-content {
    width: 100%;
    flex: 1;
    overflow-y: auto;

    .result-text {
      white-space: pre-wrap;
      line-height: 1.6;
      color: var(--td-text-color-primary);
      font-family: var(--td-font-family-code, monospace);
    }
  }
}

.result-form-item {
  height: 100%;
  width: 100%;
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;

  :deep(.t-form__controls) {
    width: 100%;
  }

  :deep(.t-form__item-content) {
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  :deep(.t-form__controls-content) {
    height: 100%;
    width: 100%;
  }
}

@media (max-width: 960px) {
  .debug-split {
    grid-template-columns: 1fr;
    align-items: flex-start;
  }

  .debug-left,
  .debug-right {
    width: 100%;
    min-width: 0;
  }

  .debug-result-wrapper {
    min-height: 240px;
  }
}
</style>
