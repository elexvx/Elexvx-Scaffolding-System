<template>
  <t-card title="文件下载" :bordered="false">
    <t-space direction="vertical" style="width: 100%">
      <t-space class="download-toolbar">
        <t-button theme="primary" @click="openCreate">新增文件</t-button>
        <t-button variant="outline" @click="load">刷新</t-button>
      </t-space>

      <t-table
        :columns="columns"
        :data="list"
        :loading="loading"
        row-key="id"
        :pagination="pagination"
        class="custom-table"
        @page-change="onPageChange"
      >
        <template #index="{ rowIndex }">
          {{ query.page * query.size + rowIndex + 1 }}
        </template>
        <template #file="{ row }">
          <div class="download-file-cell">
            <div class="download-file-info">
              <span class="download-file-link" :title="row.fileName" @click="openPreview(row)">
                {{ row.fileName }}
              </span>
              <span class="download-file-suffix">{{
                (row.suffix || resolveSuffix(row.fileName)).toUpperCase() || '文件'
              }}</span>
            </div>
          </div>
        </template>
        <template #actions="{ row }">
          <t-space size="small">
            <t-button size="small" variant="text" @click="openEdit(row)">编辑</t-button>
            <t-popconfirm theme="danger" content="确定删除该文件吗？" @confirm="handleDelete(row)">
              <t-button size="small" variant="text" theme="danger">删除</t-button>
            </t-popconfirm>
          </t-space>
        </template>
      </t-table>
    </t-space>

    <t-dialog
      v-model:visible="previewVisible"
      width="90%"
      :header="previewTitle"
      placement="center"
      attach="body"
      :close-on-overlay-click="false"
      :close-on-esc-keydown="false"
      class="download-preview-dialog"
      @confirm="previewVisible = false"
    >
      <div class="download-preview-body">
        <t-alert v-if="isLocalhostOfficePreview" theme="warning" style="margin-bottom: 8px">
          当前处于本地开发环境，微软 Office 在线预览无法访问本地文件，请部署到公网测试。
        </t-alert>
        <iframe v-if="previewSource" :src="previewSource" />
        <div v-else class="download-preview-empty">当前文件暂不支持在线预览</div>
      </div>
      <template #footer>
        <div class="download-preview-footer">
          <t-button variant="outline" :disabled="!previewContext?.fileUrl" @click="openPreviewInNewTab">
            新窗口打开
          </t-button>
          <t-button theme="primary" :disabled="!previewContext?.fileUrl" @click="downloadPreviewFile"> 下载 </t-button>
          <t-button variant="outline" @click="previewVisible = false">关闭</t-button>
        </div>
      </template>
    </t-dialog>

    <confirm-drawer
      v-model:visible="formVisible"
      :header="drawerTitle"
      size="760px"
      :confirm-btn="{ content: '提交', loading: saving }"
      @confirm="onConfirm"
    >
      <t-form
        ref="formRef"
        :data="form"
        :rules="rules"
        layout="vertical"
        label-align="top"
        class="download-drawer-form"
        @submit="save"
      >
        <t-row :gutter="[24, 24]">
          <t-col :xs="24" :sm="12">
            <t-form-item label="内容" name="content">
              <t-textarea
                v-model="form.content"
                :autosize="{ minRows: 2, maxRows: 4 }"
                placeholder="请描述文件用途或说明"
              />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="文件" name="fileUrl">
              <div class="download-upload-panel">
                <t-upload
                  v-model="files"
                  :auto-upload="false"
                  :max="1"
                  theme="file"
                  :accept="uploadAccept"
                  :disabled="uploadingFile"
                  :before-upload="handleBeforeUpload"
                  @select-change="handleSelectChange"
                  @remove="handleUploadRemove"
                />
                <div class="download-upload-tips">
                  <p>支持 Office/PDF/图片等常见格式。</p>
                  <p>编辑时可保留当前文件，如需替换请先移除再重新上传。</p>
                  <p>文件会先缓存在本地，点击提交后开始分片上传并支持断点续传，建议单文件大小 10GB 内。</p>
                </div>
                <div v-if="formMode === 'edit' && form.fileUrl && !pendingFile" class="download-existing-file">
                  <span>当前文件：</span>
                  <t-link theme="primary" hover="color" @click="openFileUrl(form.fileUrl)">
                    {{ form.fileName || '点击查看' }}
                  </t-link>
                  <span class="download-existing-hint">如需替换，请先移除再重新上传。</span>
                </div>
                <p v-if="uploadingFile" class="download-upload-caption">上传中 {{ uploadProgress }}%</p>
                <p v-else-if="uploadProgress > 0" class="download-upload-caption">已完成 {{ uploadProgress }}%</p>
                <p v-if="uploadError" class="download-upload-error">{{ uploadError }}</p>
              </div>
            </t-form-item>
          </t-col>
        </t-row>
      </t-form>
    </confirm-drawer>
  </t-card>
