<template>
  <div class="org-management">
    <t-card title="机构管理" :bordered="false">
      <t-space class="org-toolbar" style="flex-wrap: wrap; margin-bottom: 24px" size="24px">
        <t-input
          v-model="filters.keyword"
          clearable
          placeholder="请输入机构名称"
          style="width: 240px"
          @enter="reload"
        />
        <t-select
          v-model="filters.status"
          :options="statusOptions"
          clearable
          placeholder="机构状态"
          style="width: 160px"
        />
        <t-button theme="primary" @click="reload">搜索</t-button>
        <t-button variant="outline" @click="resetFilters">重置</t-button>
        <t-button theme="primary" @click="() => openCreate()">新增</t-button>
        <t-button variant="outline" @click="toggleExpand">{{ expandAll ? '收起全部' : '展开全部' }}</t-button>
        <t-button theme="primary" :disabled="!dirty" :loading="savingOrder" @click="saveOrder">保存排序</t-button>
      </t-space>

      <t-enhanced-table
        ref="tableRef"
        v-model:expanded-tree-nodes="expandedTreeNodes"
        row-key="id"
        drag-sort="row-handler"
        :data="filteredTree"
        :columns="columns"
        :loading="loading"
        :tree="treeConfig"
        :before-drag-sort="beforeDragSort"
        @abnormal-drag-sort="onAbnormalDragSort"
        @drag-sort="onDragSort"
      >
        <template #leaderNames="{ row }">
          <span>{{ formatList(row.leaderNames) }}</span>
        </template>
        <template #status="{ row }">
          <t-tag :theme="row.status === 1 ? 'success' : 'danger'" variant="light">
            {{ row.status === 1 ? '正常' : '停用' }}
          </t-tag>
        </template>
        <template #createdAt="{ row }">
          <span>{{ formatTime(row.createdAt) }}</span>
        </template>
        <template #op="{ row }">
          <t-space class="org-table-actions">
            <t-link theme="primary" @click="openCreate(row)">新增</t-link>
            <t-link theme="primary" @click="openEdit(row)">编辑</t-link>
            <t-link theme="danger" @click="removeRow(row)">删除</t-link>
          </t-space>
        </template>
      </t-enhanced-table>
    </t-card>

    <confirm-drawer v-model:visible="dialogVisible" :header="dialogTitle" size="760px">
      <t-form
        ref="formRef"
        class="drawer-form--single"
        :data="form"
        :rules="rules"
        label-width="120px"
        layout="vertical"
        label-align="right"
        @submit="onSubmit"
      >
        <t-row :gutter="[24, 24]">
          <t-col :xs="24">
            <t-form-item label="上级机构" name="parentId">
              <t-tree-select
                v-model="form.parentId"
                :data="orgTree"
                :keys="orgTreeKeys"
                clearable
                placeholder="选择上级机构"
                style="width: 100%"
              />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="机构名称" name="name">
              <t-input v-model="form.name" placeholder="请输入机构名称" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="机构简称" name="shortName">
              <t-input v-model="form.shortName" placeholder="请输入机构简称" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="机构类型" name="type">
              <t-select v-model="form.type" :options="typeOptions" placeholder="请选择机构类型" />
            </t-form-item>
          </t-col>
          <t-col :xs="24">
            <t-form-item label="负责人" name="leaderIds">
              <t-input v-model="leaderDisplay" readonly placeholder="请选择部门领导" @click="openLeaderDialog">
                <template #suffixIcon>
                  <t-icon name="user" />
                </template>
              </t-input>
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="联系电话" name="phone">
              <t-input v-model="form.phone" placeholder="请输入联系电话" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="邮箱" name="email">
              <t-input v-model="form.email" placeholder="请输入邮箱" />
            </t-form-item>
          </t-col>
          <t-col :xs="24">
            <t-form-item label="机构状态" name="status">
              <t-radio-group v-model="form.status">
                <t-radio :value="1">正常</t-radio>
                <t-radio :value="0">停用</t-radio>
              </t-radio-group>
            </t-form-item>
          </t-col>
        </t-row>
      </t-form>
      <template #footer>
        <t-space class="tdesign-starter-action-bar">
          <t-button variant="outline" @click="dialogVisible = false">取消</t-button>
          <t-button theme="primary" :loading="saving" @click="submitForm">确定</t-button>
        </t-space>
      </template>
    </confirm-drawer>

    <t-dialog v-model:visible="leaderDialogVisible" width="920px" header="用户选择" :footer="false">
      <div class="leader-dialog">
        <div class="leader-dialog__search">
          <t-input
            v-model="leaderFilters.keyword"
            clearable
            placeholder="请输入用户姓名"
            class="leader-dialog__keyword"
          />
          <t-space class="leader-dialog__actions" size="small">
            <t-button theme="primary" @click="loadLeaders">搜索</t-button>
            <t-button variant="outline" @click="resetLeaderFilters">重置</t-button>
          </t-space>
        </div>
        <div class="leader-dialog__content">
          <div class="leader-panel">
            <div class="leader-panel__title">组织机构</div>
            <div class="leader-panel__body">
              <t-input
                v-model="leaderFilters.orgKeyword"
                clearable
                placeholder="请输入组织名称"
                class="leader-panel__filter"
              />
              <t-tree
                class="leader-tree"
                :data="leaderFilteredTree"
                :keys="orgTreeKeys"
                hover
                activable
                @click="handleLeaderOrgSelect"
              />
            </div>
          </div>
          <div class="leader-panel">
            <div class="leader-panel__title">用户列表</div>
            <div class="leader-panel__body">
              <t-table
                row-key="id"
                :data="leaderRows"
                :columns="leaderColumns"
                :pagination="leaderPagination"
                :selected-row-keys="leaderSelection.map((u) => u.id)"
                @select-change="handleLeaderSelectChange"
                @page-change="onLeaderPageChange"
              />
            </div>
          </div>
          <div class="leader-panel">
            <div class="leader-panel__title">已选择用户 ({{ leaderSelection.length }}人)</div>
            <div class="leader-panel__body leader-selected">
              <t-tag
                v-for="user in leaderSelection"
                :key="user.id"
                theme="primary"
                variant="light"
                closable
                @close="removeLeader(user.id)"
              >
                {{ user.name }}
              </t-tag>
            </div>
          </div>
        </div>
        <div class="dialog-footer">
          <t-button variant="outline" @click="leaderDialogVisible = false">取消</t-button>
          <t-button theme="primary" @click="confirmLeaderSelection">确定</t-button>
        </div>
      </div>
    </t-dialog>
  </div>
