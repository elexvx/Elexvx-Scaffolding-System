<template>
  <div class="dict-page">
    <t-card title="字典管理" :bordered="false">
      <div class="table-action-bar">
        <div class="action-left">
          <t-input v-model="filters.keyword" clearable placeholder="关键字" style="width: 200px" />
          <t-input v-model="filters.name" clearable placeholder="字典名称" style="width: 200px" />
          <t-select v-model="filters.status" clearable placeholder="状态" style="width: 160px" :options="statusOptions" />
          <t-button theme="primary" @click="loadDicts">查询</t-button>
          <t-button variant="outline" @click="resetFilters">重置</t-button>
        </div>
        <div class="action-right">
          <t-button theme="primary" @click="openCreate">添加</t-button>
        </div>
      </div>

      <t-table
        row-key="id"
        :data="dictList"
        :columns="dictColumns"
        :pagination="pagination"
        :loading="loading"
        @page-change="onPageChange"
      >
        <template #status="{ row }">
          <t-switch :value="row.status === 1" @change="(val) => toggleStatus(row, Boolean(val))" />
        </template>
        <template #op="{ row }">
          <t-space>
            <t-link theme="primary" @click="openEdit(row)">编辑</t-link>
            <t-link theme="danger" @click="removeDict(row)">删除</t-link>
            <t-link theme="primary" @click="openConfig(row)">字典配置</t-link>
          </t-space>
        </template>
      </t-table>
    </t-card>

    <t-dialog v-model:visible="dictDialogVisible" :header="dictDialogTitle" width="520px" :close-on-overlay-click="false">
      <t-form ref="dictFormRef" :data="dictForm" :rules="dictRules" label-width="90px" layout="vertical">
        <t-form-item label="字典名称" name="name">
          <t-input v-model="dictForm.name" placeholder="如：性别" />
        </t-form-item>
        <t-form-item label="字典编码" name="code">
          <t-input v-model="dictForm.code" placeholder="如：gender" />
        </t-form-item>
        <t-form-item label="状态" name="status">
          <t-radio-group v-model="dictForm.status">
            <t-radio :value="1">启用</t-radio>
            <t-radio :value="0">禁用</t-radio>
          </t-radio-group>
        </t-form-item>
        <t-form-item label="排序" name="sort">
          <t-input-number v-model="dictForm.sort" :min="0" style="width: 160px" />
        </t-form-item>
        <t-form-item label="备注" name="remark">
          <t-textarea v-model="dictForm.remark" placeholder="补充说明" :autosize="{ minRows: 2, maxRows: 4 }" />
        </t-form-item>
      </t-form>
      <template #footer>
        <t-space>
          <t-button variant="outline" @click="dictDialogVisible = false">取消</t-button>
          <t-button theme="primary" :loading="dictSaving" @click="submitDict">保存</t-button>
        </t-space>
      </template>
    </t-dialog>

    <t-dialog
      v-model:visible="configDialogVisible"
      :header="configDialogTitle"
      width="1000px"
      :close-on-overlay-click="false"
    >
      <div class="config-toolbar">
        <div class="config-toolbar__left">
          <t-input v-model="itemFilters.keyword" clearable placeholder="名称/数据值" style="width: 200px" />
          <t-select v-model="itemFilters.status" clearable placeholder="状态" style="width: 160px" :options="statusOptions" />
          <t-button theme="primary" @click="loadItems">查询</t-button>
          <t-button variant="outline" @click="resetItemFilters">重置</t-button>
        </div>
        <div class="config-toolbar__right">
          <t-space>
            <t-button theme="primary" @click="openItemCreate">添加</t-button>
            <t-button variant="outline" @click="downloadTemplate">
              <template #icon><t-icon name="download" /></template>
              下载模板
            </t-button>
            <t-upload
              v-model="importFiles"
              :action="uploadAction"
              :headers="uploadHeaders"
              theme="file"
              :auto-upload="true"
              :use-mock-progress="true"
              :mock-progress-duration="80"
              :max="1"
              accept=".xlsx,.xls,.csv,.txt"
              @success="handleImportSuccess"
              @fail="handleImportFail"
            >
              <t-button variant="outline">
                <template #icon><t-icon name="upload" /></template>
                批量导入
              </t-button>
            </t-upload>
            <t-button variant="outline" @click="exportItems">
              <template #icon><t-icon name="download" /></template>
              批量导出
            </t-button>
          </t-space>
        </div>
      </div>

      <t-table
        row-key="id"
        :data="itemList"
        :columns="itemColumns"
        :pagination="itemPagination"
        :loading="itemLoading"
        @page-change="onItemPageChange"
      >
        <template #valueType="{ row }">
          <t-tag variant="light">{{ valueTypeLabelMap[row.valueType] || row.valueType || 'string' }}</t-tag>
        </template>
        <template #status="{ row }">
          <t-tag :theme="row.status === 1 ? 'success' : 'default'">{{ row.status === 1 ? '启用' : '禁用' }}</t-tag>
        </template>
        <template #tagColor="{ row }">
          <t-tag v-if="row.tagColor" :theme="row.tagColor" variant="light">{{ row.tagColor }}</t-tag>
          <span v-else>-</span>
        </template>
        <template #op="{ row }">
          <t-space>
            <t-link theme="primary" @click="openItemEdit(row)">编辑</t-link>
            <t-link theme="danger" @click="removeItem(row)">删除</t-link>
          </t-space>
        </template>
      </t-table>
    </t-dialog>

    <t-dialog v-model:visible="itemDialogVisible" :header="itemDialogTitle" width="520px" :close-on-overlay-click="false">
      <t-form ref="itemFormRef" :data="itemForm" :rules="itemRules" label-width="90px" layout="vertical">
        <t-form-item label="名称" name="label">
          <t-input v-model="itemForm.label" placeholder="如：男" />
        </t-form-item>
        <t-form-item label="数据值" name="value">
          <t-input v-model="itemForm.value" placeholder="如：male" />
        </t-form-item>
        <t-form-item label="数据值类型" name="valueType">
          <t-select v-model="itemForm.valueType" :options="valueTypeOptions" placeholder="选择数据值类型" />
        </t-form-item>
        <t-form-item label="状态" name="status">
          <t-radio-group v-model="itemForm.status">
            <t-radio :value="1">启用</t-radio>
            <t-radio :value="0">禁用</t-radio>
          </t-radio-group>
        </t-form-item>
        <t-form-item label="排序" name="sort">
          <t-input-number v-model="itemForm.sort" :min="0" style="width: 160px" />
        </t-form-item>
        <t-form-item label="标签颜色" name="tagColor">
          <t-select v-model="itemForm.tagColor" :options="tagColorOptions" clearable placeholder="可选" />
        </t-form-item>
      </t-form>
      <template #footer>
        <t-space>
          <t-button variant="outline" @click="itemDialogVisible = false">取消</t-button>
          <t-button theme="primary" :loading="itemSaving" @click="submitItem">保存</t-button>
        </t-space>
      </template>
    </t-dialog>
  </div>
