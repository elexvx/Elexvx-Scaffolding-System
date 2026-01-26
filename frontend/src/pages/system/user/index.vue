<template>
  <t-card title="用户管理" :bordered="false">
    <t-space direction="vertical" style="width: 100%">
      <t-space class="user-table-toolbar">
        <t-input
          v-model="keyword"
          type="search"
          clearable
          placeholder="按账号/姓名搜索"
          style="width: 260px"
          @enter="reload"
        />
        <t-button v-if="canCreate" theme="primary" @click="openCreate">新增用户</t-button>
        <t-button variant="outline" @click="reload">刷新</t-button>
      </t-space>

      <t-table
        row-key="id"
        :data="rows"
        :columns="columns"
        :pagination="pagination"
        :loading="loading"
        @page-change="onPageChange"
      >
        <template #roles="{ row }">
          <t-space>
            <t-tag v-for="r in row.roles || []" :key="r" theme="primary" variant="light">{{ r }}</t-tag>
          </t-space>
        </template>
        <template #op="{ row }">
          <t-space class="user-table-actions">
            <t-link v-if="canUpdate" :disabled="isEditDisabled(row)" theme="primary" @click="openEdit(row)"
              >编辑</t-link
            >
            <t-link v-if="canReset" :disabled="isResetDisabled(row)" theme="primary" @click="resetPwd(row)"
              >重置密码</t-link
            >
            <t-link v-if="canDelete" :disabled="isDeleteDisabled(row)" theme="danger" @click="removeRow(row)"
              >删除</t-link
            >
            <span v-if="!canUpdate && !canReset && !canDelete">--</span>
          </t-space>
        </template>
      </t-table>
    </t-space>

    <confirm-drawer v-model:visible="drawerVisible" :header="drawerTitle" size="720px">
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
            <t-form-item label="账号" name="account">
              <t-input
                v-model="form.account"
                :disabled="mode === 'edit'"
                placeholder="如：admin"
                style="max-width: 500px; width: 100%"
              />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="姓名" name="name">
              <t-input v-model="form.name" placeholder="如：张三" style="max-width: 500px; width: 100%" />
            </t-form-item>
          </t-col>
          <t-col v-if="mode === 'create'" :xs="24" :sm="12">
            <t-form-item label="初始密码" name="password" :help="passwordHelp">
              <t-input
                v-model="form.password"
                type="password"
                :placeholder="passwordPlaceholder"
                style="max-width: 500px; width: 100%"
              />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="角色" name="roles">
              <t-select
                v-model="form.roles"
                :options="roleOptions"
                multiple
                clearable
                placeholder="选择角色"
                style="max-width: 500px; width: 100%"
              />
            </t-form-item>
          </t-col>

          <t-col :xs="24" :sm="12">
            <t-form-item label="手机" name="mobile">
              <t-input v-model="form.mobile" placeholder="+86 138xxxx" style="max-width: 500px; width: 100%" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="邮箱" name="email">
              <t-input v-model="form.email" placeholder="xxx@company.com" style="max-width: 500px; width: 100%" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="身份证号码" name="idCard">
              <t-input v-model="form.idCard" placeholder="身份证号" style="max-width: 500px; width: 100%" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="入职日期" name="joinDay">
              <t-date-picker
                v-model="form.joinDay"
                clearable
                format="YYYY-MM-DD"
                value-type="YYYY-MM-DD"
                style="max-width: 500px; width: 100%"
              />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="团队" name="team">
              <t-input v-model="form.team" placeholder="组织路径/团队描述" style="max-width: 500px; width: 100%" />
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
import type { FormInstanceFunctions, FormRule, PageInfo, PrimaryTableCol, SelectOption } from 'tdesign-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted, reactive, ref } from 'vue';

import ConfirmDrawer from '@/components/ConfirmDrawer.vue';
import { useUserStore } from '@/store';
import { hasPerm } from '@/utils/permission';
import { request } from '@/utils/request';

type Mode = 'create' | 'edit';