</template>
<script setup lang="ts">
import dayjs from 'dayjs';
import { MoveIcon } from 'tdesign-icons-vue-next';
import type {
  EnhancedTableInstanceFunctions,
  FormInstanceFunctions,
  FormRule,
  PageInfo,
  PrimaryTableCol,
} from 'tdesign-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { computed, h, nextTick, onMounted, reactive, ref } from 'vue';

import ConfirmDrawer from '@/components/ConfirmDrawer.vue';
import { useDictionary } from '@/hooks/useDictionary';
import { buildDictOptions } from '@/utils/dict';
import { request } from '@/utils/request';

interface OrgUnitNode {
  id: number;
  parentId?: number;
  name: string;
  shortName?: string;
  type: string;
  typeLabel?: string;
  sortOrder?: number;
  status?: number;
  phone?: string;
  email?: string;
  leaderIds?: number[];
  leaderNames?: string[];
  createdAt?: string;
  children?: OrgUnitNode[];
}

interface UserRow {
  id: number;
  name: string;
  account: string;
  orgUnitNames?: string[];
}

interface PageResult<T> {
  list: T[];
  total: number;
}

const loading = ref(false);
const saving = ref(false);
const savingOrder = ref(false);
const dirty = ref(false);
const dialogVisible = ref(false);
const leaderDialogVisible = ref(false);
const editingId = ref<number | null>(null);
const tableRef = ref<EnhancedTableInstanceFunctions<OrgUnitNode> | null>(null);
const expandedTreeNodes = ref<Array<string | number>>([]);
const expandAll = ref(true);

const orgTree = ref<OrgUnitNode[]>([]);
const filteredTree = ref<OrgUnitNode[]>([]);

const filters = reactive({
  keyword: '',
  status: null as null | number,
});

const formRef = ref<FormInstanceFunctions>();
const form = reactive({
  parentId: undefined as number | undefined,
  name: '',
  shortName: '',
  type: 'DEPARTMENT',
  status: 1,
  leaderIds: [] as number[],
  phone: '',
  email: '',
});

const statusDict = useDictionary('org_status');
const typeDict = useDictionary('org_type');

const fallbackStatusOptions = [
  { label: '正常', value: 1 },
  { label: '停用', value: 0 },
];

const fallbackTypeOptions = [
  { label: '单位', value: 'UNIT' },
  { label: '部门', value: 'DEPARTMENT' },
  { label: '科室', value: 'SECTION' },
  { label: '班组', value: 'TEAM' },
  { label: '用户', value: 'USER' },
];

