<template>
  <div class="user-management">
    <div class="user-management__left">
      <t-card title="组织机构" :bordered="false" class="org-panel">
        <t-input v-model="orgKeyword" type="search" clearable placeholder="请输入部门名称" @change="filterOrgTree" />
        <t-tree
          class="org-tree"
          :data="filteredOrgTree"
          :keys="orgTreeKeys"
          hover
          activable
          :expanded="expandedOrgIds"
          @click="handleOrgSelect"
        />
      </t-card>
    </div>

    <div class="user-management__right">
      <t-card title="用户管理" :bordered="false" class="user-panel">
        <div class="user-panel__filters">
          <div class="user-filter">
            <t-input v-model="filters.keyword" clearable placeholder="用户名称" />
            <t-input v-model="filters.mobile" clearable placeholder="手机号" />
            <t-select v-model="filters.status" :options="statusOptions" clearable placeholder="用户状态" />
            <t-date-range-picker
              v-model="filters.createdRange"
              allow-input
              clearable
              format="YYYY-MM-DD"
              value-type="YYYY-MM-DD"
              placeholder="开始日期 - 结束日期"
            />
            <div class="user-filter__buttons">
              <t-space size="small">
                <t-button theme="primary" @click="reload">搜索</t-button>
                <t-button variant="outline" @click="resetFilters">重置</t-button>
              </t-space>
            </div>
          </div>
          <div class="user-filter__actions">
            <t-button v-if="canCreate" theme="primary" @click="openCreate">新增</t-button>
          </div>
        </div>

        <t-table
          class="user-table"
          row-key="id"
          :data="rows"
          :columns="columns"
          :pagination="pagination"
          :loading="loading"
          @page-change="onPageChange"
        >
          <template #orgUnitNames="{ row }">
            <span>{{ formatOrgUnits(row.orgUnitNames) }}</span>
          </template>
          <template #departmentNames="{ row }">
            <span>{{ formatDepartments(row.departmentNames) }}</span>
          </template>
          <template #status="{ row }">
            <t-switch
              :value="row.status === 1"
              :disabled="!canUpdate"
              @change="(val) => toggleStatus(row, Boolean(val))"
            />
          </template>
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
      </t-card>
    </div>

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
            <t-form-item label="所属机构" name="orgUnitIds">
              <t-tree-select
                v-model="form.orgUnitIds"
                :data="orgUnitTree"
                multiple
                clearable
                filterable
                placeholder="选择机构"
                :keys="orgTreeKeys"
                style="max-width: 500px; width: 100%"
              />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="所属部门" name="departmentIds">
              <t-tree-select
                v-model="form.departmentIds"
                :data="departmentTree"
                multiple
                clearable
                filterable
                :disabled="form.orgUnitIds.length === 0"
                placeholder="请先选择机构"
                :keys="orgTreeKeys"
                style="max-width: 500px; width: 100%"
              />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="状态" name="status">
              <t-radio-group v-model="form.status">
                <t-radio v-for="opt in statusOptions" :key="String(opt.value)" :value="opt.value">
                  {{ opt.label }}
                </t-radio>
              </t-radio-group>
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

    <t-dialog v-model:visible="resetDialogVisible" header="重置密码" width="480px" :close-on-overlay-click="false">
      <t-form
        ref="resetFormRef"
        :data="resetPasswordForm"
        :rules="resetRules"
        label-width="90px"
        layout="vertical"
        @submit="onResetSubmit"
      >
        <t-form-item label="新密码" name="password" :help="resetPasswordHelp">
          <t-input v-model="resetPasswordForm.password" type="password" :placeholder="resetPasswordPlaceholder" />
        </t-form-item>
      </t-form>
      <template #footer>
        <t-space>
          <t-button variant="outline" @click="closeResetDialog">取消</t-button>
          <t-button theme="primary" :loading="resetSubmitting" @click="submitResetPassword">确认重置</t-button>
        </t-space>
      </template>
    </t-dialog>
  </div>
