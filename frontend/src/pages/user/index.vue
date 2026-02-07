<template>
  <div class="user-center-container">
    <div class="user-center-grid">
      <div class="user-center-left">
        <t-card :bordered="false" class="user-info-card">
          <div class="user-info-header">
            <div class="user-avatar-wrapper">
              <t-upload
                action="/api/system/file/upload?page=user-profile"
                name="file"
                :show-file-list="false"
                :headers="uploadHeaders"
                theme="custom"
                @success="handleAvatarSuccess"
                @fail="handleAvatarFail"
              >
                <t-avatar size="100px" :image="profile.avatar" class="user-avatar">
                  <template v-if="!profile.avatar" #icon>
                    <t-icon name="user" />
                  </template>
                  <div class="avatar-edit-overlay">
                    <t-icon name="edit" />
                  </div>
                </t-avatar>
              </t-upload>
            </div>
            <div class="user-name">{{ profile.name }}</div>
            <div class="user-introduction">专注于用户体验视觉设计</div>
          </div>

          <div class="user-info-detail">
            <div class="detail-item">
              <t-icon name="mail" />
              <span>{{ profile.email || 'jdkjjfnndf@mall.com' }}</span>
            </div>
            <div class="detail-item">
              <t-icon name="user" />
              <span>交互专家</span>
            </div>
            <div class="detail-item">
              <t-icon name="location" />
              <span>{{
                (profile.province || '') +
                  (profile.city || '') +
                  (profile.district || '') +
                  (profile.address || '') || '广东省深圳市'
              }}</span>
            </div>
            <div class="detail-item">
              <t-icon name="usergroup" />
              <span>{{ profile.team || '字节跳动 - 某某平台部 - UED' }}</span>
            </div>
          </div>
        </t-card>
      </div>

      <div class="user-center-right">
        <!-- 基本设置 -->
        <t-card title="个人信息" :bordered="false" class="user-setting-card">
          <template #actions>
            <t-button theme="primary" variant="text" @click="openEditDrawer">
              <t-icon name="edit" />
              编辑
            </t-button>
          </template>
          <t-descriptions :column="profileDescColumn" layout="horizontal" class="user-profile-descriptions">
            <t-descriptions-item label="手机">{{ profile.mobile || '-' }}</t-descriptions-item>
            <t-descriptions-item label="座机">{{ profile.phone || '-' }}</t-descriptions-item>
            <t-descriptions-item label="邮箱">{{ profile.email || '-' }}</t-descriptions-item>
            <t-descriptions-item label="角色">{{ formatList(displayedRoles) }}</t-descriptions-item>
            <t-descriptions-item label="所属部门" :span="2">{{ formatList(displayedOrgUnits) }}</t-descriptions-item>
            <t-descriptions-item label="座位">{{ profile.seat || '-' }}</t-descriptions-item>
            <t-descriptions-item label="主体">{{ profile.entity || '-' }}</t-descriptions-item>
            <t-descriptions-item label="上级">{{ profile.leader || '-' }}</t-descriptions-item>
            <t-descriptions-item label="职位">{{ profile.position || '-' }}</t-descriptions-item>
            <t-descriptions-item label="入职时间">{{ profile.joinDay || '-' }}</t-descriptions-item>
            <t-descriptions-item label="所属团队" :span="2">{{ profile.team || '-' }}</t-descriptions-item>
          </t-descriptions>
        </t-card>

        <!-- 编辑抽屉 -->
        <confirm-drawer v-model:visible="editProfileVisible" header="编辑个人信息" :size="drawerSize">
          <t-form
            ref="profileFormRef"
            class="drawer-form--single"
            :data="profileForm"
            :rules="profileRules"
            label-align="right"
            label-width="120px"
            layout="vertical"
            @submit="handleUpdateProfile"
          >
            <t-row :gutter="[24, 24]">
              <t-col :xs="24" :sm="12">
                <t-form-item label="角色">
                  <t-input :value="formatList(displayedRoles)" readonly />
                </t-form-item>
              </t-col>
              <t-col :xs="24" :sm="12">
                <t-form-item label="所属部门">
                  <t-input :value="formatList(displayedOrgUnits)" readonly />
                </t-form-item>
              </t-col>
              <t-col :xs="24" :sm="12">
                <t-form-item label="姓名" name="name">
                  <t-input v-model="profileForm.name" placeholder="请输入姓名" />
                </t-form-item>
              </t-col>
              <t-col :xs="24" :sm="12">
                <t-form-item label="性别" name="gender">
                  <t-select v-model="profileForm.gender" :options="genderOptions" placeholder="请选择性别" />
                </t-form-item>
              </t-col>
              <t-col :xs="24" :sm="12">
                <t-form-item label="昵称" name="nickname">
                  <t-input v-model="profileForm.nickname" placeholder="请输入昵称" />
                </t-form-item>
              </t-col>
              <t-col :xs="24" :sm="12">
                <t-form-item label="邮箱" name="email">
                  <t-input v-model="profileForm.email" placeholder="请输入邮箱" />
                </t-form-item>
              </t-col>
              <t-col :xs="24" :sm="12">
                <t-form-item label="身份证号码" name="idCard">
                  <t-input v-model="profileForm.idCard" placeholder="请输入身份证号码" />
                </t-form-item>
              </t-col>
              <t-col :xs="24" :sm="12">
                <t-form-item label="手机" name="mobile">
                  <t-input v-model="profileForm.mobile" placeholder="请输入手机号" />
                </t-form-item>
              </t-col>
              <t-col :xs="24" :sm="12">
                <t-form-item label="座机" name="phone">
                  <t-input v-model="profileForm.phone" placeholder="请输入座机号" />
                </t-form-item>
              </t-col>
              <t-col :xs="24" :sm="12">
                <t-form-item label="省/市/区县" name="provinceId">
                  <t-cascader
                    v-model="areaValue"
                    :options="areaOptions"
                    :lazy="areaLazy"
                    :load="areaLazy ? loadAreaChildren : undefined"
                    :loading="areaLoading"
                    value-type="full"
                    :show-all-levels="true"
                    clearable
                    placeholder="请选择省/市/区县"
                    @change="handleAreaChange"
                  />
                </t-form-item>
              </t-col>
              <t-col :xs="24" :sm="12">
                <t-form-item label="邮编" name="zipCode">
                  <t-input v-model="profileForm.zipCode" placeholder="请输入邮编" />
                </t-form-item>
              </t-col>
              <t-col :xs="24" :sm="12">
                <t-form-item label="详细地址" name="address">
                  <t-input v-model="profileForm.address" placeholder="请输入详细地址" />
                </t-form-item>
              </t-col>
              <t-col :xs="24" :sm="12">
                <t-form-item label="个人介绍" name="introduction">
                  <t-textarea
                    v-model="profileForm.introduction"
                    placeholder="请输入个人简介"
                    :autosize="{ minRows: 3, maxRows: 5 }"
                    class="introduction-textarea"
                  />
                </t-form-item>
              </t-col>
            </t-row>
          </t-form>
          <template #footer>
            <t-space class="tdesign-starter-action-bar">
              <t-button variant="outline" @click="editProfileVisible = false">取消</t-button>
              <t-button theme="primary" :loading="updatingProfile" @click="profileFormRef?.submit()">保存</t-button>
            </t-space>
          </template>
        </confirm-drawer>

        <!-- 更改密码 -->
        <t-card title="更改密码" :bordered="false" class="user-setting-card" style="margin-top: 24px">
          <t-form
            ref="passwordFormRef"
            :data="passwordForm"
            :rules="passwordRules"
            label-align="right"
            label-width="140px"
            colon
            @submit="handleSubmitPassword"
          >
            <t-row :gutter="[24, 24]">
              <t-col :span="24">
                <t-form-item label="当前密码" name="oldPassword">
                  <t-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码" />
                </t-form-item>
              </t-col>
              <t-col :span="24">
                <t-form-item label="新密码" name="newPassword">
                  <t-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
                </t-form-item>
              </t-col>
              <t-col :span="24">
                <t-form-item label="确认新密码" name="confirmPassword">
                  <t-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" />
                </t-form-item>
              </t-col>
            </t-row>
            <t-form-item class="form-submit">
              <t-button theme="primary" type="submit" :loading="changingPassword">修改密码</t-button>
            </t-form-item>
          </t-form>
        </t-card>

        <!-- 登录日志 -->
        <t-card title="登录日志" :bordered="false" class="user-setting-card" style="margin-top: 24px">
          <t-table
            row-key="id"
            :data="loginLogs"
            :columns="loginLogColumns"
            :loading="loginLogLoading"
            :pagination="null"
          />
        </t-card>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import type { FormInstanceFunctions, FormRule, PrimaryTableCol, SubmitContext } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue';