</template>
<script setup lang="ts">
import type {
  FormInstanceFunctions,
  FormRule,
  PageInfo,
  PrimaryTableCol,
  SubmitContext,
  UploadFile,
} from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue';

import type { FileResourceItem, FileResourcePayload } from '@/api/download';
import {
  cancelFileUploadSession,
  completeFileUpload,
  createFileResource,
  deleteFileResource,
  fetchFileResources,
  initFileUploadSession,
  updateFileResource,
  uploadFileChunk,
} from '@/api/download';
import ConfirmDrawer from '@/components/ConfirmDrawer.vue';
import { prefix } from '@/config/global';

interface PreviewContext {
  fileUrl: string;
  fileName: string;
  suffix?: string;
}

const OFFICE_EXTENSIONS = new Set(['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx']);
const CHUNK_SIZE = 5 * 1024 * 1024;
const list = ref<FileResourceItem[]>([]);
const total = ref(0);
const loading = ref(false);
const saving = ref(false);
const formVisible = ref(false);
const formMode = ref<'create' | 'edit'>('create');
const currentId = ref<number | null>(null);
const query = reactive({ page: 0, size: 10 });
const formRef = ref<FormInstanceFunctions>();
const files = ref<UploadFile[]>([]);
const pendingFile = ref<File | null>(null);
const uploadProgress = ref(0);
const uploadingFile = ref(false);
const uploadError = ref('');
const uploadSessionId = ref<string | null>(null);
const previewVisible = ref(false);
const previewContext = ref<PreviewContext | null>(null);
const previewBodyOverflowBackup = ref<string | null>(null);
const previewMainOverflowBackup = ref<string | null>(null);
const form = reactive<FileResourcePayload>({
  content: '',
  fileName: '',
  suffix: '',
  fileUrl: '',
});

const uploadAccept = '.pdf,.doc,.docx,.xls,.xlsx,.csv,.ppt,.pptx,.png,.jpg,.jpeg,.gif,.webp,.bmp';
const drawerTitle = computed(() => (formMode.value === 'create' ? '添加下载文件' : '编辑下载文件'));
const previewTitle = computed(() => previewContext.value?.fileName || '文件预览');
const resolveSuffix = (name: string) => {
  if (!name) return '';
  const dot = name.lastIndexOf('.');
  if (dot < 0 || dot === name.length - 1) return '';
  return name.substring(dot + 1).toLowerCase();
};

const columns: PrimaryTableCol[] = [
  { title: '序号', colKey: 'index', width: 90, align: 'center' },
  { title: '内容', colKey: 'content', minWidth: 220 },
  { title: '文件', colKey: 'file', minWidth: 280 },
  { title: '创建时间', colKey: 'createdAt', width: 180 },
  { title: '操作', colKey: 'actions', width: 160, align: 'center' },
];

const rules = computed<Record<string, FormRule[]>>(() => ({
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  fileUrl: [
    {
      validator: (val) => !!val || !!pendingFile.value,
      message: '请选择文件',
      trigger: 'change',
    },
  ],
}));

const previewSource = computed(() => {
  const ctx = previewContext.value;
  if (!ctx?.fileUrl) return '';
  const suffix = ctx.suffix || resolveSuffix(ctx.fileName);
  const absolute = buildAbsoluteUrl(ctx.fileUrl);
  if (suffix && OFFICE_EXTENSIONS.has(suffix)) {
    return `https://view.officeapps.live.com/op/embed.aspx?src=${encodeURIComponent(absolute)}`;
  }
  // 如果是PDF且没有指定view参数，添加view=FitH以适配宽度
  if (suffix === 'pdf' && !absolute.includes('#')) {
    return `${absolute}#view=FitH`;
  }
  return absolute;
});

const isLocalhostOfficePreview = computed(() => {
  const ctx = previewContext.value;
  if (!ctx || typeof window === 'undefined') return false;
  const isLocal = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';
  if (!isLocal) return false;
  const suffix = ctx.suffix || resolveSuffix(ctx.fileName);
  return suffix && OFFICE_EXTENSIONS.has(suffix);
});