</template>
<script setup lang="ts">
import type { FormInstanceFunctions, FormRule, PageInfo, PrimaryTableCol, SelectOption } from 'tdesign-vue-next';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted, reactive, ref, watch } from 'vue';

import ConfirmDrawer from '@/components/ConfirmDrawer.vue';
import { useDictionary } from '@/hooks/useDictionary';
import { useUserStore } from '@/store';
import { hasPerm } from '@/utils/permission';
import { buildDictOptions } from '@/utils/dict';
import { request } from '@/utils/request';

type Mode = 'create' | 'edit';

interface RoleRow {
  id: number;
  name: string;
  description?: string;
  permissions?: string[];
}

interface OrgUnitNode {
  id: number;
  parentId?: number | null;
  name: string;
  type?: string;
  typeLabel?: string;
  disabled?: boolean;
  children?: OrgUnitNode[];
}

interface UserRow {
  id: number;
  guid: string;
  account: string;
  name: string;
  mobile?: string;
  email?: string;
  idCard?: string;
  joinDay?: string;
  team?: string;
  roles?: string[];
  orgUnitIds?: number[];
  orgUnitNames?: string[];
  departmentIds?: number[];
  departmentNames?: string[];
  status?: number;
  createdAt?: string;
}

interface PageResult<T> {
  list: T[];
  total: number;
}

const loading = ref(false);
const saving = ref(false);
const userStore = useUserStore();

const rows = ref<UserRow[]>([]);
const roles = ref<RoleRow[]>([]);
const orgTree = ref<OrgUnitNode[]>([]);
const filteredOrgTree = ref<OrgUnitNode[]>([]);
const orgKeyword = ref('');
const selectedOrgUnitId = ref<number | null>(null);
const selectedDepartmentId = ref<number | null>(null);
const expandedOrgIds = ref<number[]>([]);

const filters = reactive({
  keyword: '',
  mobile: '',
  status: null as null | number,
  createdRange: [] as string[],
});

const statusDict = useDictionary('user_status');
const fallbackStatusOptions = [
  { label: '正常', value: 1 },
  { label: '停用', value: 0 },
];
const statusOptions = computed(() => buildDictOptions(statusDict.items.value, fallbackStatusOptions, [1, 0]));

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

const orgTreeKeys = { value: 'id', label: 'name', children: 'children' };
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

const buildOrgUnitTree = (nodes: OrgUnitNode[]): OrgUnitNode[] => {
  const result = nodes
    .map((node) => {
      const isOrg = ORG_UNIT_TYPES.has(node.type || '');
      if (!isOrg) return null;
      const children = node.children ? buildOrgUnitTree(node.children) : [];
      return { ...node, children };
    })
    .filter((node) => node !== null);
  return result as OrgUnitNode[];
};

const findNodeById = (nodes: OrgUnitNode[], targetId: number | string): OrgUnitNode | null => {
  for (const node of nodes) {
    if (String(node.id) === String(targetId)) return node;
    if (node.children && node.children.length) {
      const found = findNodeById(node.children, targetId);
      if (found) return found;
    }
  }
  return null;
};

const buildDepartmentSubtree = (node: OrgUnitNode): OrgUnitNode | null => {
  const children = node.children
    ? node.children.map((child) => buildDepartmentSubtree(child)).filter((child): child is OrgUnitNode => !!child)
    : [];
  const type = node.type || '';
  const isDept = DEPARTMENT_TYPES.has(type);
  const isOrg = ORG_UNIT_TYPES.has(type);
  if (!isDept && !isOrg && children.length === 0) return null;
  return { ...node, children, disabled: !isDept };
};

const buildDepartmentTree = (nodes: OrgUnitNode[], selectedOrgIds: number[]): OrgUnitNode[] => {
  if (!selectedOrgIds || selectedOrgIds.length === 0) return [];
  const roots = selectedOrgIds.map((id) => findNodeById(nodes, id)).filter((node): node is OrgUnitNode => !!node);
  return roots.map((root) => buildDepartmentSubtree(root)).filter((node): node is OrgUnitNode => !!node);
};