import type { AreaNodeResponse, AreaPathNode } from '@/api/system/area';
import { fetchAreaChildren, fetchAreaPath, resolveAreaPath } from '@/api/system/area';
import type { ChangePasswordRequest, UserProfile } from '@/api/user';
import { changePassword, getMyProfile, updateMyProfile } from '@/api/user';
import ConfirmDrawer from '@/components/ConfirmDrawer.vue';
import { useDictionary } from '@/hooks/useDictionary';
import { useUserStore } from '@/store';
import { buildDictOptions, parseDictValue } from '@/utils/dict';
import { request } from '@/utils/request';

// 基础状态
const profileLoading = ref(false);
const updatingProfile = ref(false);

const userStore = useUserStore();

// 上传配置
const uploadHeaders = computed(() => ({
  Authorization: userStore.token,
}));

const handleAvatarSuccess = async (context: any) => {
  const url = context.response?.data?.url;
  if (url) {
    try {
      await updateMyProfile({ avatar: url });
      profile.value.avatar = url;
      // 同步更新 store 中的头像，确保右上角头像实时更新
      userStore.userInfo.avatar = url;
      MessagePlugin.success('头像更新成功');
    } catch {
      MessagePlugin.error('加载地区失败');
    }
  }
};

const handleAvatarFail = (context: any) => {
  console.error('Avatar upload failed:', context);
  const msg = context.response?.data?.message || context.response?.statusText || '上传失败';
  MessagePlugin.error(`头像上传失败: ${msg}`);
};

