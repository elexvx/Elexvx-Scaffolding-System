<template>
  <t-card title="字段管理" :bordered="false" class="field-management">
    <t-row :gutter="[16, 16]">
      <t-col :xs="24" :sm="8" :md="6">
        <div class="field-panel">
          <div class="panel-header panel-header--space">
            <span class="panel-title">数据库/表</span>
            <t-space size="small">
              <t-button size="small" variant="outline" @click="openDbDialog">新增库</t-button>
              <t-button size="small" variant="outline" @click="openTableDialog">新增表</t-button>
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
      </t-col>
      <t-col :xs="24" :sm="16" :md="18">
        <div class="field-panel">
          <div class="panel-header panel-header--space">
            <div>
              <div class="panel-title">字段列表</div>
              <div class="panel-subtitle">当前表：{{ currentTableLabel }}</div>
            </div>
            <t-space>
              <t-button theme="primary" :disabled="!currentTableKey" @click="openCreate">新增字段</t-button>
              <t-button variant="outline" :disabled="!currentTableKey" @click="openRemoveTable">删除表</t-button>
            </t-space>
          </div>
          <t-table
            row-key="name"
            :data="currentFields"
            :columns="columns"
            :pagination="pagination"
            bordered
            @page-change="handlePageChange"
          >
            <template #required="{ row }">
              <t-switch v-model="row.required" size="small" />
            </template>
            <template #primaryKey="{ row }">
              <t-switch v-model="row.primaryKey" size="small" />
            </template>
            <template #showInModule="{ row }">
              <t-switch v-model="row.showInModule" size="small" />
            </template>
            <template #op="{ row }">
              <t-space>
                <t-link theme="primary" @click="openEdit(row)">编辑</t-link>
                <t-link theme="danger" @click="handleDelete(row)">删除</t-link>
              </t-space>
            </template>
          </t-table>
        </div>
      </t-col>
    </t-row>

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
              <t-input v-model="form.name" :disabled="dialogMode === 'edit'" placeholder="如：user_name" />
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
          <t-col :xs="24" :sm="8">
            <t-form-item label="模块展示">
              <t-switch v-model="form.showInModule" />
            </t-form-item>
          </t-col>
        </t-row>
      </t-form>
    </t-dialog>

    <t-dialog
      v-model:visible="dbDialogVisible"
      header="新增数据库"
      width="520px"
      :confirm-btn="{ content: '保存' }"
      @confirm="saveDatabase"
      @close="dbDialogVisible = false"
    >
      <t-form :data="dbForm" layout="vertical">
        <t-form-item label="数据库名称">
          <t-input v-model="dbForm.label" placeholder="如：业务数据库" />
        </t-form-item>
        <t-form-item label="数据库标识">
          <t-input v-model="dbForm.value" placeholder="如：business_db" />
        </t-form-item>
      </t-form>
    </t-dialog>

    <t-dialog
      v-model:visible="tableDialogVisible"
      header="新增表"
      width="520px"
      :confirm-btn="{ content: '保存' }"
      @confirm="saveTable"
      @close="tableDialogVisible = false"
    >
      <t-form :data="tableForm" layout="vertical">
        <t-form-item label="所属数据库">
          <t-select v-model="tableForm.db" :options="dbOptions" placeholder="请选择数据库" />
        </t-form-item>
        <t-form-item label="表名称">
          <t-input v-model="tableForm.label" placeholder="如：订单表 (biz_order)" />
        </t-form-item>
        <t-form-item label="表标识">
          <t-input v-model="tableForm.value" placeholder="如：biz_order" />
        </t-form-item>
      </t-form>
    </t-dialog>
  </t-card>
</template>

<script setup lang="ts">
import type { PageInfo, PrimaryTableCol } from 'tdesign-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { computed, reactive, ref, watch } from 'vue';

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
  showInModule: boolean;
}

const defaultTableTree: TableNode[] = [
  {
    label: '核心业务库',
    value: 'core_db',
    nodeType: 'db',
    children: [
      { label: '用户表 (sys_user)', value: 'sys_user', nodeType: 'table' },
      { label: '角色表 (sys_role)', value: 'sys_role', nodeType: 'table' },
      { label: '菜单表 (sys_menu)', value: 'sys_menu', nodeType: 'table' },
    ],
  },
  {
    label: '订单数据库',
    value: 'order_db',
    nodeType: 'db',
    children: [
      { label: '订单表 (biz_order)', value: 'biz_order', nodeType: 'table' },
      { label: '订单明细 (biz_order_item)', value: 'biz_order_item', nodeType: 'table' },
    ],
  },
];

