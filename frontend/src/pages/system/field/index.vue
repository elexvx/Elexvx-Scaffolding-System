<template>
  <t-card title="字段管理" :bordered="false" class="field-management">
    <div class="field-layout">
      <div class="field-layout__left">
        <div class="field-panel">
          <div class="panel-header panel-header--space">
            <span class="panel-title">数据库/表</span>
            <t-space size="small">
              <t-button size="small" variant="outline" @click="loadMetadata">刷新</t-button>
            </t-space>
          </div>
          <t-input v-model="treeKeyword" clearable placeholder="搜索表名" class="panel-search" />
          <t-tree
            class="field-tree"
            :data="filteredTree"
            :keys="{ value: 'value', label: 'label', children: 'children' }"
            activable
            :expand-all="true"
            v-model:actived="actived"
            @active="handleTreeActive"
          />
        </div>
      </div>
      <div class="field-layout__right">
        <div class="field-panel">
          <div class="panel-header panel-header--space">
            <div>
              <div class="panel-title">字段列表</div>
              <div class="panel-subtitle">当前表：{{ currentTableLabel }}</div>
            </div>
            <t-space>
              <t-button variant="outline" @click="loadMetadata">刷新</t-button>
            </t-space>
          </div>
          <t-table row-key="name" :data="currentFields" :columns="columns" bordered>
            <template #required="{ row }">
              <t-switch v-model="row.required" size="small" />
            </template>
            <template #primaryKey="{ row }">
              <t-switch v-model="row.primaryKey" size="small" />
            </template>
            <template #op="{ row }">
              <t-space>
                <t-link theme="primary" @click="openEdit(row)">编辑</t-link>
              </t-space>
            </template>
          </t-table>
        </div>
      </div>
    </div>

    <t-dialog
      v-model:visible="dialogVisible"
      :header="dialogTitle"
      width="560px"
      :confirm-btn="{ content: '保存', loading: saving }"
      @confirm="saveField"
      @close="dialogVisible = false"
    >
      <t-form ref="formRef" :data="form" layout="vertical">
        <t-row :gutter="[16, 16]">
          <t-col :xs="24" :sm="12">
            <t-form-item label="字段名" name="name">
              <t-input v-model="form.name" disabled placeholder="如：user_name" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="字段类型" name="type">
              <t-select v-model="form.type" :options="typeOptions" placeholder="请选择类型" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="长度" name="length">
              <t-input v-model="form.length" placeholder="如：255" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="默认值" name="defaultValue">
              <t-input v-model="form.defaultValue" placeholder="可选" />
            </t-form-item>
          </t-col>
          <t-col :xs="24">
            <t-form-item label="说明" name="description">
              <t-textarea v-model="form.description" :autosize="{ minRows: 2, maxRows: 4 }" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="8">
            <t-form-item label="必填">
              <t-switch v-model="form.required" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="8">
            <t-form-item label="主键">
              <t-switch v-model="form.primaryKey" />
            </t-form-item>
          </t-col>
        </t-row>
      </t-form>
    </t-dialog>

  </t-card>
</template>

<script setup lang="ts">
import type { PrimaryTableCol } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, reactive, ref } from 'vue';

import { request } from '@/utils/request';

interface TableNode {
  label: string;
  value: string;
  nodeType: 'db' | 'table';
  children?: TableNode[];
}

interface FieldItem {
  name: string;
  type: string;
  length: string;
  defaultValue: string;
  description: string;
  required: boolean;
  primaryKey: boolean;
}

interface TableMetadata {
  name: string;
  comment: string;
  columns: FieldItem[];
}

interface DatabaseMetadata {
  name: string;
  tables: TableMetadata[];
}

const tableTree = ref<TableNode[]>([]);
const fieldMap = reactive<Record<string, FieldItem[]>>({});

const actived = ref<Array<string | number>>([]);
const treeKeyword = ref('');
const currentTableKey = ref('');
const currentDbKey = ref('');

const columns: PrimaryTableCol[] = [
  { colKey: 'name', title: '字段名', width: 180 },
  { colKey: 'type', title: '类型', width: 120 },
  { colKey: 'length', title: '长度', width: 90 },
  { colKey: 'required', title: '必填', width: 80, cell: 'required' },
  { colKey: 'primaryKey', title: '主键', width: 80, cell: 'primaryKey' },
  { colKey: 'defaultValue', title: '默认值', width: 120 },
  { colKey: 'description', title: '说明', minWidth: 180, ellipsis: true },
  { colKey: 'op', title: '操作', width: 140, fixed: 'right' },
];

const currentFields = computed(() => (currentTableKey.value ? fieldMap[currentTableKey.value] || [] : []));

const currentTableLabel = computed(() => {
  const node = findTableByValue(currentTableKey.value);
  return node?.label || '请在左侧选择表';
});

const filteredTree = computed(() => {
  const keyword = treeKeyword.value.trim().toLowerCase();
  if (!keyword) return tableTree.value;
  const filterNodes = (nodes: TableNode[]) =>
    nodes.reduce<TableNode[]>((acc, node) => {
      const labelMatch = node.label.toLowerCase().includes(keyword);
      if (node.nodeType === 'db') {
        const children = filterNodes(node.children || []);
        if (labelMatch || children.length > 0) {
          acc.push({ ...node, children });
        }
      } else if (labelMatch) {
        acc.push({ ...node });
      }
      return acc;
    }, []);
  return filterNodes(tableTree.value);
});