// 用户资料
const profile = ref<UserProfile>({} as UserProfile);
const profileDescColumn = 2;

const formatList = (items?: string[]) => {
  if (!items || items.length === 0) return '-';
  return items.join(' / ');
};

const displayedRoles = computed(() => {
  if (profile.value?.roles && profile.value.roles.length > 0) {
    return profile.value.roles;
  }
  return userStore.userInfo?.roles || [];
});

const displayedOrgUnits = computed(() => {
  if (profile.value?.orgUnitNames && profile.value.orgUnitNames.length > 0) {
    return profile.value.orgUnitNames;
  }
  return userStore.userInfo?.orgUnitNames || [];
});

// 资料表单
const profileFormRef = ref<FormInstanceFunctions>();
const profileForm = reactive({
  name: '',
  nickname: '',
  gender: '',
  mobile: '',
  email: '',
  idCard: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  provinceId: null as number | null,
  cityId: null as number | null,
  districtId: null as number | null,
  zipCode: '',
  address: '',
  introduction: '',
  tags: '',
});

const genderDict = useDictionary('gender');
const provinceDict = useDictionary('address_province');
const cityDict = useDictionary('address_city');
const districtDict = useDictionary('address_district');

const fallbackGenderOptions = [
  { label: '?', value: 'male' },
  { label: '?', value: 'female' },
  { label: '保密', value: 'secret' },
];

const genderOptions = computed(() => buildDictOptions(genderDict.items.value, fallbackGenderOptions));

interface AreaOption {
  label: string;
  value: number | string;
  level?: number;
  zipCode?: string | null;
  children?: AreaOption[] | boolean;
}