const pagination = computed(() => {
  return {
    current: query.page + 1,
    pageSize: query.size,
    total: total.value,
    showJumper: true,
  };
});

const load = async () => {
  loading.value = true;
  try {
    const data = await fetchFileResources({ page: query.page, size: query.size });
    list.value = data.list;
    total.value = data.total;
  } catch (error) {
    handleApiError(error, '加载失败');
  } finally {
    loading.value = false;
  }
};

const onPageChange = (pageInfo: PageInfo) => {
  query.page = pageInfo.current - 1;
  query.size = pageInfo.pageSize;
  load();
};

const resetUploadState = async () => {
  uploadProgress.value = 0;
  updateUploadProgress(0);
  uploadingFile.value = false;
  uploadError.value = '';
  pendingFile.value = null;
  files.value = [];
  if (uploadSessionId.value) {
    try {
      await cancelFileUploadSession(uploadSessionId.value);
    } catch {
      // ignore
    }
    uploadSessionId.value = null;
  }
};

const resetForm = () => {
  form.content = '';
  form.fileName = '';
  form.fileUrl = '';
  form.suffix = '';
  void resetUploadState();
};

const openCreate = () => {
  formMode.value = 'create';
  currentId.value = null;
  resetForm();
  formVisible.value = true;
};

const openEdit = (row: FileResourceItem) => {
  formMode.value = 'edit';
  currentId.value = row.id;
  form.content = row.content;
  form.fileName = row.fileName;
  form.fileUrl = row.fileUrl;
  form.suffix = row.suffix || resolveSuffix(row.fileName);
  files.value = row.fileUrl
    ? [
        {
          url: row.fileUrl,
          name: row.fileName,
          status: 'success',
          percent: 100,
          uid: String(Date.now()),
        },
      ]
    : [];
  pendingFile.value = null;
  uploadProgress.value = 0;
  uploadError.value = '';
  uploadSessionId.value = null;
  formVisible.value = true;
};

const buildPayload = (): FileResourcePayload => ({
  content: String(form.content || '').trim(),
  fileName: String(form.fileName || '').trim(),
  suffix: String(form.suffix || '').trim(),
  fileUrl: String(form.fileUrl || '').trim(),
});

const onConfirm = () => {
  if (saving.value || uploadingFile.value) return;
  formRef.value?.submit();
};

const ensureFileUploaded = async () => {
  if (!pendingFile.value) {
    if (!form.fileUrl) {
      throw new Error('请先上传文件');
    }
    return;
  }
  uploadingFile.value = true;
  uploadError.value = '';
  try {
    const fingerprint = buildFingerprint(pendingFile.value);
    const session = await initFileUploadSession({
      fileName: pendingFile.value.name,
      fileSize: pendingFile.value.size,
      chunkSize: CHUNK_SIZE,
      fingerprint,
    });
    uploadSessionId.value = session.uploadId;
    const chunkSize = session.chunkSize || CHUNK_SIZE;
    const total = session.totalChunks || Math.max(1, Math.ceil(pendingFile.value.size / chunkSize));
    const uploadedChunks = new Set(session.uploadedChunks || []);
    updateUploadProgress(Math.round((uploadedChunks.size / total) * 100));
    for (let index = 0; index < total; index++) {
      if (uploadedChunks.has(index)) continue;
      const start = index * chunkSize;
      const end = Math.min(pendingFile.value.size, start + chunkSize);
      const chunk = pendingFile.value.slice(start, end);
      await uploadFileChunk(session.uploadId, index, chunk);
      uploadedChunks.add(index);
      updateUploadProgress(Math.round((uploadedChunks.size / total) * 100));
    }
    const result = await completeFileUpload({ uploadId: session.uploadId, page: 'console-download' });
    form.fileUrl = result.url;
    form.fileName = pendingFile.value.name;
    form.suffix = resolveSuffix(pendingFile.value.name);
    updateUploadProgress(100);
    files.value = [
      {
        uid: `${Date.now()}-${pendingFile.value.name}`,
        name: pendingFile.value.name,
        status: 'success',
        percent: 100,
      },
    ];
    pendingFile.value = null;
    uploadSessionId.value = null;
  } catch (error) {
    uploadError.value = String((error as any)?.message || '上传失败');
    throw error;
  } finally {
    uploadingFile.value = false;
  }
};