const cloneFields = (fields: FieldItem[]) => fields.map((item) => ({ ...item }));
const cloneTree = (nodes: TableNode[]) =>
  nodes.map((node) => ({
    ...node,
    children: node.children ? cloneTree(node.children) : undefined,
  }));

const tableTree = ref<TableNode[]>(cloneTree(defaultTableTree));

const defaultFieldMap: Record<string, FieldItem[]> = {
  sys_user: [
    {
      name: 'id',
      type: 'bigint',
      length: '20',
      defaultValue: '',
      description: '用户ID',
      required: true,
      primaryKey: true,
      showInModule: false,
    },
    {
      name: 'account',
      type: 'varchar',
      length: '64',
      defaultValue: '',
      description: '账号',
      required: true,
      primaryKey: false,
      showInModule: true,
    },
    {
      name: 'name',
      type: 'varchar',
      length: '64',
      defaultValue: '',
      description: '姓名',
      required: true,
      primaryKey: false,
      showInModule: true,
    },
  ],
  sys_role: [
    {
      name: 'id',
      type: 'bigint',
      length: '20',
      defaultValue: '',
      description: '角色ID',
      required: true,
      primaryKey: true,
      showInModule: false,
    },
    {
      name: 'name',
      type: 'varchar',
      length: '64',
      defaultValue: '',
      description: '角色名称',
      required: true,
      primaryKey: false,
      showInModule: true,
    },
  ],
  sys_menu: [
    {
      name: 'id',
      type: 'bigint',
      length: '20',
      defaultValue: '',
      description: '菜单ID',
      required: true,
      primaryKey: true,
      showInModule: false,
    },
    {
      name: 'title',
      type: 'varchar',
      length: '128',
      defaultValue: '',
      description: '菜单标题',
      required: true,
      primaryKey: false,
      showInModule: true,
    },
  ],
  biz_order: [
    {
      name: 'id',
      type: 'bigint',
      length: '20',
      defaultValue: '',
      description: '订单ID',
      required: true,
      primaryKey: true,
      showInModule: false,
    },
    {
      name: 'order_no',
      type: 'varchar',
      length: '32',
      defaultValue: '',
      description: '订单号',
      required: true,
      primaryKey: false,
      showInModule: true,
    },
    {
      name: 'status',
      type: 'varchar',
      length: '16',
      defaultValue: 'pending',
      description: '订单状态',
      required: true,
      primaryKey: false,
      showInModule: true,
    },
  ],
  biz_order_item: [
    {
      name: 'id',
      type: 'bigint',
      length: '20',
      defaultValue: '',
      description: '明细ID',
      required: true,
      primaryKey: true,
      showInModule: false,
    },
    {
      name: 'order_id',
      type: 'bigint',
      length: '20',
      defaultValue: '',
      description: '订单ID',
      required: true,
      primaryKey: false,
      showInModule: false,
    },
    {
      name: 'sku_name',
      type: 'varchar',
      length: '128',
      defaultValue: '',
      description: '商品名称',
      required: true,
      primaryKey: false,
      showInModule: true,
    },
  ],
};

const fieldMap = reactive<Record<string, FieldItem[]>>({});

const STORAGE_KEYS = {
  tree: 'field-management.table-tree',
  fields: 'field-management.field-map',
};

const loadPersistedState = () => {
  if (typeof window === 'undefined') {
    tableTree.value = cloneTree(defaultTableTree);
    Object.keys(fieldMap).forEach((key) => {
      delete fieldMap[key];
    });
    Object.entries(defaultFieldMap).forEach(([key, fields]) => {
      fieldMap[key] = cloneFields(fields);
    });
    return;
  }
  try {
    const rawTree = window.localStorage.getItem(STORAGE_KEYS.tree);
    const rawFields = window.localStorage.getItem(STORAGE_KEYS.fields);
    const parsedTree = rawTree ? (JSON.parse(rawTree) as TableNode[]) : cloneTree(defaultTableTree);
    const parsedFields = rawFields ? (JSON.parse(rawFields) as Record<string, FieldItem[]>) : defaultFieldMap;
    tableTree.value = Array.isArray(parsedTree) ? parsedTree : cloneTree(defaultTableTree);
    Object.keys(fieldMap).forEach((key) => {
      delete fieldMap[key];
    });
    Object.entries(parsedFields || {}).forEach(([key, fields]) => {
      fieldMap[key] = cloneFields(fields);
    });
  } catch {
    tableTree.value = cloneTree(defaultTableTree);
    Object.keys(fieldMap).forEach((key) => {
      delete fieldMap[key];
    });
    Object.entries(defaultFieldMap).forEach(([key, fields]) => {
      fieldMap[key] = cloneFields(fields);
    });
  }
};