interface RoleRow {
  id: number;
  name: string;
  description?: string;
  permissions?: string[];
}

interface UserRow {
  id: number;
  account: string;
  name: string;
  mobile?: string;
  email?: string;
  idCard?: string;
  joinDay?: string;
  team?: string;
  roles?: string[];
}

interface PageResult<T> {
  list: T[];
  total: number;
}

const keyword = ref('');
const loading = ref(false);
const saving = ref(false);
const userStore = useUserStore();

const rows = ref<UserRow[]>([]);
const roles = ref<RoleRow[]>([]);
const passwordPolicy = reactive({
  minLength: 6,
  requireUppercase: false,
  requireLowercase: false,
  requireSpecial: false,
  allowSequential: true,
});

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
});

const roleOptions = computed<SelectOption[]>(() => (roles.value || []).map((r) => ({ label: r.name, value: r.name })));

const columns: PrimaryTableCol[] = [
  {
    colKey: 'serial-number',
    title: '序号',
    width: 80,
    fixed: 'left',
    cell: (_h, { rowIndex }) => String((pagination.current - 1) * pagination.pageSize + rowIndex + 1),
  },
  { colKey: 'account', title: '账号', width: 160, ellipsis: true },
  { colKey: 'name', title: '姓名', width: 160, ellipsis: true },
  { colKey: 'mobile', title: '手机', width: 160, ellipsis: true },
  { colKey: 'email', title: '邮箱', width: 220, ellipsis: true },
  { colKey: 'idCard', title: '身份证号', width: 200, ellipsis: true },
  { colKey: 'roles', title: '角色', width: 220 },
  { colKey: 'op', title: '操作', width: 200, fixed: 'right' },
];

const drawerVisible = ref(false);
const mode = ref<Mode>('create');
const editingId = ref<number | null>(null);
const drawerTitle = computed(() => (mode.value === 'create' ? '新增用户' : '编辑用户'));
const currentUserId = computed(() => userStore.userInfo?.id);
const canCreate = computed(() => hasPerm('system:SystemUser:create'));
const canUpdate = computed(() => hasPerm('system:SystemUser:update'));
const canDelete = computed(() => hasPerm('system:SystemUser:delete'));
const canReset = computed(() => hasPerm('system:SystemUser:update'));

const formRef = ref<FormInstanceFunctions>();
const form = reactive({
  account: '',
  name: '',
  password: '',
  roles: [] as string[],
  mobile: '',
  email: '',
  idCard: '',
  joinDay: '' as string | '',
  team: '',
});

const minPasswordLength = computed(() =>
  passwordPolicy.minLength && passwordPolicy.minLength > 0 ? passwordPolicy.minLength : 6,
);

const requiresCustomPassword = computed(
  () =>
    minPasswordLength.value > 6 ||
    passwordPolicy.requireUppercase ||
    passwordPolicy.requireLowercase ||
    passwordPolicy.requireSpecial ||
    passwordPolicy.allowSequential === false,
);

const passwordRequirementMessage = computed(() => {
  const parts: string[] = [];
  if (minPasswordLength.value) parts.push(`至少 ${minPasswordLength.value} 位`);
  if (passwordPolicy.requireUppercase) parts.push('包含大写字母');
  if (passwordPolicy.requireLowercase) parts.push('包含小写字母');
  if (passwordPolicy.requireSpecial) parts.push('包含特殊字符');
  if (passwordPolicy.allowSequential === false) parts.push('禁止连续字符');
  return parts.join('、') || '至少 6 位';
});

const passwordHelp = computed(() =>
  requiresCustomPassword.value ? `需符合密码规范：${passwordRequirementMessage.value}` : '留空则默认 123456',
);

const passwordPlaceholder = computed(() => (requiresCustomPassword.value ? '请输入初始密码' : '默认 123456'));