const areaOptions = ref<AreaOption[]>([]);
const areaValue = ref<Array<number | string>>([]);
const areaLoadingState = ref(false);
const areaLoading = computed(
  () => areaLoadingState.value || provinceDict.loading.value || cityDict.loading.value || districtDict.loading.value,
);
const useDictArea = computed(
  () => provinceDict.items.value.length > 0 || cityDict.items.value.length > 0 || districtDict.items.value.length > 0,
);
const areaLazy = computed(() => !useDictArea.value);

const showAreaError = (error: any) => {
  const raw = error?.response?.data?.message || error?.message || error?.response?.statusText || '';
  const msg = String(raw)
    .replace(/\s*\[\d{3}\]\s*$/, '')
    .trim();
  MessagePlugin.error(msg || '加载地区失败');
};

const toAreaOption = (row: AreaNodeResponse): AreaOption => ({
  label: row.name,
  value: row.id,
  level: row.level,
  zipCode: row.zipCode ?? null,
  children: row.level >= 3 ? [] : row.hasChildren ? true : [],
});

const resolveDictPathParts = (raw: string) => {
  const text = raw.trim();
  if (!text) return [];
  return text
    .split(/[>|/]/)
    .map((part) => part.trim())
    .filter(Boolean);
};

const parseDictAreaMeta = (item: any) => {
  const raw = String(item?.value ?? item?.label ?? '').trim();
  let province;
  let city;
  let district;
  let zipCode;
  if (raw.startsWith('{') && raw.endsWith('}')) {
    try {
      const parsed = JSON.parse(raw);
      province = parsed.province ?? parsed.p;
      city = parsed.city ?? parsed.c;
      district = parsed.district ?? parsed.d;
      zipCode = parsed.zipCode ?? parsed.zip;
    } catch (error) {
      console.warn('Parse area dict json failed:', error);
    }
  }
  if (!province && !city && !district) {
    const parts = resolveDictPathParts(raw);
    if (parts.length === 2) {
      [province, city] = parts;
    } else if (parts.length >= 3) {
      [province, city, district] = parts;
    }
  }
  return {
    province,
    city,
    district,
    zipCode,
  };
};

const createDictAreaOptions = () => {
  const provinceItems = provinceDict.items.value;
  const cityItems = cityDict.items.value;
  const districtItems = districtDict.items.value;

  const districtEntries = districtItems.map((item) => {
    const meta = parseDictAreaMeta(item);
    return {
      label: item.label,
      value: parseDictValue(item),
      province: meta.province,
      city: meta.city,
      zipCode: meta.zipCode,
    };
  });

  const cityEntries = cityItems.map((item) => {
    const meta = parseDictAreaMeta(item);
    return {
      label: item.label,
      value: parseDictValue(item),
      province: meta.province,
      city: meta.city,
    };
  });

  const allDistrictOptions = districtEntries.map((entry) => ({
    label: entry.label,
    value: entry.value,
    level: 3,
    zipCode: entry.zipCode ?? null,
    children: [],
  }));

  const buildDistrictOptions = (province?: string, city?: string) => {
    const filtered = districtEntries.filter((entry) => {
      if (entry.city) return entry.city === city;
      if (entry.province) return entry.province === province;
      return true;
    });
    const source = filtered.length > 0 ? filtered : districtEntries;
    return source.map((entry) => ({
      label: entry.label,
      value: entry.value,
      level: 3,
      zipCode: entry.zipCode ?? null,
      children: [],
    }));
  };

  const buildCityOptions = (province?: string) => {
    const filtered = cityEntries.filter((entry) => !entry.province || entry.province === province);
    const source = filtered.length > 0 ? filtered : cityEntries;
    return source.map((entry) => ({
      label: entry.label,
      value: entry.value,
      level: 2,
      children: buildDistrictOptions(province, entry.city || entry.label),
    }));
  };

  const provinces =
    provinceItems.length > 0
      ? provinceItems
      : [
          {
            label: '默认省份',
            value: 'default',
            status: 1,
          },
        ];

  return provinces.map((item) => ({
    label: item.label,
    value: parseDictValue(item),
    level: 1,
    children: buildCityOptions(item.label),
  }));
};