const save = async (ctx: SubmitContext) => {
  if (ctx.validateResult !== true) return;
  saving.value = true;
  try {
    await ensureFileUploaded();
    const payload = buildPayload();
    if (formMode.value === 'create') {
      await createFileResource(payload);
      MessagePlugin.success('添加成功');
    } else if (currentId.value !== null) {
      await updateFileResource(currentId.value, payload);
      MessagePlugin.success('更新成功');
    }
    formVisible.value = false;
    await load();
  } catch (error) {
    handleApiError(error, '保存失败');
  } finally {
    saving.value = false;
  }
};

const handleDelete = async (row: FileResourceItem) => {
  try {
    await deleteFileResource(row.id);
    MessagePlugin.success('删除成功');
    load();
  } catch (error) {
    handleApiError(error, '删除失败');
  }
};

const handleBeforeUpload = (file: UploadFile) => {
  const rawFile = ((file as any)?.raw || (file as any)) as any;
  if (!rawFile || typeof rawFile.name !== 'string' || typeof rawFile.size !== 'number') return true;
  pendingFile.value = rawFile as File;
  files.value = [
    {
      uid: `${Date.now()}-${rawFile.name}`,
      name: rawFile.name,
      status: 'waiting',
      percent: 0,
    },
  ];
  uploadError.value = '';
  uploadProgress.value = 0;
  return true;
};

const handleSelectChange = (selected: File[]) => {
  if (!selected?.length) return;
  pendingFile.value = selected[0];
  uploadError.value = '';
  uploadProgress.value = 0;
  (formRef.value as any)?.validate?.({ fields: ['fileUrl'] });
};

const handleUploadRemove = () => {
  form.fileUrl = '';
  form.fileName = '';
  form.suffix = '';
  void resetUploadState();
  (formRef.value as any)?.validate?.({ fields: ['fileUrl'] });
};

const updateUploadProgress = (percent: number) => {
  const safePercent = Math.min(100, Math.max(0, percent));
  uploadProgress.value = safePercent;
  if (files.value.length) {
    files.value = [
      {
        ...files.value[0],
        status: safePercent >= 100 ? 'success' : 'progress',
        percent: safePercent,
      },
    ];
  }
};

const buildFingerprint = (file: File) => {
  const origin = `${file.name}|${file.size}|${file.lastModified}`;
  if (typeof window === 'undefined') return origin;
  try {
    return window.btoa(unescape(encodeURIComponent(origin)));
  } catch {
    return window.btoa(origin);
  }
};