const hasSequentialChars = (value: string) => {
  if (!value || value.length < 3) return false;
  let streak = 1;
  let prev = -1;
  for (const raw of value) {
    if (!/[a-z0-9]/i.test(raw)) {
      streak = 1;
      prev = -1;
      continue;
    }
    const current = raw.toLowerCase().charCodeAt(0);
    if (prev !== -1 && Math.abs(current - prev) === 1) {
      streak += 1;
      if (streak >= 3) return true;
    } else {
      streak = 1;
    }
    prev = current;
  }
  return false;
};

const validatePasswordPolicy = (val: string) => {
  if (!val) return true;
  const violations: string[] = [];
  if (val.length < minPasswordLength.value) violations.push(`至少 ${minPasswordLength.value} 位`);
  if (passwordPolicy.requireUppercase && !/[A-Z]/.test(val)) violations.push('包含大写字母');
  if (passwordPolicy.requireLowercase && !/[a-z]/.test(val)) violations.push('包含小写字母');
  if (passwordPolicy.requireSpecial && !/[^a-z0-9]/i.test(val)) violations.push('包含特殊字符');
  if (passwordPolicy.allowSequential === false && hasSequentialChars(val)) violations.push('禁止连续字符');
  return violations.length === 0;
};

const rules = computed<Record<string, FormRule[]>>(() => {
  const passwordRules: FormRule[] = [];
  if (mode.value === 'create' && requiresCustomPassword.value) {
    passwordRules.push({ required: true, message: '请输入初始密码', type: 'error' });
  }
  passwordRules.push({
    validator: (val: string) => validatePasswordPolicy(val),
    message: `密码需满足：${passwordRequirementMessage.value}`,
    type: 'error',
  });

  return {
    account: [
      { required: true, message: '请输入账号', type: 'error' },
      { validator: (val: string) => /^[\w@.-]+$/.test(val), message: '账号包含非法字符', type: 'error' },
    ],
    name: [{ required: true, message: '请输入姓名', type: 'error' }],
    password: mode.value === 'create' ? passwordRules : [],
    roles: [
      {
        validator: (val: string[]) => Array.isArray(val) && val.length > 0,
        message: 'Please select at least one role',
        type: 'error',
      },
    ],
  };
});

const resetForm = () => {
  form.account = '';
  form.name = '';
  form.password = '';
  form.roles = [];
  form.mobile = '';
  form.email = '';
  form.idCard = '';
  form.joinDay = '';
  form.team = '';
};

const loadRoles = async () => {
  roles.value = await request.get<RoleRow[]>({ url: '/system/role/list' });
};

const loadPasswordPolicy = async () => {
  try {
    const s = await request.get<any>({ url: '/system/ui' });
    if (!s) return;
    passwordPolicy.minLength = s.passwordMinLength ?? 6;
    passwordPolicy.requireUppercase = !!s.passwordRequireUppercase;
    passwordPolicy.requireLowercase = !!s.passwordRequireLowercase;
    passwordPolicy.requireSpecial = !!s.passwordRequireSpecial;
    passwordPolicy.allowSequential = s.passwordAllowSequential !== false;
  } catch (error) {
    console.warn('Failed to load password policy:', error);
  }
};

const reload = async () => {
  loading.value = true;
  try {
    const res = await request.get<PageResult<UserRow>>({
      url: '/system/user/page',
      params: {
        keyword: keyword.value || undefined,
        page: pagination.current - 1,
        size: pagination.pageSize,
      },
    });
    rows.value = res.list;
    pagination.total = res.total;
  } finally {
    loading.value = false;
  }
};

const onPageChange = (pi: PageInfo) => {
  pagination.current = pi.current;
  pagination.pageSize = pi.pageSize;
  reload();
};

const openCreate = () => {
  if (!canCreate.value) {
    MessagePlugin.warning('无创建权限');
    return;
  }
  mode.value = 'create';
  editingId.value = null;
  resetForm();
  drawerVisible.value = true;
};