const dialogVisible = ref(false);
const saving = ref(false);
const form = reactive<FieldItem>({
  name: '',
  type: 'varchar',
  length: '',
  defaultValue: '',
  description: '',
  required: false,
  primaryKey: false,
});

const typeOptions = [
  { label: 'varchar', value: 'varchar' },
  { label: 'int', value: 'int' },
  { label: 'integer', value: 'integer' },
  { label: 'tinyint', value: 'tinyint' },
  { label: 'bigint', value: 'bigint' },
  { label: 'decimal', value: 'decimal' },
  { label: 'float', value: 'float' },
  { label: 'double', value: 'double' },
  { label: 'boolean', value: 'boolean' },
  { label: 'date', value: 'date' },
  { label: 'datetime', value: 'datetime' },
  { label: 'timestamp', value: 'timestamp' },
  { label: 'text', value: 'text' },
];

const dialogTitle = computed(() => '编辑字段');

const findTableByValue = (value: string) => {
  if (!value) return null;
  for (const db of tableTree.value) {
    const target = db.children?.find((child) => child.value === value);
    if (target) return target;
  }
  return null;
};

const ensureDefaultSelection = () => {
  if (currentTableKey.value) return;
  const firstDb = tableTree.value[0];
  const firstTable = firstDb?.children?.[0];
  if (!firstTable) return;
  currentTableKey.value = firstTable.value;
  actived.value = [firstTable.value];
  currentDbKey.value = firstDb.value;
};

const handleTreeActive = (_value: Array<string | number>, context: { node?: { data?: TableNode } }) => {
  const data = context?.node?.data;
  if (!data) return;
  if (data.nodeType === 'table') {
    currentTableKey.value = data.value;
    currentDbKey.value = findDbByTableKey(data.value)?.value || '';
  } else {
    currentTableKey.value = '';
    currentDbKey.value = '';
  }
};

const openEdit = (row: FieldItem) => {
  Object.assign(form, { ...row });
  dialogVisible.value = true;
};

const saveField = async () => {
  if (!currentTableKey.value || !currentDbKey.value) return;
  if (!form.name.trim()) {
    MessagePlugin.warning('请输入字段名');
    return;
  }
  if (!form.type) {
    MessagePlugin.warning('请选择字段类型');
    return;
  }
  saving.value = true;
  try {
    await request.post({
      url: '/system/fields/columns/update',
      data: {
        database: currentDbKey.value,
        table: currentTableKey.value,
        name: form.name.trim(),
        type: form.type,
        length: form.length,
        defaultValue: form.defaultValue,
        description: form.description,
        required: form.required,
        primaryKey: form.primaryKey,
      },
    });
    dialogVisible.value = false;
    await loadMetadata();
    MessagePlugin.success('字段已更新');
  } finally {
    saving.value = false;
  }
};

const findDbByTableKey = (value: string) => {
  if (!value) return null;
  return tableTree.value.find((db) => db.children?.some((child) => child.value === value)) || null;
};

const loadMetadata = async () => {
  try {
    const response = await request.get<DatabaseMetadata>({ url: '/system/fields/metadata' });
    tableTree.value = [
      {
        label: response.name || '默认数据库',
        value: response.name || 'default',
        nodeType: 'db',
        children: response.tables.map((table) => ({
          label: table.comment ? `${table.comment} (${table.name})` : table.name,
          value: table.name,
          nodeType: 'table',
        })),
      },
    ];
    Object.keys(fieldMap).forEach((key) => {
      delete fieldMap[key];
    });
    response.tables.forEach((table) => {
      fieldMap[table.name] = table.columns.map((column) => ({
        ...column,
      }));
    });
    if (!currentTableKey.value || !fieldMap[currentTableKey.value]) {
      currentTableKey.value = '';
      actived.value = [];
    }
    ensureDefaultSelection();
  } catch (error) {
    console.error(error);
    MessagePlugin.error('获取数据库字段失败');
  }
};

loadMetadata();
</script>

<style lang="less" scoped>
.field-management {
  .field-layout {
    display: flex;
    gap: 16px;
    align-items: stretch;
  }

  .field-layout__left {
    flex: 0 0 320px;
    min-width: 280px;
  }

  .field-layout__right {
    flex: 1;
    min-width: 0;
  }

  .field-panel {
    border: 1px solid var(--td-component-border);
    border-radius: var(--td-radius-default);
    padding: 16px;
    background-color: var(--td-bg-color-container);
    min-height: 520px;
    display: flex;
    flex-direction: column;
  }

  .panel-header {
    display: flex;
    align-items: center;
    margin-bottom: 12px;

    &--space {
      justify-content: space-between;
    }
  }

  .panel-title {
    font-weight: 600;
    color: var(--td-text-color-primary);
  }

  .panel-subtitle {
    margin-top: 4px;
    color: var(--td-text-color-secondary);
    font-size: 12px;
  }

  .panel-search {
    margin-bottom: 12px;
  }

  .field-tree {
    flex: 1;
    min-height: 0;
    overflow: auto;
    padding-right: 8px;
  }
}
</style>
