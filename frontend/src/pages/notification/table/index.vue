<template>
  <div class="notification-table">
    <t-card title="通知管理" :bordered="false">
      <t-space style="flex-wrap: wrap; margin-bottom: 24px" size="24px">
        <t-input v-model="query.keyword" placeholder="请输入标题/摘要" style="width: 240px" @enter="load" />
        <t-select
          v-model="query.status"
          clearable
          :options="statusOptions"
          placeholder="通知状态"
          style="width: 160px"
        />
        <t-select
          v-model="query.priority"
          clearable
          :options="priorityOptions"
          placeholder="优先级"
          style="width: 160px"
        />
        <t-button theme="primary" @click="load">查询</t-button>
        <t-button variant="outline" @click="reset">重置</t-button>
        <t-button theme="primary" @click="openCreate">新增通知</t-button>
      </t-space>

      <t-table
        :data="list"
        :columns="columns"
        row-key="id"
        :loading="loading"
        :pagination="pagination"
        @page-change="handlePageChange"
      >
        <template #priority="{ row }">
          <t-tag :theme="priorityTheme(row.priority)" variant="light-outline">{{ priorityLabel(row.priority) }}</t-tag>
        </template>
        <template #status="{ row }">
          <t-tag :theme="statusTheme(row.status)" variant="light-outline">
            {{ statusLabel(row.status) }}
          </t-tag>
        </template>
        <template #op="{ row }">
          <t-space size="small">
            <t-button size="small" variant="text" @click="openEdit(row)">编辑</t-button>
            <t-popconfirm v-if="row.status === 'published'" content="确认撤回该通知？" @confirm="togglePublish(row)">
              <t-button size="small" variant="text">撤回</t-button>
            </t-popconfirm>
            <t-button v-else size="small" variant="text" @click="togglePublish(row)">发布</t-button>
            <t-popconfirm theme="danger" content="确认删除该通知？" @confirm="handleDelete(row.id)">
              <t-button size="small" variant="text" theme="danger">删除</t-button>
            </t-popconfirm>
          </t-space>
        </template>
      </t-table>
    </t-card>

    <confirm-drawer
      v-model:visible="formVisible"
      :header="drawerTitle"
      size="720px"
      :confirm-btn="{ content: '保存', loading: saving }"
      @confirm="onConfirm"
    >
      <t-form ref="formRef" layout="vertical" :data="form" :rules="rules" @submit="save">
        <t-row :gutter="[16, 12]">
          <t-col :xs="24" :sm="12">
            <t-form-item label="标题" name="title">
              <t-input v-model="form.title" placeholder="请输入标题" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="摘要" name="summary">
              <t-input v-model="form.summary" placeholder="用于列表展示的简要说明" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="6">
            <t-form-item label="类型" name="type">
              <t-select v-model="form.type" :options="typeOptions" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="6">
            <t-form-item label="优先级" name="priority">
              <t-select v-model="form.priority" :options="priorityOptions" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="6">
            <t-form-item label="状态" name="status">
              <t-select v-model="form.status" :options="statusOptions" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="封面图片" name="coverUrl">
              <t-upload
                v-model="files"
                action="/api/system/file/upload?page=notification"
                name="file"
                :headers="uploadHeaders"
                :format-response="formatUploadResponse"
                theme="image"
                tips="请上传通知封面图"
                accept="image/*"
                :auto-upload="true"
                :locale="{
                  triggerUploadText: {
                    image: '点击上传封面',
                  },
                }"
                @success="handleUploadSuccess"
                @fail="(ctx) => handleUploadFail('封面图片', ctx)"
                @remove="handleRemove"
              />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="附件" name="attachmentUrl">
              <t-upload
                v-model="attachmentFiles"
                action="/api/system/file/upload?page=notification"
                name="file"
                :headers="uploadHeaders"
                :format-response="formatUploadResponse"
                theme="file"
                tips="支持图片、PDF、Word、Excel、PPT等常见格式"
                :auto-upload="true"
                :max="1"
                accept=".pdf,.doc,.docx,.xls,.xlsx,.csv,.ppt,.pptx,.png,.jpg,.jpeg,.gif,.webp,image/*,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,text/csv,application/vnd.ms-powerpoint,application/vnd.openxmlformats-officedocument.presentationml.presentation"
                :before-upload="beforeAttachmentUpload"
                @success="handleAttachmentUploadSuccess"
                @fail="(ctx) => handleUploadFail('附件', ctx)"
                @remove="handleAttachmentRemove"
              />
            </t-form-item>
          </t-col>
          <t-col :xs="24">
            <t-form-item label="正文" name="content">
              <rich-text-editor v-model="form.content" :min-height="260" />
            </t-form-item>
          </t-col>
        </t-row>
      </t-form>
    </confirm-drawer>
  </div>
</template>
<script setup lang="ts">
import type {
  FormInstanceFunctions,
  FormRule,
  PrimaryTableCol,
  SubmitContext,
  SuccessContext,
  UploadFile,
} from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, defineAsyncComponent, onMounted, reactive, ref } from 'vue';