const loadRootAreas = async () => {
  if (useDictArea.value) {
    areaOptions.value = createDictAreaOptions();
    return;
  }
  if (areaOptions.value.length > 0) return;
  areaLoadingState.value = true;
  try {
    const rows = await fetchAreaChildren(0);
    areaOptions.value = rows.map(toAreaOption);
  } catch (error) {
    console.error('Load area root failed:', error);
    showAreaError(error);
  } finally {
    areaLoadingState.value = false;
  }
};

const loadAreaChildren = async (node: any) => {
  if (useDictArea.value) return [];
  if (node?.data?.level >= 3) return [];
  const parentId = Number(node?.value || 0);
  try {
    const rows = await fetchAreaChildren(parentId);
    return rows.map(toAreaOption);
  } catch (error) {
    console.error('Load area children failed:', error);
    showAreaError(error);
    return [];
  }
};

const resetAreaFields = () => {
  areaValue.value = [];
  profileForm.provinceId = null;
  profileForm.cityId = null;
  profileForm.districtId = null;
  profileForm.province = '';
  profileForm.city = '';
  profileForm.district = '';
  profileForm.zipCode = '';
};

const toNumericId = (value: unknown) => (typeof value === 'number' && Number.isFinite(value) ? value : null);

const applyAreaPath = (path: AreaPathNode[]) => {
  if (!path || path.length === 0) {
    resetAreaFields();
    return;
  }
  const trimmedPath = path.slice(0, 3);
  const ids = trimmedPath.map((node) => node.id);
  const names = trimmedPath.map((node) => node.name || '');
  areaValue.value = ids;
  profileForm.provinceId = toNumericId(ids[0]);
  profileForm.cityId = toNumericId(ids[1]);
  profileForm.districtId = toNumericId(ids[2]);
  profileForm.province = names[0] ?? '';
  profileForm.city = names[1] ?? '';
  profileForm.district = names[2] ?? '';
  profileForm.zipCode = trimmedPath[trimmedPath.length - 1]?.zipCode || '';
};

const ensureAreaPathOptions = async (path: AreaPathNode[]) => {
  if (!path || path.length === 0) return;
  await loadRootAreas();
  let cursor = areaOptions.value;
  const trimmedPath = path.slice(0, 3);
  for (let i = 0; i < trimmedPath.length; i += 1) {
    const node = trimmedPath[i];
    let option = cursor.find((item) => item.value === node.id);
    if (!option) {
      option = {
        label: node.name,
        value: node.id,
        level: node.level,
        zipCode: node.zipCode ?? null,
        children: i < trimmedPath.length - 1 ? true : [],
      };
      cursor.push(option);
    } else {
      option.label = node.name || option.label;
      option.level = node.level ?? option.level;
      if (node.zipCode !== undefined) {
        option.zipCode = node.zipCode ?? null;
      }
    }
    if (i === trimmedPath.length - 1) break;
    if (!Array.isArray(option.children)) {
      const rows = await fetchAreaChildren(node.id);
      option.children = rows.map(toAreaOption);
    }
    cursor = Array.isArray(option.children) ? option.children : [];
  }
};