</template>

<script setup lang="ts">
import type { FormInstanceFunctions, FormRule, PageInfo, PrimaryTableCol, UploadFile } from 'tdesign-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted, reactive, ref } from 'vue';

import type { DictionaryImportResult, SysDict, SysDictItem } from '@/api/system/dictionary';
import {
  createDict,
  createDictItem,
  deleteDict,
  deleteDictItem,
  downloadDictTemplate,
  exportDictItems,
  fetchDictItems,
  fetchDictPage,
  updateDict,
  updateDictItem,
} from '@/api/system/dictionary';
import { useDictionaryStore, useUserStore } from '@/store';

const dictStore = useDictionaryStore();
const userStore = useUserStore();

const loading = ref(false);
const dictSaving = ref(false);
const dictDialogVisible = ref(false);
const dictDialogMode = ref<'create' | 'edit'>('create');
const dictFormRef = ref<FormInstanceFunctions>();
const dictList = ref<SysDict[]>([]);
const editingDictId = ref<number | null>(null);

const filters = reactive({
  keyword: '',
  name: '',
  status: null as number | null,
});

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
});

const dictForm = reactive({
  name: '',
  code: '',
  status: 1,
  sort: 0,
  remark: '',
});

const dictRules: Record<string, FormRule[]> = {
  name: [{ required: true, message: '请输入字典名称', type: 'error' }],
  code: [{ required: true, message: '请输入字典编码', type: 'error' }],
};

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
];

