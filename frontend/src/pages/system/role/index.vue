<template>
  <t-card title="角色管理" :bordered="false">
    <t-space direction="vertical" style="width: 100%">
      <t-space>
        <t-button v-if="canCreate" theme="primary" @click="openCreate">新增角色</t-button>
        <t-button variant="outline" @click="reload">刷新</t-button>
      </t-space>

      <t-table row-key="id" :data="rows" :columns="columns" :loading="loading">
        <template #op="{ row }">
          <t-space>
            <t-link v-if="canUpdate" theme="primary" @click="openEdit(row)">编辑</t-link>
            <t-link v-if="canDelete" theme="danger" :disabled="row.name === 'admin'" @click="removeRow(row)"
              >删除</t-link
            >
            <span v-if="!canUpdate && !canDelete">--</span>
          </t-space>
        </template>
      </t-table>
    </t-space>

    <confirm-drawer v-model:visible="drawerVisible" :header="drawerTitle" size="760px">
      <t-form
        ref="formRef"
        :data="form"
        :rules="rules"
        label-width="100px"
        layout="vertical"
        label-align="right"
        @submit="onSubmit"
      >
        <t-row :gutter="[24, 24]">
          <t-col :xs="24" :sm="12">
            <t-form-item label="角色名" name="name">
              <t-input v-model="form.name" :disabled="form.name === 'admin'" style="max-width: 500px; width: 100%" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="描述" name="description">
              <t-input v-model="form.description" style="max-width: 500px; width: 100%" />
            </t-form-item>
          </t-col>
          <t-col :span="24">
            <t-form-item label="菜单权限">
              <t-space direction="vertical" style="width: 100%">
                <t-space>
                  <t-checkbox v-model="expandAll" @change="onExpandAllToggle">展开/折叠</t-checkbox>
                  <t-checkbox v-model="selectAll" @change="onSelectAllToggle">全选/全不选</t-checkbox>
                  <t-checkbox v-model="checkStrictly">父子联动</t-checkbox>
                </t-space>
                <div class="menu-tree-container">
                  <t-tree
                    ref="treeRef"
                    v-model="form.menuIds"
                    v-model:expanded="expandedMenuIds"
                    :data="menuTree"
                    :keys="{ value: 'id', label: 'titleZhCn' }"
                    checkable
                    :check-strictly="!checkStrictly"
                    :disabled="form.name === 'admin'"
                    hover
                  />
                </div>
              </t-space>
            </t-form-item>
          </t-col>
        </t-row>
      </t-form>

      <template #footer>
        <t-space class="tdesign-starter-action-bar">
          <t-button variant="outline" @click="drawerVisible = false">取消</t-button>
          <t-button theme="primary" :loading="saving" @click="submitForm">保存</t-button>
        </t-space>
      </template>
    </confirm-drawer>
  </t-card>
</template>
<script setup lang="ts">
import type { FormInstanceFunctions, FormRule, PrimaryTableCol } from 'tdesign-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted, reactive, ref, watch } from 'vue';

import ConfirmDrawer from '@/components/ConfirmDrawer.vue';
import { hasPerm } from '@/utils/permission';
import { request } from '@/utils/request';

interface RoleRow {
  id: number;
  name: string;
  description?: string;
  menuIds?: number[];
}

interface MenuNode {
  id: number;
  titleZhCn: string;
  path?: string | null;
  routeName?: string | null;
  nodeType?: string | null;
  actions?: string;
  hidden?: boolean;
  children?: MenuNode[];
}

const loading = ref(false);
const saving = ref(false);
const canCreate = computed(() => hasPerm('system:SystemRole:create'));
const canUpdate = computed(() => hasPerm('system:SystemRole:update'));
const canDelete = computed(() => hasPerm('system:SystemRole:delete'));
const rows = ref<RoleRow[]>([]);

const columns: PrimaryTableCol[] = [
  {
    colKey: 'serial-number',
    title: '序号',
    width: 80,
    fixed: 'left',
    cell: (_h, { rowIndex }) => String(rowIndex + 1),
  },
  { colKey: 'name', title: '角色名', width: 160, ellipsis: true },
  { colKey: 'description', title: '描述', width: 220, ellipsis: true },
  { colKey: 'op', title: '操作', width: 160, fixed: 'right' },
];

const drawerVisible = ref(false);
const editingId = ref<number | null>(null);
const drawerTitle = computed(() => (editingId.value ? '编辑角色' : '新增角色'));

const formRef = ref<FormInstanceFunctions>();
const form = reactive({
  name: '',
  description: '',
  menuIds: [] as number[],
});

const menuTree = ref<MenuNode[]>([]);
const treeRef = ref();
const expandAll = ref(false);
const expandedMenuIds = ref<number[]>([]);
const selectAll = ref(false);
const checkStrictly = ref(false);