const ORG_TYPE_VALUES = ['UNIT', 'DEPARTMENT', 'SECTION', 'TEAM', 'USER'];

const statusOptions = computed(() => buildDictOptions(statusDict.items.value, fallbackStatusOptions, [1, 0]));
const typeOptions = computed(() => buildDictOptions(typeDict.items.value, fallbackTypeOptions, ORG_TYPE_VALUES));

const ORG_UNIT_TYPES = new Set(['UNIT']);
const DEPARTMENT_TYPES = new Set(['DEPARTMENT', 'SECTION', 'TEAM']);
const TYPE_LABEL_MAP = new Map([
  ['单位', 'UNIT'],
  ['部门', 'DEPARTMENT'],
  ['科室', 'SECTION'],
  ['班组', 'TEAM'],
  ['用户', 'USER'],
]);
const normalizeOrgUnitType = (value?: string | null) => (value || '').toString().trim().toUpperCase();

const orgTreeKeys = { value: 'id', label: 'name', children: 'children' };

const treeConfig = reactive({
  childrenKey: 'children',
  treeNodeColumnIndex: 1,
  indent: 24,
  expandTreeNodeOnClick: true,
});

const columns: PrimaryTableCol[] = [
  {
    colKey: 'drag',
    title: '排序',
    width: 46,
    className: 't-table__drag-col',
    cell: () => h(MoveIcon, { class: 't-table__handle-draggable org-table__drag-handle' }),
  },
  { colKey: 'name', title: '机构名称', width: 220 },
  { colKey: 'typeLabel', title: '机构类型', width: 120 },
  { colKey: 'leaderNames', title: '负责人', width: 200 },
  { colKey: 'status', title: '状态', width: 100 },
  { colKey: 'createdAt', title: '创建时间', width: 180 },
  { colKey: 'op', title: '操作', width: 180, fixed: 'right' },
];

const rules: Record<string, FormRule[]> = {
  name: [{ required: true, message: '请输入机构名称', type: 'error' }],
  type: [{ required: true, message: '请选择机构类型', type: 'error' }],
};

const dialogTitle = computed(() => (editingId.value ? '编辑机构' : '添加机构'));

const leaderDisplay = computed(() => formatList(selectedLeaderNames.value));

const leaderFilters = reactive({
  keyword: '',
  orgKeyword: '',
  orgUnitId: null as number | null,
  departmentId: null as number | null,
});

const leaderRows = ref<UserRow[]>([]);
const leaderSelection = ref<UserRow[]>([]);
const selectedLeaderNames = ref<string[]>([]);
const leaderPagination = reactive({
  current: 1,
  pageSize: 8,
  total: 0,
});

const leaderColumns: PrimaryTableCol[] = [
  {
    colKey: 'row-select',
    type: 'multiple',
    width: 48,
    fixed: 'left',
  },
  { colKey: 'name', title: '用户名', width: 140 },
  { colKey: 'account', title: '账号', width: 160 },
  { colKey: 'orgUnitNames', title: '所属部门', width: 180 },
];

const leaderFilteredTree = computed(() => {
  if (!leaderFilters.orgKeyword) return orgTree.value;
  return filterTreeByKeyword(orgTree.value, leaderFilters.orgKeyword.trim());
});

const reload = async () => {
  loading.value = true;
  try {
    orgTree.value = await request.get<OrgUnitNode[]>({ url: '/system/org/tree' });
    filteredTree.value = applyFilter(orgTree.value, filters.keyword, filters.status);
    dirty.value = false;
    await nextTick();
    expandAll.value ? tableRef.value?.expandAll() : tableRef.value?.foldAll();
  } finally {
    loading.value = false;
  }
};

const buildReorderItems = (tree: OrgUnitNode[]) => {
  const items: Array<{ id: number; parentId: number | null; sortOrder: number }> = [];
  const walk = (nodes: OrgUnitNode[], parentId: number | null) => {
    nodes.forEach((n, index) => {
      items.push({ id: n.id, parentId, sortOrder: index });
      if (n.children?.length) walk(n.children, n.id);
    });
  };
  walk(tree || [], null);
  return items;
};

const saveOrder = async () => {
  const tree = tableRef.value?.getTreeNode?.() || orgTree.value;
  savingOrder.value = true;
  try {
    await request.put({ url: '/system/org/reorder', data: { items: buildReorderItems(tree) } });
    MessagePlugin.success('排序已保存');
    dirty.value = false;
    await reload();
  } finally {
    savingOrder.value = false;
  }
};