const dictColumns: PrimaryTableCol[] = [
  { colKey: 'id', title: '序号', width: 80 },
  { colKey: 'name', title: '字典名称', minWidth: 180 },
  { colKey: 'code', title: '字典编码', minWidth: 200 },
  { colKey: 'status', title: '状态', width: 120 },
  { colKey: 'sort', title: '排序', width: 100 },
  { colKey: 'op', title: '操作', width: 220, fixed: 'right' },
];

const dictDialogTitle = computed(() => (dictDialogMode.value === 'create' ? '新增字典' : '编辑字典'));

const configDialogVisible = ref(false);
const selectedDict = ref<SysDict | null>(null);
const itemLoading = ref(false);
const itemSaving = ref(false);
const itemDialogVisible = ref(false);
const itemDialogMode = ref<'create' | 'edit'>('create');
const itemFormRef = ref<FormInstanceFunctions>();
const itemList = ref<SysDictItem[]>([]);
const editingItemId = ref<number | null>(null);

const itemFilters = reactive({
  keyword: '',
  status: null as number | null,
});

const itemPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
});

const itemForm = reactive({
  label: '',
  value: '',
  valueType: 'string',
  status: 1,
  sort: 0,
  tagColor: '' as string | '',
});

const itemRules: Record<string, FormRule[]> = {
  label: [{ required: true, message: '请输入名称', type: 'error' }],
  value: [{ required: true, message: '请输入数据值', type: 'error' }],
};

const valueTypeOptions = [
  { label: '字符串', value: 'string' },
  { label: '数字', value: 'number' },
  { label: '布尔', value: 'boolean' },
];

const valueTypeLabelMap: Record<string, string> = {
  string: '字符串',
  number: '数字',
  boolean: '布尔',
};

const tagColorOptions = [
  { label: 'success', value: 'success' },
  { label: 'primary', value: 'primary' },
  { label: 'warning', value: 'warning' },
  { label: 'danger', value: 'danger' },
  { label: 'default', value: 'default' },
];

const itemColumns: PrimaryTableCol[] = [
  { colKey: 'label', title: '名称', minWidth: 120 },
  { colKey: 'valueType', title: '数据值类型', width: 120 },
  { colKey: 'value', title: '数据值', minWidth: 120 },
  { colKey: 'status', title: '状态', width: 100 },
  { colKey: 'sort', title: '排序', width: 100 },
  { colKey: 'tagColor', title: '标签颜色', width: 120 },
  { colKey: 'op', title: '操作', width: 140, fixed: 'right' },
];

const configDialogTitle = computed(() => (selectedDict.value ? `${selectedDict.value.name} - 字典配置` : '字典配置'));
const itemDialogTitle = computed(() => (itemDialogMode.value === 'create' ? '新增字段' : '编辑字段'));

const uploadHeaders = computed(() => ({ Authorization: userStore.token }));
const uploadAction = computed(() =>
  selectedDict.value ? `/api/system/dict/${selectedDict.value.id}/items/import` : '/api/system/dict/0/items/import',
);
const importFiles = ref<UploadFile[]>([]);