const syncAreaFromProfile = async (data: UserProfile) => {
  if (useDictArea.value) {
    await loadRootAreas();
    const matchedProvince = areaOptions.value.find(
      (option) => option.label === data.province || String(option.value) === data.province,
    );
    const provincesToScan = matchedProvince ? [matchedProvince] : areaOptions.value;
    let matchedCity: AreaOption | undefined;
    let matchedDistrict: AreaOption | undefined;

    for (const province of provincesToScan) {
      const cityChildren = Array.isArray(province.children) ? province.children : [];
      matchedCity = cityChildren.find(
        (option) => option.label === data.city || String(option.value) === data.city,
      );
      if (matchedCity) {
        const districtChildren = Array.isArray(matchedCity.children) ? matchedCity.children : [];
        matchedDistrict = districtChildren.find(
          (option) => option.label === data.district || String(option.value) === data.district,
        );
      }
      if (matchedCity || matchedDistrict) break;
    }

    const path: AreaOption[] = [];
    if (matchedProvince) path.push(matchedProvince);
    if (matchedCity) path.push(matchedCity);
    if (matchedDistrict) path.push(matchedDistrict);

    if (path.length > 0) {
      areaValue.value = path.map((option) => option.value);
      profileForm.provinceId = toNumericId(path[0]?.value);
      profileForm.cityId = toNumericId(path[1]?.value);
      profileForm.districtId = toNumericId(path[2]?.value);
      profileForm.province = path[0]?.label || data.province || '';
      profileForm.city = path[1]?.label || data.city || '';
      profileForm.district = path[2]?.label || data.district || '';
      profileForm.zipCode = path[path.length - 1]?.zipCode || data.zipCode || '';
    } else if (!data.province && !data.city && !data.district) {
      resetAreaFields();
    }
    return;
  }

  const areaId = data.districtId || data.cityId || data.provinceId;
  const hasName = !!(data.province || data.city || data.district);
  let path: AreaPathNode[] = [];
  try {
    if (areaId) {
      path = await fetchAreaPath(areaId);
    } else if (hasName) {
      path = await resolveAreaPath({
        province: data.province,
        city: data.city,
        district: data.district,
      });
    }
  } catch (error) {
    console.error('Resolve area path failed:', error);
  }
  if (path.length > 0) {
    await ensureAreaPathOptions(path);
    applyAreaPath(path);
  } else if (!areaId && !hasName) {
    resetAreaFields();
  }
};

const handleAreaChange = (_value: any, context: any) => {
  const node = context?.node;
  if (!node) {
    resetAreaFields();
    return;
  }
  const pathNodes = node.getPath?.() || [];
  if (!pathNodes.length) {
    resetAreaFields();
    return;
  }
  const ids = pathNodes.map((item: any) => Number(item.value));
  const names = pathNodes.map((item: any) => String(item.label || item.data?.label || item.data?.name || ''));
  const zipCode = pathNodes[pathNodes.length - 1]?.data?.zipCode || '';
  areaValue.value = pathNodes.map((item: any) => item.value);
  profileForm.provinceId = toNumericId(ids[0]);
  profileForm.cityId = toNumericId(ids[1]);
  profileForm.districtId = toNumericId(ids[2]);
  profileForm.province = names[0] ?? '';
  profileForm.city = names[1] ?? '';
  profileForm.district = names[2] ?? '';
  profileForm.zipCode = zipCode;
};

const profileRules: Record<string, FormRule[]> = {
  name: [{ required: true, message: '请输入姓名', type: 'error' }],
  email: [{ email: true, message: '请输入正确的邮箱地址', type: 'warning' }],
};