const onExpandAllToggle = (val: boolean) => {
  if (val) {
    expandedMenuIds.value = getAllMenuIds(menuTree.value);
  } else {
    expandedMenuIds.value = [];
  }
};

const getAllMenuIds = (nodes: MenuNode[]): number[] => {
  let ids: number[] = [];
  nodes.forEach((n) => {
    ids.push(n.id);
    if (n.children?.length) {
      ids = ids.concat(getAllMenuIds(n.children));
    }
  });
  return ids;
};

const onSelectAllToggle = (val: boolean) => {
  if (val) {
    form.menuIds = getAllMenuIds(menuTree.value);
  } else {
    form.menuIds = [];
  }
};

const filterHiddenMenus = (nodes: MenuNode[]): MenuNode[] => {
  return nodes
    .filter((node) => !node.hidden)
    .map((node) => ({
      ...node,
      children: node.children ? filterHiddenMenus(node.children) : [],
    }));
};

// 监听菜单选择变化，更新全选状态
watch(
  () => form.menuIds,
  (val) => {
    const allIds = getAllMenuIds(menuTree.value);
    selectAll.value = val.length > 0 && val.length === allIds.length;
  },
  { deep: true },
);

// 监听展开状态变化，更新展开/折叠状态
watch(
  () => expandedMenuIds.value,
  (val) => {
    const allIds = getAllMenuIds(menuTree.value);
    expandAll.value = val.length > 0 && val.length === allIds.length;
  },
  { deep: true },
);

const rules: Record<string, FormRule[]> = {
  name: [{ required: true, message: '请输入角色名', type: 'error' }],
};

const resetForm = () => {
  form.name = '';
  form.description = '';
  form.menuIds = [];
  expandAll.value = false;
  expandedMenuIds.value = [];
  selectAll.value = false;
  checkStrictly.value = true;
};

const loadPermissionOptions = async () => {
  try {
    const tree = await request.get<MenuNode[]>({ url: '/system/menu/tree' });
    menuTree.value = filterHiddenMenus(tree || []);
  } catch {
    menuTree.value = [];
  }
};

const reload = async () => {
  loading.value = true;
  try {
    rows.value = await request.get<RoleRow[]>({ url: '/system/role/list' });
    await loadPermissionOptions();
  } finally {
    loading.value = false;
  }
};

const openCreate = async () => {
  if (!canCreate.value) {
    MessagePlugin.warning('无创建权限');
    return;
  }
  editingId.value = null;
  resetForm();
  await loadPermissionOptions();
  drawerVisible.value = true;
};

const openEdit = async (row: RoleRow) => {
  if (!canUpdate.value) {
    MessagePlugin.warning('无编辑权限');
    return;
  }
  editingId.value = row.id;
  resetForm();
  const detail = await request.get<RoleRow>({ url: `/system/role/${row.id}` });
  form.name = detail.name;
  form.description = detail.description || '';
  await loadPermissionOptions();

  form.menuIds = form.name === 'admin' ? getAllMenuIds(menuTree.value) : [...(detail.menuIds || [])];
  drawerVisible.value = true;
};

const submitForm = async () => {
  const vr = await formRef.value?.validate();
  if (vr !== true) return;
  if (editingId.value && !canUpdate.value) {
    MessagePlugin.warning('无修改权限');
    return;
  }
  if (!editingId.value && !canCreate.value) {
    MessagePlugin.warning('无创建权限');
    return;
  }
  saving.value = true;
  try {
    const payload = {
      name: form.name,
      description: form.description || undefined,
      menuIds: form.menuIds,
    };
    if (editingId.value) {
      await request.put({ url: `/system/role/${editingId.value}`, data: payload });
    } else {
      await request.post({ url: '/system/role', data: payload });
    }
    MessagePlugin.success('保存成功');
    drawerVisible.value = false;
    reload();
  } finally {
    saving.value = false;
  }
};

const onSubmit = (ctx: any) => {
  if (ctx?.validateResult === true) submitForm();
};

const removeRow = (row: RoleRow) => {
  if (!canDelete.value) {
    MessagePlugin.warning('无删除权限');
    return;
  }
  if (row.name === 'admin') {
    MessagePlugin.warning('不允许删除管理员角色');
    return;
  }
  const dialog = DialogPlugin.confirm({
    header: '删除角色',
    body: `确认删除角色「${row.name}」？`,
    theme: 'danger',
    confirmBtn: '删除',
    onConfirm: async () => {
      await request.delete({ url: `/system/role/${row.id}` });
      MessagePlugin.success('已删除');
      dialog.hide();
      reload();
    },
  });
};

onMounted(async () => {
  await reload();
});
</script>
<style lang="less" scoped>
.menu-tree-container {
  margin-top: 8px;
  padding: 8px;
  border: 1px solid var(--td-component-stroke);
  border-radius: var(--td-radius-default);
  max-height: 400px;
  overflow-y: auto;
}
</style>