import type { NotificationItem, NotificationPayload } from '@/api/notification';
import {
  createNotification,
  deleteNotification,
  fetchNotifications,
  publishNotification,
  updateNotification,
} from '@/api/notification';
import ConfirmDrawer from '@/components/ConfirmDrawer.vue';

const RichTextEditor = defineAsyncComponent(() => import('@/components/RichTextEditor.vue'));
import { useUserStore } from '@/store';

const list = ref<NotificationItem[]>([]);
const loading = ref(false);
const saving = ref(false);

const userStore = useUserStore();
const uploadHeaders = computed(() => ({
  Authorization: userStore.token,
}));
const formatUploadResponse = (res: any) => {
  let payload = res;
  if (typeof res === 'string') {
    try {
      payload = JSON.parse(res);
    } catch {
      payload = { message: res };
    }
  }
  if (payload?.code === 0) {
    return { ...payload, url: payload?.data?.url };
  }
  return { ...payload, error: payload?.message || '上传失败' };
};

const handleUploadFail = (label: string, context: any) => {
  const msg = context?.response?.message || context?.response?.error || context?.error?.message || '上传失败';
  MessagePlugin.error(`${label}上传失败: ${msg}`);
};

const query = reactive({
  keyword: '',
  priority: '',
  status: '',
});

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
});

const formVisible = ref(false);
const formMode = ref<'create' | 'edit'>('create');
const formRef = ref<FormInstanceFunctions>();
const form = reactive<NotificationPayload>({
  title: '',
  summary: '',
  content: '',
  type: 'notification',
  priority: 'middle',
  status: 'draft',
  coverUrl: '',
  attachmentUrl: '',
  attachmentName: '',
});

const files = ref<UploadFile[]>([]);
const attachmentFiles = ref<UploadFile[]>([]);

const handleUploadSuccess = (context: SuccessContext) => {
  const response = (context?.response || {}) as any;
  if (response?.code !== undefined && response.code !== 0) {
    MessagePlugin.error(response?.message || '封面图片上传失败');
    return;
  }
  const url = response?.url || response?.data?.url;
  if (!url) {
    MessagePlugin.error('封面图片上传失败：未返回地址');
    return;
  }
  form.coverUrl = url;
  files.value = [{ url, name: '封面图片', status: 'success' }];
};

const handleRemove = () => {
  form.coverUrl = '';
  files.value = [];
};

const allowedAttachmentExtensions = new Set([
  'pdf',
  'doc',
  'docx',
  'xls',
  'xlsx',
  'csv',
  'ppt',
  'pptx',
  'png',
  'jpg',
  'jpeg',
  'gif',
  'webp',
]);

const beforeAttachmentUpload = (file: any) => {
  const rawFile: File | undefined = file?.raw || file;
  const name = String(rawFile?.name || file?.name || '');
  const size = Number(rawFile?.size || file?.size || 0);
  const ext = name.includes('.') ? name.split('.').pop()?.toLowerCase() : '';
  if (!ext || !allowedAttachmentExtensions.has(ext)) {
    MessagePlugin.error('附件格式不支持，请上传常见文档/图片/PDF/PPT/表格文件');
    return false;
  }
  const maxSize = 20 * 1024 * 1024;
  if (size > maxSize) {
    MessagePlugin.error('附件过大，请上传 20MB 以内文件');
    return false;
  }
  return true;
};

const handleAttachmentUploadSuccess = (context: SuccessContext) => {
  const { response, file } = context as any;
  if (response?.code !== undefined && response.code !== 0) {
    MessagePlugin.error(response?.message || '附件上传失败');
    return;
  }
  const url = response?.url || response?.data?.url || response?.data?.data?.url;
  if (!url) {
    MessagePlugin.error('附件上传失败：未返回地址');
    return;
  }
  const name = String(file?.name || '附件');
  form.attachmentUrl = url;
  form.attachmentName = name;
  attachmentFiles.value = [{ url, name, status: 'success' }];
};

const handleAttachmentRemove = () => {
  form.attachmentUrl = '';
  form.attachmentName = '';
  attachmentFiles.value = [];
};

const priorityOptions = [
  { label: '高', value: 'high' },
  { label: '中', value: 'middle' },
  { label: '低', value: 'low' },
];
const statusOptions = [
  { label: '草稿', value: 'draft' },
  { label: '已发布', value: 'published' },
  { label: '已撤回', value: 'withdrawn' },
];
const typeOptions = [{ label: '通知', value: 'notification' }];

const rules: Record<string, FormRule[]> = {
  title: [{ required: true, message: '请输入标题', type: 'error' }],
  content: [{ required: true, message: '请输入正文', type: 'error' }],
  type: [{ required: true, message: '请选择类型', type: 'error' }],
  priority: [{ required: true, message: '请选择优先级', type: 'error' }],
};