const onDragSort = (ctx: any) => {
  dirty.value = true;
  if (ctx?.newData) {
    const tree = tableRef.value?.getTreeNode?.();
    if (tree) {
      orgTree.value = tree;
      filteredTree.value = tree;
    }
  }
};

const onAbnormalDragSort = (params: any) => {
  if (params?.code === 1001) {
    MessagePlugin.warning('不同层级的元素，不允许直接拖拽排序');
    return;
  }
  if (params?.reason) MessagePlugin.warning(String(params.reason));
};

const beforeDragSort = () => {
  if (filters.keyword.trim() || filters.status != null) {
    MessagePlugin.warning('请先清空筛选条件，再进行拖拽排序');
    return false;
  }
  return true;
};

const resetFilters = () => {
  filters.keyword = '';
  filters.status = null;
  filteredTree.value = [...orgTree.value];
};

const openCreate = (parent?: OrgUnitNode) => {
  editingId.value = null;
  resetForm();
  if (parent) {
    form.parentId = parent.id;
  }
  dialogVisible.value = true;
};

const openEdit = (row: OrgUnitNode) => {
  editingId.value = row.id;
  form.parentId = row.parentId;
  form.name = row.name;
  form.shortName = row.shortName || '';
  form.type = row.type || 'DEPARTMENT';
  form.status = row.status ?? 1;
  form.phone = row.phone || '';
  form.email = row.email || '';
  form.leaderIds = [...(row.leaderIds || [])];
  selectedLeaderNames.value = [...(row.leaderNames || [])];
  dialogVisible.value = true;
};

const resetForm = () => {
  form.parentId = undefined;
  form.name = '';
  form.shortName = '';
  form.type = 'DEPARTMENT';
  form.status = 1;
  form.phone = '';
  form.email = '';
  form.leaderIds = [];
  selectedLeaderNames.value = [];
};

const submitForm = async () => {
  const valid = await formRef.value?.validate();
  if (valid !== true) return;
  saving.value = true;
  try {
    const payload = {
      parentId: form.parentId ?? null,
      name: form.name,
      shortName: form.shortName || undefined,
      type: form.type,
      status: form.status,
      leaderIds: form.leaderIds,
      phone: form.phone || undefined,
      email: form.email || undefined,
    };
    if (editingId.value) {
      await request.put({ url: `/system/org/${editingId.value}`, data: payload });
      MessagePlugin.success('保存成功');
    } else {
      await request.post({ url: '/system/org', data: payload });
      MessagePlugin.success('创建成功');
    }
    dialogVisible.value = false;
    reload();
  } finally {
    saving.value = false;
  }
};

const onSubmit = (ctx: any) => {
  if (ctx?.validateResult === true) submitForm();
};

const removeRow = (row: OrgUnitNode) => {
  const dialog = DialogPlugin.confirm({
    header: '删除机构',
    body: `确认删除机构「${row.name}」？`,
    theme: 'danger',
    confirmBtn: '删除',
    onConfirm: async () => {
      await request.delete({ url: `/system/org/${row.id}` });
      MessagePlugin.success('已删除');
      dialog.hide();
      reload();
    },
  });
};

const toggleExpand = () => {
  expandAll.value = !expandAll.value;
  expandAll.value ? tableRef.value?.expandAll() : tableRef.value?.foldAll();
};

const formatList = (names?: string[]) => {
  if (!names || names.length === 0) return '-';
  return names.join(' / ');
};

const applyFilter = (nodes: OrgUnitNode[], keywordValue?: string, status?: number | null): OrgUnitNode[] => {
  if (!keywordValue && status == null) return [...nodes];
  const matched: OrgUnitNode[] = [];
  nodes.forEach((node) => {
    const children = node.children ? applyFilter(node.children, keywordValue, status) : [];
    const keywordMatch = keywordValue
      ? node.name.includes(keywordValue) || node.shortName?.includes(keywordValue)
      : true;
    const statusMatch = status == null ? true : node.status === status;
    if ((keywordMatch && statusMatch) || children.length > 0) {
      matched.push({ ...node, children });
    }
  });
  return matched;
};

const filterTreeByKeyword = (nodes: OrgUnitNode[], keywordValue: string): OrgUnitNode[] => {
  const matched: OrgUnitNode[] = [];
  nodes.forEach((node) => {
    const children = node.children ? filterTreeByKeyword(node.children, keywordValue) : [];
    if (node.name.includes(keywordValue) || node.shortName?.includes(keywordValue) || children.length > 0) {
      matched.push({ ...node, children });
    }
  });
  return matched;
};