const loadDicts = async () => {
  loading.value = true;
  try {
    const data = await fetchDictPage({
      keyword: filters.keyword || filters.name || undefined,
      status: filters.status ?? undefined,
      page: pagination.current - 1,
      size: pagination.pageSize,
    });
    dictList.value = data.list || [];
    pagination.total = data.total || 0;
  } finally {
    loading.value = false;
  }
};

const resetFilters = () => {
  filters.keyword = '';
  filters.name = '';
  filters.status = null;
  pagination.current = 1;
  loadDicts();
};

const onPageChange = (pageInfo: PageInfo) => {
  pagination.current = pageInfo.current;
  pagination.pageSize = pageInfo.pageSize;
  loadDicts();
};

const openCreate = () => {
  dictDialogMode.value = 'create';
  editingDictId.value = null;
  dictForm.name = '';
  dictForm.code = '';
  dictForm.status = 1;
  dictForm.sort = 0;
  dictForm.remark = '';
  dictDialogVisible.value = true;
};

const openEdit = (row: SysDict) => {
  dictDialogMode.value = 'edit';
  editingDictId.value = row.id;
  dictForm.name = row.name || '';
  dictForm.code = row.code || '';
  dictForm.status = row.status ?? 1;
  dictForm.sort = row.sort ?? 0;
  dictForm.remark = row.remark || '';
  dictDialogVisible.value = true;
};

const submitDict = async () => {
  const valid = await dictFormRef.value?.validate();
  if (valid !== true) return;
  dictSaving.value = true;
  try {
    const payload = {
      name: dictForm.name,
      code: dictForm.code,
      status: dictForm.status,
      sort: dictForm.sort,
      remark: dictForm.remark || undefined,
    };
    if (dictDialogMode.value === 'create') {
      await createDict(payload);
      MessagePlugin.success('新增成功');
    } else if (editingDictId.value) {
      await updateDict(editingDictId.value, payload);
      MessagePlugin.success('保存成功');
    }
    dictDialogVisible.value = false;
    loadDicts();
  } finally {
    dictSaving.value = false;
  }
};

const removeDict = (row: SysDict) => {
  const dialog = DialogPlugin.confirm({
    header: '删除字典',
    body: `确认删除字典「${row.name}」？`,
    theme: 'danger',
    confirmBtn: '删除',
    onConfirm: async () => {
      await deleteDict(row.id);
      MessagePlugin.success('已删除');
      dialog.hide();
      loadDicts();
    },
  });
};

const toggleStatus = async (row: SysDict, enabled: boolean) => {
  await updateDict(row.id, { status: enabled ? 1 : 0 });
  row.status = enabled ? 1 : 0;
};

const openConfig = (row: SysDict) => {
  selectedDict.value = row;
  itemFilters.keyword = '';
  itemFilters.status = null;
  itemPagination.current = 1;
  configDialogVisible.value = true;
  loadItems();
};

const loadItems = async () => {
  if (!selectedDict.value) return;
  itemLoading.value = true;
  try {
    const data = await fetchDictItems(selectedDict.value.id, {
      keyword: itemFilters.keyword || undefined,
      status: itemFilters.status ?? undefined,
      page: itemPagination.current - 1,
      size: itemPagination.pageSize,
    });
    itemList.value = data.list || [];
    itemPagination.total = data.total || 0;
  } finally {
    itemLoading.value = false;
  }
};

const resetItemFilters = () => {
  itemFilters.keyword = '';
  itemFilters.status = null;
  itemPagination.current = 1;
  loadItems();
};

const onItemPageChange = (pageInfo: PageInfo) => {
  itemPagination.current = pageInfo.current;
  itemPagination.pageSize = pageInfo.pageSize;
  loadItems();
};

const openItemCreate = () => {
  itemDialogMode.value = 'create';
  editingItemId.value = null;
  itemForm.label = '';
  itemForm.value = '';
  itemForm.valueType = 'string';
  itemForm.status = 1;
  itemForm.sort = 0;
  itemForm.tagColor = '';
  itemDialogVisible.value = true;
};