// 密码修改
const changingPassword = ref(false);
const passwordFormRef = ref<FormInstanceFunctions>();
const passwordForm = reactive<ChangePasswordRequest>({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

const passwordRules: Record<string, FormRule[]> = {
  oldPassword: [{ required: true, message: '请输入当前密码', type: 'error' }],
  newPassword: [
    { required: true, message: '请输入新密码', type: 'error' },
    { min: 6, max: 20, message: '密码长度应为6-20位', type: 'error' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', type: 'error' },
    {
      validator: (val) => val === passwordForm.newPassword,
      message: '两次输入的密码不一致',
      type: 'error',
    },
  ],
};

interface LoginLogRow {
  id: number;
  detail?: string;
  account?: string;
  ipAddress?: string;
  deviceInfo?: string;
  createdAt?: string;
}

interface PageResult<T> {
  list: T[];
  total: number;
}

const loginLogs = ref<LoginLogRow[]>([]);
const loginLogLoading = ref(false);

const loginLogColumns: PrimaryTableCol[] = [
  { colKey: 'createdAt', title: '登录时间', width: 180 },
  { colKey: 'ipAddress', title: 'IP', width: 140 },
  { colKey: 'deviceInfo', title: '设备信息', minWidth: 220, ellipsis: true },
  { colKey: 'detail', title: '备注', minWidth: 200, ellipsis: true },
];

const loadDictionaries = async (force = false) => {
  await Promise.all([
    genderDict.load(force),
    provinceDict.load(force),
    cityDict.load(force),
    districtDict.load(force),
  ]);
};

const normalizeGender = (value?: string) => {
  if (!value) return '';
  if (value === 'secret') {
    const hasUnknown = genderDict.items.value.some((item) => item.value === 'unknown');
    return hasUnknown ? 'unknown' : value;
  }
  return value;
};

// 获取数据
const fetchProfile = async () => {
  profileLoading.value = true;
  try {
    await loadDictionaries();
    const res = await getMyProfile();
    profile.value = res;
    // 同步更新 store 中的用户信息，确保 Header 等组件实时更新
    userStore.userInfo = {
      ...userStore.userInfo,
      name: res.name || '',
      avatar: res.avatar || '',
    };
    // 同步到表单
    Object.assign(profileForm, {
      name: res.name || '',
      nickname: res.nickname || '',
      gender: normalizeGender(res.gender),
      mobile: res.mobile || '',
      email: res.email || '',
      idCard: res.idCard || '',
      phone: res.phone || '',
      seat: res.seat || '',
      provinceId: res.provinceId ?? null,
      province: res.province || '',
      cityId: res.cityId ?? null,
      city: res.city || '',
      districtId: res.districtId ?? null,
      district: res.district || '',
      zipCode: res.zipCode || '',
      address: res.address || '',
      introduction: res.introduction || '',
      tags: res.tags || '',
    });
    await syncAreaFromProfile(res);
  } catch {
    MessagePlugin.error('加载地区失败');
  } finally {
    fetchLoginLogs(profile.value.account || profile.value.email);
    profileLoading.value = false;
  }
};

const fetchLoginLogs = async (account?: string) => {
  loginLogLoading.value = true;
  try {
    const res = await request.get<PageResult<LoginLogRow>>({
      url: '/system/log/page',
      params: {
        action: 'LOGIN',
        keyword: account || undefined,
        page: 0,
        size: 5,
      },
    });
    loginLogs.value = res.list || [];
  } catch {
    MessagePlugin.error('加载地区失败');
  } finally {
    loginLogLoading.value = false;
  }
};

const editProfileVisible = ref(false);
const isMobile = ref(false);
const drawerSize = computed(() => (isMobile.value ? '100%' : '760px'));

const updateIsMobile = () => {
  if (typeof window === 'undefined') return;
  isMobile.value = window.innerWidth <= 768;
};

const openEditDrawer = () => {
  void loadDictionaries(true);
  void loadRootAreas();
  editProfileVisible.value = true;
};

// 提交个人资料更新
const handleUpdateProfile = async (context: SubmitContext) => {
  if (context.validateResult !== true) return;

  updatingProfile.value = true;
  try {
    await updateMyProfile({ ...profileForm });
    MessagePlugin.success('个人资料更新成功');
    fetchProfile();
  } catch (error) {
    console.error('Update profile failed:', error);
    MessagePlugin.error('加载地区失败');
  } finally {
    updatingProfile.value = false;
  }
};

// 提交密码修改
const handleSubmitPassword = async (context: SubmitContext) => {
  if (context.validateResult !== true) return;
  changingPassword.value = true;
  try {
    await changePassword(passwordForm);
    MessagePlugin.success('密码修改成功');
    // 清空表单
    passwordForm.oldPassword = '';
    passwordForm.newPassword = '';
    passwordForm.confirmPassword = '';
    passwordFormRef.value?.reset();
  } catch (err: any) {
    const raw = String(err?.message || '密码修改失败');
    const humanMsg = raw.replace(/\s*\[\d{3}\]\s*$/, '').trim();
    MessagePlugin.error(humanMsg || '密码修改失败');
  } finally {
    changingPassword.value = false;
  }
};

onMounted(() => {
  fetchProfile();
  updateIsMobile();
  if (typeof window !== 'undefined') {
    window.addEventListener('resize', updateIsMobile);
  }
});

onUnmounted(() => {
  if (typeof window !== 'undefined') {
    window.removeEventListener('resize', updateIsMobile);
  }
});
</script>
<style lang="less" scoped>
.user-center-container {
  .user-center-grid {
    display: grid;
    grid-template-columns: minmax(280px, 360px) minmax(0, 1fr);
    gap: 24px;
    align-items: start;
    width: 100%;
  }

  .user-center-left,
  .user-center-right {
    min-width: 0;
  }

  .user-info-card,
  .user-setting-card {
    width: 100%;
  }

  .user-info-card {
    height: auto;
    overflow: hidden;

    .user-info-header {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 48px 24px 32px;
      position: relative;
      z-index: 1;
      text-align: center;

      .user-avatar-wrapper {
        margin-bottom: 20px;
      }

      .user-avatar {
        border: 4px solid var(--td-bg-color-container);
        box-shadow: 0 4px 12px rgb(0 0 0 / 8%);
        position: relative;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          .avatar-edit-overlay {
            opacity: 1;
          }
        }

        .avatar-edit-overlay {
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          background: rgb(0 0 0 / 40%);
          display: flex;
          align-items: center;
          justify-content: center;
          color: #fff;
          opacity: 0;
          transition: opacity 0.3s;
          border-radius: 50%;
          font-size: 24px;
        }
      }

      .user-name {
        font: var(--td-font-title-large);
        color: var(--td-text-color-primary);
        margin-bottom: 8px;
        font-weight: 600;
      }

      .user-introduction {
        font: var(--td-font-body-medium);
        color: var(--td-text-color-secondary);
        max-width: 280px;
      }
    }

    .user-info-detail {
      padding: 16px 24px 24px;
      border-top: 1px solid var(--td-border-level-1-color);

      .detail-item {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 14px;
        color: var(--td-text-color-secondary);
        font: var(--td-font-body-medium);

        .t-icon {
          font-size: 16px;
          color: var(--td-text-color-secondary);
        }
      }
    }
  }

  .user-setting-card {
    :deep(.t-card__header) {
      padding: 18px 24px;
      border-bottom: 1px solid var(--td-border-level-1-color);
    }

    :deep(.t-card__title) {
      font-weight: 600;
      color: var(--td-text-color-primary);
    }

    :deep(.t-card__body) {
      padding: 24px;
    }

    .form-submit {
      display: flex;
      justify-content: flex-end;
      padding-top: 16px;
    }

    .introduction-textarea {
      width: 100%;
    }

    .user-profile-descriptions {
      :deep(.t-descriptions) {
        table-layout: auto;
      }

      :deep(.t-descriptions__row) {
        td {
          padding: 8px 0;

          &:nth-child(odd) {
            width: 90px;
            padding-right: 12px;
            color: var(--td-text-color-secondary);
            text-align: right;
          }

          &:nth-child(even) {
            padding-right: 40px;
            color: var(--td-text-color-primary);
          }
        }
      }
    }
  }

  @media (width <= 768px) {
    .user-center-grid {
      grid-template-columns: 1fr;
      gap: 16px;
    }

    .user-center-left,
    .user-center-right {
      width: 100%;
    }

    .user-info-card {
      margin-bottom: 16px;
    }

    .user-info-card {
      .user-info-header {
        padding: 24px 16px;
      }

      .user-info-detail {
        padding: 8px 16px 16px;
      }
    }

    .user-setting-card {
      :deep(.t-card__header) {
        padding: 14px 16px;
      }

      :deep(.t-card__body) {
        padding: 16px;
      }

      .form-submit {
        flex-direction: column;
        align-items: stretch;
        gap: 12px;
      }

      .form-submit :deep(.t-button) {
        width: 100%;
      }
    }
  }
}
</style>