const openLeaderDialog = () => {
  leaderDialogVisible.value = true;
  leaderSelection.value = form.leaderIds.map((id, index) => ({
    id,
    name: selectedLeaderNames.value[index] || `用户${id}`,
    account: '',
  }));
  loadLeaders();
};

const loadLeaders = async () => {
  const res = await request.get<PageResult<UserRow>>({
    url: '/system/user/page',
    params: {
      keyword: leaderFilters.keyword || undefined,
      orgUnitId: leaderFilters.orgUnitId || undefined,
      departmentId: leaderFilters.departmentId || undefined,
      page: leaderPagination.current - 1,
      size: leaderPagination.pageSize,
    },
  });
  leaderRows.value = res.list;
  leaderPagination.total = res.total;
};

const onLeaderPageChange = (pageInfo: PageInfo) => {
  leaderPagination.current = pageInfo.current;
  leaderPagination.pageSize = pageInfo.pageSize;
  loadLeaders();
};

const resetLeaderFilters = () => {
  leaderFilters.keyword = '';
  leaderFilters.orgKeyword = '';
  leaderFilters.orgUnitId = null;
  leaderFilters.departmentId = null;
  leaderPagination.current = 1;
  loadLeaders();
};

const handleLeaderOrgSelect = (ctx: any) => {
  const node = ctx?.node;
  if (!node) return;
  const rawNode = node.data || (node.getModel?.() as any) || node;
  const rawId = rawNode?.id ?? node.value ?? node.id;
  const nodeId = Number(rawId);
  if (Number.isNaN(nodeId)) return;
  let nodeType = normalizeOrgUnitType(rawNode?.type ?? rawNode?.typeLabel);
  if (!nodeType && rawNode?.typeLabel) {
    nodeType = TYPE_LABEL_MAP.get(rawNode.typeLabel.trim()) || '';
  }
  if (DEPARTMENT_TYPES.has(nodeType)) {
    leaderFilters.departmentId = nodeId;
    leaderFilters.orgUnitId = null;
  } else if (ORG_UNIT_TYPES.has(nodeType)) {
    leaderFilters.orgUnitId = nodeId;
    leaderFilters.departmentId = null;
  } else {
    leaderFilters.orgUnitId = nodeId;
    leaderFilters.departmentId = null;
  }
  leaderPagination.current = 1;
  loadLeaders();
};

const removeLeader = (id: number) => {
  leaderSelection.value = leaderSelection.value.filter((user) => user.id !== id);
};

const handleLeaderSelectChange = (selectedKeys: Array<string | number>, ctx: any) => {
  const selectedIds = selectedKeys.map((key) => Number(key)).filter((key) => !Number.isNaN(key));
  if (ctx?.selectedRowData) {
    leaderSelection.value = ctx.selectedRowData as UserRow[];
  } else {
    leaderSelection.value = leaderRows.value.filter((row) => selectedIds.includes(row.id));
  }
};

const confirmLeaderSelection = () => {
  form.leaderIds = leaderSelection.value.map((user) => user.id);
  selectedLeaderNames.value = leaderSelection.value.map((user) => user.name);
  leaderDialogVisible.value = false;
};

const formatTime = (value?: string) => {
  if (!value) return '-';
  return dayjs(value).format('YYYY-MM-DD HH:mm');
};

onMounted(async () => {
  void statusDict.load();
  void typeDict.load();
  await reload();
});
</script>
<style scoped lang="less">
.org-management {
  :deep(.t-card__title) {
    font-weight: 600;
  }
}

.org-table-actions {
  flex-wrap: wrap;
  gap: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}

.leader-dialog__search {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.leader-dialog__keyword {
  flex: 1;
  min-width: 240px;
}

.leader-dialog__actions {
  flex-shrink: 0;
}

.leader-dialog__content {
  display: grid;
  grid-template-columns: 220px minmax(360px, 1fr) 240px;
  gap: 16px;
  min-height: 420px;
}

.leader-panel {
  background: var(--td-bg-color-container);
  border-radius: 8px;
  padding: 12px;
  min-height: 420px;
  display: flex;
  flex-direction: column;
}

.leader-panel__title {
  font-weight: 600;
  margin-bottom: 8px;
}

.leader-panel__body {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  min-height: 0;
}

.leader-panel__filter {
  flex-shrink: 0;
}

.leader-tree {
  flex: 1;
  overflow: auto;
}

.leader-selected {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  overflow: auto;
}

.org-table__drag-handle {
  cursor: grab;
  color: var(--td-text-color-secondary);
}

.org-table__drag-handle:active {
  cursor: grabbing;
}

@media (max-width: 1200px) {
  .leader-dialog__content {
    grid-template-columns: 1fr;
  }
}
</style>