const openItemEdit = (row: SysDictItem) => {
  itemDialogMode.value = 'edit';
  editingItemId.value = row.id;
  itemForm.label = row.label;
  itemForm.value = row.value;
  itemForm.valueType = row.valueType || 'string';
  itemForm.status = row.status ?? 1;
  itemForm.sort = row.sort ?? 0;
  itemForm.tagColor = row.tagColor || '';
  itemDialogVisible.value = true;
};

const submitItem = async () => {
  if (!selectedDict.value) return;
  const valid = await itemFormRef.value?.validate();
  if (valid !== true) return;
  itemSaving.value = true;
  try {
    const payload = {
      label: itemForm.label,
      value: itemForm.value,
      valueType: itemForm.valueType,
      status: itemForm.status,
      sort: itemForm.sort,
      tagColor: itemForm.tagColor || undefined,
    };
    if (itemDialogMode.value === 'create') {
      await createDictItem(selectedDict.value.id, payload);
      MessagePlugin.success('新增成功');
    } else if (editingItemId.value) {
      await updateDictItem(editingItemId.value, payload);
      MessagePlugin.success('保存成功');
    }
    dictStore.clearDictCache(selectedDict.value.code);
    itemDialogVisible.value = false;
    loadItems();
  } finally {
    itemSaving.value = false;
  }
};

const removeItem = (row: SysDictItem) => {
  const dialog = DialogPlugin.confirm({
    header: '删除字段',
    body: `确认删除字段「${row.label}」？`,
    theme: 'danger',
    confirmBtn: '删除',
    onConfirm: async () => {
      await deleteDictItem(row.id);
      MessagePlugin.success('已删除');
      dialog.hide();
      if (selectedDict.value) dictStore.clearDictCache(selectedDict.value.code);
      loadItems();
    },
  });
};

const handleImportSuccess = (context: { response: any }) => {
  const response = context?.response || {};
  const result: DictionaryImportResult | undefined = response?.data || response?.result || response?.data?.data;
  if (response?.code !== undefined && response.code !== 0) {
    MessagePlugin.error(response?.message || '导入失败');
    importFiles.value = [];
    return;
  }
  if (result) {
    MessagePlugin.success(`导入完成：新增${result.imported}条，更新${result.updated}条，失败${result.failed}条`);
    if (result.errors && result.errors.length > 0) {
      DialogPlugin.alert({ header: '导入异常提示', body: result.errors.join('\n') });
    }
  } else {
    MessagePlugin.success('导入完成');
  }
  importFiles.value = [];
  if (selectedDict.value) dictStore.clearDictCache(selectedDict.value.code);
  loadItems();
};

const handleImportFail = () => {
  MessagePlugin.error('导入失败');
  importFiles.value = [];
};

const exportItems = async () => {
  if (!selectedDict.value) return;
  const response = await exportDictItems(selectedDict.value.id);
  const blob = new Blob([response.data]);
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  const disposition = response.headers?.['content-disposition'] as string;
  let filename = `dict_${selectedDict.value.code}_items.csv`;
  if (disposition && disposition.includes('filename=')) {
    filename = decodeURIComponent(disposition.split('filename=')[1]);
  }
  link.href = url;
  link.download = filename;
  link.click();
  URL.revokeObjectURL(url);
};

const downloadTemplate = async () => {
  const response = await downloadDictTemplate();
  const blob = new Blob([response.data]);
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = 'dict_items_template.csv';
  link.click();
  URL.revokeObjectURL(url);
};

onMounted(() => {
  loadDicts();
});
</script>

<style scoped lang="less">
.dict-page {
  .table-action-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    flex-wrap: wrap;
    gap: 12px;

    .action-left {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 12px;
    }
  }

  .config-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    flex-wrap: wrap;
    gap: 12px;

    &__left,
    &__right {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 12px;
    }
  }
}
</style>