const openEdit = (row: UserRow) => {
  if (!canUpdate.value) {
    MessagePlugin.warning('无编辑权限');
    return;
  }
  if (isEditDisabled(row)) return;
  mode.value = 'edit';
  editingId.value = row.id;
  resetForm();
  form.account = row.account;
  form.name = row.name;
  form.roles = [...(row.roles || [])];
  form.mobile = row.mobile || '';
  form.email = row.email || '';
  form.idCard = row.idCard || '';
  form.joinDay = row.joinDay || '';
  form.team = row.team || '';
  drawerVisible.value = true;
};

const submitForm = async () => {
  const vr = await formRef.value?.validate();
  if (vr !== true) return;
  saving.value = true;
  try {
    if (mode.value === 'create') {
      await request.post({
        url: '/system/user',
        data: {
          account: form.account,
          name: form.name,
          password: form.password || undefined,
          roles: form.roles,
          mobile: form.mobile || undefined,
          email: form.email || undefined,
          idCard: form.idCard || undefined,
          joinDay: form.joinDay || undefined,
          team: form.team || undefined,
        },
      });
      MessagePlugin.success('创建成功');
    } else {
      await request.put({
        url: `/system/user/${editingId.value}`,
        data: {
          name: form.name,
          roles: form.roles,
          mobile: form.mobile || undefined,
          email: form.email || undefined,
          idCard: form.idCard || undefined,
          joinDay: form.joinDay || undefined,
          team: form.team || undefined,
        },
      });
      MessagePlugin.success('保存成功');
    }
    drawerVisible.value = false;
    reload();
  } finally {
    saving.value = false;
  }
};

const onSubmit = (ctx: any) => {
  if (ctx?.validateResult === true) submitForm();
};

const resetPwd = (row: UserRow) => {
  if (!canReset.value) {
    MessagePlugin.warning('无重置权限');
    return;
  }
  if (isResetDisabled(row)) return;
  const dialog = DialogPlugin.confirm({
    header: '重置密码',
    body: `确认将用户「${row.account}」密码重置为 123456？`,
    confirmBtn: '重置',
    onConfirm: async () => {
      await request.post({ url: `/system/user/${row.id}/reset-password` });
      MessagePlugin.success('已重置');
      dialog.hide();
    },
  });
};

const removeRow = (row: UserRow) => {
  if (!canDelete.value) {
    MessagePlugin.warning('无删除权限');
    return;
  }
  if (isDeleteDisabled(row)) return;
  const dialog = DialogPlugin.confirm({
    header: '删除用户',
    body: `确认删除用户「${row.account}」？`,
    theme: 'danger',
    confirmBtn: '删除',
    onConfirm: async () => {
      await request.delete({ url: `/system/user/${row.id}` });
      MessagePlugin.success('已删除');
      dialog.hide();
      reload();
    },
  });
};

const isRootAdmin = (row: UserRow) => {
  const account = (row?.account || '').trim().toLowerCase();
  return account === 'admin';
};

const isEditDisabled = (row: UserRow) => {
  if (!row) return false;
  return isRootAdmin(row);
};

const isResetDisabled = (row: UserRow) => {
  if (!row) return false;
  if (isRootAdmin(row)) return true;
  if (currentUserId.value && row.id === currentUserId.value) return true;
  return false;
};

const isDeleteDisabled = (row: UserRow) => {
  if (!row) return false;
  if (isRootAdmin(row)) return true;
  if (currentUserId.value && row.id === currentUserId.value) return true;
  return false;
};

onMounted(async () => {
  await loadRoles();
  await loadPasswordPolicy();
  await reload();
});
</script>
<style scoped lang="less">
.user-table-toolbar {
  flex-wrap: wrap;
  gap: 12px;
}

.user-table-actions {
  flex-wrap: wrap;
  gap: 8px;
}

@media (width <= 768px) {
  .user-table-toolbar {
    align-items: stretch;
  }

  .user-table-toolbar :deep(.t-input) {
    width: 100%;
  }

  .user-table-toolbar :deep(.t-button) {
    width: 100%;
  }
}
</style>