const persistState = () => {
  if (typeof window === 'undefined') return;
  window.localStorage.setItem(STORAGE_KEYS.tree, JSON.stringify(tableTree.value));
  window.localStorage.setItem(STORAGE_KEYS.fields, JSON.stringify(fieldMap));
};

const actived = ref<Array<string | number>>([]);
const treeKeyword = ref('');
const currentTableKey = ref('');

const columns: PrimaryTableCol[] = [
  { colKey: 'name', title: '字段名', width: 180 },
  { colKey: 'type', title: '类型', width: 120 },
  { colKey: 'length', title: '长度', width: 90 },
  { colKey: 'required', title: '必填', width: 80, cell: 'required' },
  { colKey: 'primaryKey', title: '主键', width: 80, cell: 'primaryKey' },
  { colKey: 'showInModule', title: '模块展示', width: 110, cell: 'showInModule' },
  { colKey: 'defaultValue', title: '默认值', width: 120 },
  { colKey: 'description', title: '说明', minWidth: 180, ellipsis: true },
  { colKey: 'op', title: '操作', width: 140, fixed: 'right' },
];

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
});

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

const dbOptions = computed(() =>
  tableTree.value.map((db) => ({
    label: db.label,
    value: db.value,
  })),
);

const dialogVisible = ref(false);
const dialogMode = ref<'create' | 'edit'>('create');
const editingFieldName = ref('');
const saving = ref(false);
const form = reactive<FieldItem>({
  name: '',
  type: 'varchar',
  length: '',
  defaultValue: '',
  description: '',
  required: false,
  primaryKey: false,
  showInModule: true,
});

const typeOptions = [
  { label: 'varchar', value: 'varchar' },
  { label: 'int', value: 'int' },
  { label: 'bigint', value: 'bigint' },
  { label: 'decimal', value: 'decimal' },
  { label: 'datetime', value: 'datetime' },
  { label: 'text', value: 'text' },
];

const dialogTitle = computed(() => (dialogMode.value === 'create' ? '新增字段' : '编辑字段'));

const dbDialogVisible = ref(false);
const tableDialogVisible = ref(false);
const dbForm = reactive({
  label: '',
  value: '',
});
const tableForm = reactive({
  db: '',
  label: '',
  value: '',
});

const handlePageChange = (pageInfo: PageInfo) => {
  pagination.current = pageInfo.current;
  pagination.pageSize = pageInfo.pageSize;
};

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
  if (!fieldMap[firstTable.value]) {
    fieldMap[firstTable.value] = [];
  }
};

const handleTreeActive = (_value: Array<string | number>, context: { node?: { data?: TableNode } }) => {
  const data = context?.node?.data;
  if (!data) return;
  if (data.nodeType === 'table') {
    currentTableKey.value = data.value;
    if (!fieldMap[data.value]) {
      fieldMap[data.value] = [];
      persistState();
    }
  } else {
    currentTableKey.value = '';
  }
};

const openDbDialog = () => {
  dbForm.label = '';
  dbForm.value = '';
  dbDialogVisible.value = true;
};

const openTableDialog = () => {
  tableForm.label = '';
  tableForm.value = '';
  tableForm.db = currentTableKey.value ? findDbByTableKey(currentTableKey.value)?.value || '' : '';
  tableDialogVisible.value = true;
};

const saveDatabase = () => {
  const label = dbForm.label.trim();
  const value = dbForm.value.trim();
  if (!label || !value) {
    MessagePlugin.warning('请输入数据库名称与标识');
    return;
  }
  const exists = tableTree.value.some((item) => item.value === value);
  if (exists) {
    MessagePlugin.error('数据库标识已存在');
    return;
  }
  tableTree.value.push({
    label,
    value,
    nodeType: 'db',
    children: [],
  });
  dbDialogVisible.value = false;
  persistState();
};

