<template>
  <t-card title="团队管理" :bordered="false">
    <t-space direction="vertical" style="width: 100%">
      <t-space>
        <t-button v-if="canCreate" theme="primary" @click="openCreate">新增团队</t-button>
        <t-button variant="outline" :loading="loading" @click="loadTeams">刷新</t-button>
      </t-space>

      <t-table row-key="id" :data="rows" :columns="columns" :loading="loading">
        <template #status="{ row }">
          <t-tag :theme="row.status === 1 ? 'success' : 'default'" variant="light">
            {{ row.status === 1 ? '启用' : '停用' }}
          </t-tag>
        </template>
        <template #op="{ row }">
          <t-space>
            <t-link v-if="canUpdate" theme="primary" @click="openEdit(row)">编辑</t-link>
            <t-link v-if="canDelete" theme="danger" @click="removeRow(row)">删除</t-link>
            <span v-if="!canUpdate && !canDelete">--</span>
          </t-space>
        </template>
      </t-table>
    </t-space>

    <confirm-drawer v-model:visible="drawerVisible" :header="drawerTitle" size="760px">
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
          <t-col :xs="24" :sm="12">
            <t-form-item label="团队名称" name="label">
              <t-input v-model="form.label" placeholder="例如：研发一部" style="max-width: 500px; width: 100%" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="团队编码" name="value">
              <t-input v-model="form.value" placeholder="例如：rd-1" style="max-width: 500px; width: 100%" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="状态" name="status">
              <t-radio-group v-model="form.status">
                <t-radio :value="1">启用</t-radio>
                <t-radio :value="0">停用</t-radio>
              </t-radio-group>
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="排序" name="sort">
              <t-input-number v-model="form.sort" :min="0" style="max-width: 500px; width: 100%" />
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
import type { FormInstanceFunctions, FormRule, PrimaryTableCol, SubmitContext } from 'tdesign-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted, reactive, ref } from 'vue';

import type { SysDict, SysDictItem } from '@/api/system/dictionary';
import {
  createDict,
  createDictItem,
  deleteDictItem,
  fetchDictItems,
  fetchDictPage,
  updateDictItem,
} from '@/api/system/dictionary';
import ConfirmDrawer from '@/components/ConfirmDrawer.vue';
import { hasPerm } from '@/utils/permission';

const dictCode = 'team';
const loading = ref(false);
const saving = ref(false);
const rows = ref<SysDictItem[]>([]);
const dictInfo = ref<SysDict | null>(null);

const canCreate = computed(() => hasPerm('system:SystemDict:create'));
const canUpdate = computed(() => hasPerm('system:SystemDict:update'));
const canDelete = computed(() => hasPerm('system:SystemDict:delete'));

const columns: PrimaryTableCol[] = [
  {
    colKey: 'serial-number',
    title: '序号',
    width: 80,
    fixed: 'left',
    cell: (_h, { rowIndex }) => String(rowIndex + 1),
  },
  { colKey: 'label', title: '团队名称', minWidth: 180, ellipsis: true },
  { colKey: 'value', title: '团队编码', minWidth: 180, ellipsis: true },
  { colKey: 'status', title: '状态', width: 100 },
  { colKey: 'sort', title: '排序', width: 100 },
  { colKey: 'op', title: '操作', width: 140, fixed: 'right' },
];

const drawerVisible = ref(false);
const editingId = ref<number | null>(null);
const drawerTitle = computed(() => (editingId.value ? '编辑团队' : '新增团队'));

const formRef = ref<FormInstanceFunctions>();
const form = reactive({
  label: '',
  value: '',
  status: 1,
  sort: 0,
});

const rules: Record<string, FormRule[]> = {
  label: [{ required: true, message: '请输入团队名称', type: 'error' }],
  value: [{ required: true, message: '请输入团队编码', type: 'error' }],
};

const ensureTeamDict = async () => {
  if (dictInfo.value) return dictInfo.value;
  const res = await fetchDictPage({ keyword: dictCode, page: 0, size: 50 });
  let hit = (res.list || []).find((item) => item.code === dictCode) || null;
  if (!hit) {
    hit = await createDict({
      name: '团队',
      code: dictCode,
      status: 1,
      sort: 21,
      remark: '团队管理',
    });
  }
  dictInfo.value = hit;
  return hit;
};

const loadTeams = async () => {
  loading.value = true;
  try {
    const dict = await ensureTeamDict();
    const res = await fetchDictItems(dict.id, { page: 0, size: 200 });
    rows.value = (res.list || []).sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0));
  } catch (error: any) {
    MessagePlugin.error(error?.message || '加载团队失败');
  } finally {
    loading.value = false;
  }
};

const openCreate = () => {
  editingId.value = null;
  form.label = '';
  form.value = '';
  form.status = 1;
  form.sort = 0;
  drawerVisible.value = true;
};

const openEdit = (row: SysDictItem) => {
  editingId.value = row.id;
  form.label = row.label || '';
  form.value = row.value || '';
  form.status = row.status ?? 1;
  form.sort = row.sort ?? 0;
  drawerVisible.value = true;
};

const submitForm = async () => {
  await formRef.value?.submit();
};

const onSubmit = async (context: SubmitContext) => {
  if (context.validateResult !== true) return;
  const dict = await ensureTeamDict();
  saving.value = true;
  try {
    if (editingId.value) {
      await updateDictItem(editingId.value, {
        label: form.label,
        value: form.value,
        status: form.status,
        sort: form.sort,
      });
    } else {
      await createDictItem(dict.id, {
        label: form.label,
        value: form.value,
        status: form.status,
        sort: form.sort,
      });
    }
    MessagePlugin.success('保存成功');
    drawerVisible.value = false;
    loadTeams();
  } catch (error: any) {
    MessagePlugin.error(error?.message || '保存失败');
  } finally {
    saving.value = false;
  }
};

const removeRow = (row: SysDictItem) => {
  const dialog = DialogPlugin.confirm({
    header: '确认删除',
    body: `确定删除团队「${row.label}」吗？`,
    confirmBtn: '删除',
    onConfirm: async () => {
      dialog.hide();
      try {
        await deleteDictItem(row.id);
        MessagePlugin.success('已删除');
        loadTeams();
      } catch (error: any) {
        MessagePlugin.error(error?.message || '删除失败');
      }
    },
    onClose: () => dialog.hide(),
  });
};

onMounted(() => {
  loadTeams();
});
</script>