const buildAbsoluteUrl = (url: string) => {
  if (!url) return '';
  if (/^https?:\/\//i.test(url)) return url;
  const base = typeof window !== 'undefined' ? window.location.origin : '';
  const normalized = url.startsWith('/') ? url : `/${url}`;
  return `${base}${normalized}`;
};

const openPreview = (row: FileResourceItem) => {
  if (!row.fileUrl) {
    MessagePlugin.warning('文件地址未准备好');
    return;
  }
  previewContext.value = {
    fileName: row.fileName,
    fileUrl: row.fileUrl,
    suffix: row.suffix || resolveSuffix(row.fileName),
  };
  previewVisible.value = true;
};

const openPreviewInNewTab = () => {
  if (!previewContext.value?.fileUrl || typeof window === 'undefined') return;
  window.open(previewContext.value.fileUrl, '_blank');
};

const downloadPreviewFile = () => {
  if (!previewContext.value?.fileUrl || typeof document === 'undefined') return;
  const link = document.createElement('a');
  link.href = previewContext.value.fileUrl;
  link.target = '_blank';
  link.rel = 'noreferrer';
  link.click();
};

const openFileUrl = (url: string) => {
  if (!url || typeof window === 'undefined') return;
  window.open(url, '_blank');
};

const handleApiError = (error: unknown, fallback: string) => {
  const raw = String((error as any)?.message || fallback);
  const message = raw.replace(/\s*\[\d{3}\]\s*$/, '').trim();
  MessagePlugin.error(message || fallback);
};

watch(formVisible, (visible) => {
  if (!visible) {
    void resetUploadState();
  }
});

watch(previewVisible, (visible) => {
  if (typeof document === 'undefined') return;
  const body = document.body;
  const mainWrapper = document.querySelector<HTMLElement>(`.${prefix}-main-wrapper`);
  if (visible) {
    if (previewBodyOverflowBackup.value === null) previewBodyOverflowBackup.value = body.style.overflow;
    try {
      body.style.overflow = 'hidden';
      // 同时也尝试锁定 t-layout 防止内容滚动
      const layout = document.querySelector<HTMLElement>('.t-layout');
      if (layout) layout.style.overflow = 'hidden';
    } catch {
      // ignore
    }
    if (mainWrapper) {
      if (previewMainOverflowBackup.value === null) previewMainOverflowBackup.value = mainWrapper.style.overflow;
      try {
        mainWrapper.style.overflow = 'hidden';
      } catch {
        // ignore
      }
    }
    return;
  }
  try {
    body.style.overflow = previewBodyOverflowBackup.value ?? '';
    previewBodyOverflowBackup.value = null;
    const layout = document.querySelector<HTMLElement>('.t-layout');
    if (layout) layout.style.overflow = '';
  } catch {
    // ignore
  }
  if (mainWrapper) {
    try {
      mainWrapper.style.overflow = previewMainOverflowBackup.value ?? '';
      previewMainOverflowBackup.value = null;
    } catch {
      // ignore
    }
  }
});

onMounted(load);
onUnmounted(() => {
  if (typeof document === 'undefined') return;
  const body = document.body;
  const mainWrapper = document.querySelector<HTMLElement>(`.${prefix}-main-wrapper`);
  try {
    body.style.overflow = previewBodyOverflowBackup.value ?? '';
    previewBodyOverflowBackup.value = null;
    const layout = document.querySelector<HTMLElement>('.t-layout');
    if (layout) layout.style.overflow = '';
  } catch {
    // ignore
  }
  if (mainWrapper) {
    try {
      mainWrapper.style.overflow = previewMainOverflowBackup.value ?? '';
      previewMainOverflowBackup.value = null;
    } catch {
      // ignore
    }
  }
});
</script>
<style scoped lang="less">
.custom-table {
  :deep(.t-table__header) {
    background-color: var(--td-bg-color-secondarycontainer);
  }
}

.download-toolbar {
  flex-wrap: wrap;
  gap: 12px;
}

.download-drawer-form {
  :deep(.t-form__controls),
  :deep(.t-form__controls-content) {
    width: 100%;
    max-width: 100%;
  }

  :deep(.t-input),
  :deep(.t-textarea),
  :deep(.t-textarea__inner),
  :deep(.t-upload) {
    width: 100%;
  }
}

.download-file-cell {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
}

.download-file-info {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
}

.download-file-link {
  color: #1f64ff;
  font-weight: 500;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
  max-width: 100%;
}

.download-file-link:hover {
  text-decoration: underline;
}

.download-file-suffix {
  font-size: 12px;
  color: var(--td-text-color-3);
}

.download-action-link {
  padding: 0;
  color: var(--td-text-color-primary);
}

.download-action-link:hover {
  color: var(--td-text-color-primary);
}

.download-preview-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.download-preview-body {
  height: 75vh;

  iframe {
    width: 100%;
    height: 100%;
    border: none;
  }
}

.download-preview-empty {
  width: 100%;
  height: 100%;
  border-radius: 6px;
  background: var(--td-bg-color-2);
  color: var(--td-text-color-3);
  display: flex;
  align-items: center;
  justify-content: center;
}

.download-upload-caption {
  margin-top: 0;
  font-size: 12px;
  color: var(--td-text-color-2);
}

.download-upload-panel {
  width: 100%;
  padding: 16px;
  border-radius: var(--td-radius-default);
  border: 1px solid var(--td-component-stroke);
  background: var(--td-bg-color-container);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.download-upload-tips {
  margin-top: 0;
  font-size: 12px;
  color: var(--td-text-color-3);
  line-height: 20px;

  p {
    margin-bottom: 4px;
    margin-top: 0;
  }
}

.download-existing-file {
  margin-top: 0;
  font-size: 12px;
  color: var(--td-text-color-2);
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}

.download-existing-hint {
  color: var(--td-text-color-3);
}

.download-upload-error {
  margin-top: 0;
  font-size: 12px;
  color: var(--td-danger-color);
}

:deep(.download-preview-dialog) {
  /* 固定定位并居中，确保不随页面滚动 */
  position: fixed !important;
  top: 50% !important;
  left: 50% !important;
  transform: translate(-50%, -50%) !important;
  margin: 0 !important;

  .t-dialog__header {
    padding: 2px 24px 0px;
  }

  .t-dialog__body {
    padding: 0 24px;
    padding-bottom: 0;
    overflow: hidden;
  }

  .t-dialog__footer {
    padding: 2px 16px 4px;
  }

  .t-dialog__close {
    font-size: 24px;
    top: 8px;
    right: 8px;
  }
}
</style>