const collectSelectableIds = (
  nodes: OrgUnitNode[],
  predicate: (node: OrgUnitNode) => boolean,
  bucket: Set<number> = new Set(),
): Set<number> => {
  nodes.forEach((node) => {
    if (predicate(node)) bucket.add(node.id);
    if (node.children && node.children.length) collectSelectableIds(node.children, predicate, bucket);
  });
  return bucket;
};

const orgUnitTree = computed(() => buildOrgUnitTree(orgTree.value));
const departmentTree = computed(() => buildDepartmentTree(orgTree.value, form.orgUnitIds));
const orgSelectableIds = computed(() =>
  collectSelectableIds(orgUnitTree.value, (node) => ORG_UNIT_TYPES.has(node.type || '')),
);
const departmentSelectableIds = computed(() =>
  collectSelectableIds(departmentTree.value, (node) => DEPARTMENT_TYPES.has(node.type || '')),
);

const roleOptions = computed<SelectOption[]>(() => (roles.value || []).map((r) => ({ label: r.name, value: r.name })));

const columns: PrimaryTableCol[] = [
  { colKey: 'name', title: '用户名称', width: 140 },
  { colKey: 'orgUnitNames', title: '所属机构', width: 180, ellipsis: true },
  { colKey: 'departmentNames', title: '所属部门', width: 180, ellipsis: true },
  { colKey: 'account', title: '系统账号', width: 160, ellipsis: true },
  { colKey: 'guid', title: '系统编号', width: 260, ellipsis: true },
  { colKey: 'mobile', title: '手机号', width: 140, ellipsis: true },
  { colKey: 'status', title: '状态', width: 120 },
  { colKey: 'createdAt', title: '创建时间', width: 180 },
  { colKey: 'op', title: '操作', width: 200, fixed: 'right' },
];

const drawerVisible = ref(false);
const mode = ref<Mode>('create');
const editingId = ref<number | null>(null);
const resetDialogVisible = ref(false);
const resetSubmitting = ref(false);
const resetFormRef = ref<FormInstanceFunctions>();
const resetTarget = ref<UserRow | null>(null);
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
  orgUnitIds: [] as number[],
  departmentIds: [] as number[],
  status: 1,
});

const resetPasswordForm = reactive({
  password: '',
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
const resetPasswordPlaceholder = computed(() => (requiresCustomPassword.value ? '请输入新密码' : '留空则默认 123456'));
const resetPasswordHelp = computed(() =>
  requiresCustomPassword.value ? `需符合密码规范：${passwordRequirementMessage.value}` : '留空则默认 123456',
);

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
        message: '请选择至少一个角色',
        type: 'error',
      },
    ],
  };
});