const columns: PrimaryTableCol[] = [
  { colKey: 'title', title: '标题', width: 260 },
  { colKey: 'priority', title: '优先级', cell: 'priority', width: 120 },
  { colKey: 'status', title: '状态', cell: 'status', width: 140 },
  { colKey: 'publishAt', title: '发布时间', width: 180 },
  { colKey: 'updatedAt', title: '最近更新', width: 180 },
  { colKey: 'op', title: '操作', cell: 'op', width: 240 },
];

const drawerTitle = computed(() => (formMode.value === 'create' ? '新增通知' : '编辑通知'));

const priorityTheme = (value?: string) => {
  switch ((value || '').toLowerCase()) {
    case 'high':
      return 'danger';
    case 'middle':
      return 'warning';
    default:
      return 'primary';
  }
};

const priorityLabel = (value?: string) => {
  switch ((value || '').toLowerCase()) {
    case 'high':
      return '高';
    case 'middle':
      return '中';
    case 'low':
      return '低';
    default:
      return '普通';
  }
};

const statusTheme = (value?: string) => {
  switch (value) {
    case 'published':
      return 'success';
    case 'withdrawn':
      return 'warning';
    default:
      return 'default';
  }
};

const statusLabel = (value?: string) => {
  switch (value) {
    case 'published':
      return '已发布';
    case 'withdrawn':
      return '已撤回';
    default:
      return '草稿';
  }
};

const load = async () => {
  loading.value = true;
  try {
    const res = await fetchNotifications({
      page: pagination.current - 1,
      size: pagination.pageSize,
      keyword: query.keyword,
      priority: query.priority,
      status: query.status,
    });
    list.value = res.list || [];
    pagination.total = res.total || 0;
  } finally {
    loading.value = false;
  }
};

const reset = () => {
  query.keyword = '';
  query.priority = '';
  query.status = '';
  pagination.current = 1;
  load();
};

const handlePageChange = ({ current, pageSize }: { current: number; pageSize: number }) => {
  pagination.current = current;
  pagination.pageSize = pageSize;
  load();
};

const openCreate = () => {
  formMode.value = 'create';
  Object.assign(form, {
    id: undefined,
    title: '',
    summary: '',
    content: '',
    type: 'notification',
    priority: 'middle',
    status: 'draft',
    coverUrl: '',
    attachmentUrl: '',
    attachmentName: '',
  });
  files.value = [];
  attachmentFiles.value = [];
  formVisible.value = true;
};

const openEdit = (row: NotificationItem) => {
  formMode.value = 'edit';
  Object.assign(form, {
    id: row.id,
    title: row.title,
    summary: row.summary || '',
    content: row.content,
    type: row.type || 'notification',
    priority: row.priority,
    status: row.status,
    coverUrl: row.coverUrl || '',
    attachmentUrl: row.attachmentUrl || '',
    attachmentName: row.attachmentName || '',
  });
  if (row.coverUrl) {
    files.value = [{ url: row.coverUrl, name: '封面图片', status: 'success' }];
  } else {
    files.value = [];
  }
  if (row.attachmentUrl) {
    const name = row.attachmentName || '附件';
    attachmentFiles.value = [{ url: row.attachmentUrl, name, status: 'success' }];
  } else {
    attachmentFiles.value = [];
  }
  formVisible.value = true;
};

const buildPayload = (): NotificationPayload => {
  return {
    title: String(form.title || '').trim(),
    summary: form.summary ? String(form.summary).trim() : undefined,
    content: form.content,
    type: form.type,
    priority: form.priority,
    status: form.status,
    coverUrl: form.coverUrl ? String(form.coverUrl).trim() : undefined,
    attachmentUrl: form.attachmentUrl ? String(form.attachmentUrl).trim() : undefined,
    attachmentName: form.attachmentName ? String(form.attachmentName).trim() : undefined,
  };
};

const onConfirm = () => {
  formRef.value?.submit();
};

const save = async (ctx: SubmitContext) => {
  if (ctx.validateResult !== true) return;
  saving.value = true;
  try {
    const payload = buildPayload();
    if (formMode.value === 'create') {
      await createNotification(payload);
      MessagePlugin.success('新增成功');
    } else {
      await updateNotification(form.id as number, payload);
      MessagePlugin.success('更新成功');
    }
    formVisible.value = false;
    await load();
  } catch (e: any) {
    const raw = String(e?.message || '保存失败');
    const humanMsg = raw.replace(/\s*\[\d{3}\]\s*$/, '').trim();
    MessagePlugin.error(humanMsg || '保存失败');
  } finally {
    saving.value = false;
  }
};

const togglePublish = async (row: NotificationItem) => {
  const publish = row.status !== 'published';
  await publishNotification(row.id, publish);
  MessagePlugin.success(publish ? '已发布' : '已撤回');
  load();
};

const handleDelete = async (id: number) => {
  await deleteNotification(id);
  MessagePlugin.success('已删除');
  load();
};

onMounted(load);
</script>
<style scoped lang="less">
.notification-table {
  :deep(.t-card__title) {
    font-weight: 600;
  }
}
</style>