const saveTable = () => {
  const db = tableTree.value.find((item) => item.value === tableForm.db);
  if (!db) {
    MessagePlugin.warning('请选择所属数据库');
    return;
  }
  const label = tableForm.label.trim();
  const value = tableForm.value.trim();
  if (!label || !value) {
    MessagePlugin.warning('请输入表名称与标识');
    return;
  }
  const exists = tableTree.value.some((item) => item.children?.some((child) => child.value === value));
  if (exists) {
    MessagePlugin.error('表标识已存在');
    return;
  }
  db.children = db.children || [];
  db.children.push({ label, value, nodeType: 'table' });
  if (!fieldMap[value]) {
    fieldMap[value] = [];
  }
  currentTableKey.value = value;
  actived.value = [value];
  tableDialogVisible.value = false;
  persistState();
};

const openCreate = () => {
  if (!currentTableKey.value) return;
  dialogMode.value = 'create';
  editingFieldName.value = '';
  Object.assign(form, {
    name: '',
    type: 'varchar',
    length: '',
    defaultValue: '',
    description: '',
    required: false,
    primaryKey: false,
    showInModule: true,
  });
  dialogVisible.value = true;
};

const openEdit = (row: FieldItem) => {
  dialogMode.value = 'edit';
  editingFieldName.value = row.name;
  Object.assign(form, { ...row });
  dialogVisible.value = true;
};

const openRemoveTable = () => {
  const current = currentTableKey.value;
  if (!current) return;
  const node = findTableByValue(current);
  if (!node) return;
  const confirm = DialogPlugin.confirm({
    header: '确认删除表',
    body: `确认删除表「${node.label}」及其字段吗？`,
    confirmBtn: '删除',
    cancelBtn: '取消',
    onConfirm: () => {
      confirm.hide();
      removeTableByValue(current);
    },
  });
};

const saveField = async () => {
  if (!currentTableKey.value) return;
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
    const list = fieldMap[currentTableKey.value] || [];
    if (dialogMode.value === 'create') {
      const exists = list.some((item) => item.name === form.name.trim());
      if (exists) {
        MessagePlugin.error('字段名已存在');
        return;
      }
      list.push({ ...form, name: form.name.trim() });
    } else {
      const target = list.find((item) => item.name === editingFieldName.value);
      if (target) {
        Object.assign(target, { ...form });
      }
    }
    dialogVisible.value = false;
    pagination.total = list.length;
    persistState();
    MessagePlugin.success('保存成功');
  } finally {
    saving.value = false;
  }
};

const handleDelete = (row: FieldItem) => {
  const confirm = DialogPlugin.confirm({
    header: '确认删除',
    body: `确认删除字段「${row.name}」吗？`,
    confirmBtn: '删除',
    cancelBtn: '取消',
    onConfirm: () => {
      confirm.hide();
      const list = fieldMap[currentTableKey.value] || [];
      const index = list.findIndex((item) => item.name === row.name);
      if (index >= 0) {
        list.splice(index, 1);
        pagination.total = list.length;
        persistState();
        MessagePlugin.success('已删除');
      }
    },
  });
};

const findDbByTableKey = (value: string) => {
  if (!value) return null;
  return tableTree.value.find((db) => db.children?.some((child) => child.value === value)) || null;
};

const removeTableByValue = (value: string) => {
  for (const db of tableTree.value) {
    const index = db.children?.findIndex((child) => child.value === value) ?? -1;
    if (index >= 0 && db.children) {
      db.children.splice(index, 1);
      delete fieldMap[value];
      if (currentTableKey.value === value) {
        currentTableKey.value = '';
        actived.value = [];
      }
      persistState();
      MessagePlugin.success('已删除');
      return;
    }
  }
};

watch(
  currentFields,
  (list) => {
    pagination.total = list.length;
    const maxPage = Math.max(1, Math.ceil(list.length / pagination.pageSize));
    if (pagination.current > maxPage) {
      pagination.current = 1;
    }
  },
  { immediate: true, deep: true },
);

watch(
  tableTree,
  () => {
    persistState();
  },
  { deep: true },
);

watch(
  fieldMap,
  () => {
    persistState();
  },
  { deep: true },
);

loadPersistedState();
ensureDefaultSelection();
</script>

<style lang="less" scoped>
.field-management {
  .field-panel {
    border: 1px solid var(--td-component-border);
    border-radius: var(--td-radius-default);
    padding: 16px;
    background-color: var(--td-bg-color-container);
    min-height: 520px;
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
    max-height: 520px;
    overflow: auto;
    padding-right: 8px;
  }
}
</style>