const resetRules = computed<Record<string, FormRule[]>>(() => {
  const resetPasswordRules: FormRule[] = [];
  if (requiresCustomPassword.value) {
    resetPasswordRules.push({ required: true, message: '请输入新密码', type: 'error' });
  }
  resetPasswordRules.push({
    validator: (val: string) => validatePasswordPolicy(val || ''),
    message: `密码需满足：${passwordRequirementMessage.value}`,
    type: 'error',
  });
  return {
    password: resetPasswordRules,
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
  form.orgUnitIds = [];
  form.departmentIds = [];
  form.status = 1;
};

const syncOrgSelection = () => {
  if (orgTree.value.length === 0) return;
  if (!Array.isArray(form.orgUnitIds)) {
    form.orgUnitIds = [];
    return;
  }
  if (form.orgUnitIds.length === 0) return;
  const allowed = orgSelectableIds.value;
  const next = form.orgUnitIds.filter((id) => allowed.has(id));
  const changed = next.length !== form.orgUnitIds.length || next.some((id, idx) => id !== form.orgUnitIds[idx]);
  if (changed) form.orgUnitIds = next;
};

const syncDepartmentSelection = () => {
  if (orgTree.value.length === 0) return;
  if (!Array.isArray(form.departmentIds)) {
    form.departmentIds = [];
    return;
  }
  if (form.departmentIds.length === 0) return;
  const allowed = departmentSelectableIds.value;
  const next = form.departmentIds.filter((id) => allowed.has(id));
  const changed = next.length !== form.departmentIds.length || next.some((id, idx) => id !== form.departmentIds[idx]);
  if (changed) form.departmentIds = next;
};

watch(orgSelectableIds, syncOrgSelection, { immediate: true });
watch(departmentSelectableIds, syncDepartmentSelection, { immediate: true });
watch(
  () => form.orgUnitIds,
  () => {
    syncOrgSelection();
    syncDepartmentSelection();
  },
  { deep: true },
);

const loadRoles = async () => {
  roles.value = await request.get<RoleRow[]>({ url: '/system/role/list' });
};

const loadOrgTree = async () => {
  orgTree.value = await request.get<OrgUnitNode[]>({ url: '/system/org/tree' });
  filteredOrgTree.value = [...orgTree.value];
  expandedOrgIds.value = flattenOrgIds(orgTree.value);
};

const filterOrgTree = () => {
  if (!orgKeyword.value) {
    filteredOrgTree.value = [...orgTree.value];
    return;
  }
  filteredOrgTree.value = filterTree(orgTree.value, orgKeyword.value.trim());
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
    const [startDate, endDate] = filters.createdRange || [];
    const res = await request.get<PageResult<UserRow>>({
      url: '/system/user/page',
      params: {
        keyword: filters.keyword || undefined,
        mobile: filters.mobile || undefined,
        orgUnitId: selectedOrgUnitId.value || undefined,
        departmentId: selectedDepartmentId.value || undefined,
        status: filters.status ?? undefined,
        startTime: startDate ? `${startDate} 00:00:00` : undefined,
        endTime: endDate ? `${endDate} 23:59:59` : undefined,
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

const resetFilters = () => {
  filters.keyword = '';
  filters.mobile = '';
  filters.status = null;
  filters.createdRange = [];
  selectedOrgUnitId.value = null;
  selectedDepartmentId.value = null;
  reload();
};

const handleOrgSelect = (ctx: any) => {
  const node = ctx?.node;
  if (!node) return;
  const rawNode = node.data || (node.getModel?.() as any) || node;
  const rawId = rawNode?.id ?? node.value ?? node.id;
  const nodeId = Number(rawId);
  if (Number.isNaN(nodeId)) return;
  const resolvedNode = findNodeById(orgTree.value, nodeId);
  let nodeType = normalizeOrgUnitType(resolvedNode?.type ?? rawNode?.type);
  if (!nodeType && rawNode?.typeLabel) {
    nodeType = TYPE_LABEL_MAP.get(rawNode.typeLabel.trim()) || '';
  }
  if (!nodeType) {
    const parentId = resolvedNode?.parentId ?? rawNode?.parentId;
    if (parentId != null && parentId !== 0) {
      const parentNode = findNodeById(orgTree.value, parentId);
      const parentType = normalizeOrgUnitType(parentNode?.type ?? parentNode?.typeLabel);
      if (ORG_UNIT_TYPES.has(parentType)) nodeType = 'DEPARTMENT';
    }
  }
  if (DEPARTMENT_TYPES.has(nodeType)) {
    selectedDepartmentId.value = nodeId;
    selectedOrgUnitId.value = null;
  } else if (ORG_UNIT_TYPES.has(nodeType)) {
    selectedOrgUnitId.value = nodeId;
    selectedDepartmentId.value = null;
  } else {
    selectedOrgUnitId.value = nodeId;
    selectedDepartmentId.value = null;
  }
  pagination.current = 1;
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
  form.orgUnitIds = [...(row.orgUnitIds || [])];
  form.departmentIds = [...(row.departmentIds || [])];
  form.status = row.status ?? 1;
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
          orgUnitIds: form.orgUnitIds,
          departmentIds: form.departmentIds,
          status: form.status,
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
          orgUnitIds: form.orgUnitIds,
          departmentIds: form.departmentIds,
          status: form.status,
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
  resetTarget.value = row;
  resetPasswordForm.password = '';
  resetDialogVisible.value = true;
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

const toggleStatus = async (row: UserRow, enabled: boolean) => {
  if (!canUpdate.value) return;
  const targetStatus = enabled ? 1 : 0;
  await request.put({
    url: `/system/user/${row.id}`,
    data: {
      status: targetStatus,
    },
  });
  row.status = targetStatus;
};

const isRootAdmin = (row: UserRow) => {
  const account = (row?.account || '').trim().toLowerCase();
  return account === 'admin';
};

const isEditDisabled = (row: UserRow) => {
  if (!row) return false;
  return false;
};

const isResetDisabled = (row: UserRow) => {
  if (!row) return false;
  if (isRootAdmin(row)) return true;
  if (currentUserId.value && row.id === currentUserId.value) return true;
  return false;
};

const closeResetDialog = () => {
  resetDialogVisible.value = false;
  resetPasswordForm.password = '';
  resetTarget.value = null;
};

const submitResetPassword = async () => {
  if (!resetTarget.value) return;
  resetSubmitting.value = true;
  try {
    const valid = await resetFormRef.value?.validate();
    if (valid !== true) return;
    const payload = resetPasswordForm.password ? { password: resetPasswordForm.password } : undefined;
    await request.post({ url: `/system/user/${resetTarget.value.id}/reset-password`, data: payload });
    MessagePlugin.success('已重置');
    closeResetDialog();
  } finally {
    resetSubmitting.value = false;
  }
};

const onResetSubmit = (ctx: any) => {
  if (ctx?.validateResult === true) submitResetPassword();
};

const isDeleteDisabled = (row: UserRow) => {
  if (!row) return false;
  if (isRootAdmin(row)) return true;
  if (currentUserId.value && row.id === currentUserId.value) return true;
  return false;
};

const formatOrgUnits = (names?: string[]) => {
  if (!names || names.length === 0) return '-';
  return names.join(' / ');
};

const formatDepartments = (names?: string[]) => {
  if (!names || names.length === 0) return '-';
  return names.join(' / ');
};

const filterTree = (nodes: OrgUnitNode[], keywordValue: string): OrgUnitNode[] => {
  const matched: OrgUnitNode[] = [];
  nodes.forEach((node) => {
    const children = node.children ? filterTree(node.children, keywordValue) : [];
    if (node.name.includes(keywordValue) || children.length > 0) {
      matched.push({ ...node, children });
    }
  });
  return matched;
};

const flattenOrgIds = (nodes: OrgUnitNode[]): number[] => {
  const ids: number[] = [];
  nodes.forEach((node) => {
    ids.push(node.id);
    if (node.children && node.children.length) {
      ids.push(...flattenOrgIds(node.children));
    }
  });
  return ids;
};

onMounted(async () => {
  void statusDict.load();
  await loadRoles();
  await loadOrgTree();
  await loadPasswordPolicy();
  await reload();
});
</script>
<style scoped lang="less">
.user-management {
  display: flex;
  gap: 16px;
  min-height: calc(100vh - 200px);
}

.user-management__left {
  width: 240px;
  flex-shrink: 0;
}

.user-management__right {
  flex: 1;
  min-width: 0;
}

.org-panel {
  height: 100%;
}

.org-tree {
  margin-top: 16px;
  max-height: calc(100vh - 320px);
  overflow: auto;
}

.user-panel__filters {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 8px 0 16px;
  border-bottom: 1px solid var(--td-component-border);
}

.user-filter {
  display: grid;
  grid-template-columns: repeat(4, minmax(160px, 1fr)) auto;
  gap: 12px;
  align-items: center;
}

.user-filter__buttons {
  display: flex;
  justify-content: flex-end;
}

.user-filter__actions {
  display: flex;
  justify-content: flex-end;
}

.user-table {
  margin-top: 16px;
}

.user-table-actions {
  flex-wrap: wrap;
  gap: 8px;
}

@media (max-width: 1200px) {
  .user-management {
    flex-direction: column;
  }

  .user-management__left {
    width: 100%;
  }

  .user-filter {
    grid-template-columns: repeat(2, minmax(140px, 1fr)) auto;
  }
}

@media (max-width: 768px) {
  .user-filter {
    grid-template-columns: 1fr;
  }
}
</style>
