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
            class="password-form"
            :data="passwordForm"
            :rules="passwordRules"
            label-align="right"
            label-width="140px"
            colon
            @submit="handleSubmitPassword"
          >
            <t-form-item label="当前密码" name="oldPassword">
              <t-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码" />
            </t-form-item>
            <t-form-item label="新密码" name="newPassword">
              <t-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
            </t-form-item>
            <t-form-item label="确认新密码" name="confirmPassword">
              <t-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" />
            </t-form-item>
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
const areaDict = useDictionary('address_district');

const genderOptions = computed(() => buildDictOptions(genderDict.items.value));

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
const areaDictHintMessage =
  '\u5730\u5740\u5b57\u5178\u672a\u5b8c\u5584\uff0c\u8bf7\u5148\u5728\u7cfb\u7edf\u5b57\u5178\u4e2d\u5728 address_district \u5b57\u5178\u9879\u4e2d\u5b8c\u5584 province/city/district \u5b57\u6bb5\u6570\u636e';
const areaLoading = computed(
  () => areaLoadingState.value || areaDict.loading.value,
);

const toAreaDictValue = (raw: unknown): string | number => {
  if (typeof raw === 'number' && Number.isFinite(raw)) return raw;
  if (typeof raw === 'string') return raw;
  if (typeof raw === 'boolean') return raw ? 'true' : 'false';
  if (raw == null) return '';
  return String(raw);
};

const createDictAreaOptions = (): AreaOption[] => {
  const areaItems = areaDict.items.value;
  if (areaItems.length === 0) {
    return [];
  }

  const districtEntries = areaItems
    .map((item) => {
      const row = item as any;
      const province = String(row.province || '').trim();
      const city = String(row.city || '').trim();
      const district = String(row.district || '').trim();
      const parsedValue = parseDictValue(item);
      return {
        label: item.label,
        value: toAreaDictValue(item.id ?? parsedValue),
        province,
        city,
        district,
      };
    })
    .filter((entry) => !!entry.province && !!entry.city && !!entry.district);
  const validEntries = districtEntries;
  if (validEntries.length === 0) {
    return [];
  }

  const buildDistrictOptions = (province?: string, city?: string): AreaOption[] => {
    if (!province || !city) return [];
    const filtered = validEntries.filter((entry) => entry.province === province && entry.city === city);
    const unique = new Set<string>();
    return filtered
      .filter((entry) => {
        const key = `${province}/${city}/${entry.district}`;
        if (unique.has(key)) return false;
        unique.add(key);
        return true;
      })
      .map((entry) => ({
        label: entry.district,
        value: entry.value,
        level: 3,
        children: [],
      }));
  };

  const buildCityOptions = (province?: string): AreaOption[] => {
    if (!province) return [];
    const source = Array.from(
      new Map(
        validEntries
          .filter((entry) => entry.province === province)
          .map((entry) => [entry.city as string, entry]),
      ).values(),
    );
    const unique = new Set<string>();
    return source
      .filter((entry) => {
        const city = String(entry.city || '').trim();
        if (!city || unique.has(city)) return false;
        unique.add(city);
        return true;
      })
      .map((entry) => {
        const city = String(entry.city || '').trim();
        return {
          label: city,
          value: `${province}/${city}`,
          level: 2,
          children: buildDistrictOptions(province, city),
        };
      });
  };

  const provinceEntries = Array.from(
    new Map(
      validEntries.map((entry) => [
        entry.province as string,
        {
          label: String(entry.province || '').trim(),
          value: String(entry.province || '').trim(),
        },
      ]),
    ).values(),
  )
    .map((entry) => ({
      label: entry.label,
      value: toAreaDictValue(entry.value),
    }))
    .filter((entry) => !!entry.label);
  const uniqueProvince = new Set<string>();
  const provinceList = provinceEntries.filter((entry) => {
    if (uniqueProvince.has(entry.label)) return false;
    uniqueProvince.add(entry.label);
    return true;
  });
  if (provinceList.length === 0) return [];
  return provinceList.map((province) => ({
    label: province.label,
    value: province.value || province.label,
    level: 1,
    children: buildCityOptions(province.label),
  }));
};

const areaDictReady = computed(() => {
  const options = createDictAreaOptions();
  if (options.length === 0) return false;
  const hasCity = options.some((province) => Array.isArray(province.children) && province.children.length > 0);
  const hasDistrict = options.some(
    (province) =>
      Array.isArray(province.children) &&
      province.children.some((city) => Array.isArray(city.children) && city.children.length > 0),
  );
  return hasCity && hasDistrict;
});

const ensureAreaDictReady = (notify = true) => {
  if (areaDictReady.value) return true;
  areaOptions.value = [];
  if (notify) {
    MessagePlugin.error(areaDictHintMessage);
  }
  return false;
};

const loadRootAreas = async (notify = true) => {
  areaLoadingState.value = true;
  try {
    if (!ensureAreaDictReady(notify)) return false;
    areaOptions.value = createDictAreaOptions();
    return true;
  } finally {
    areaLoadingState.value = false;
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

const syncAreaFromProfile = async (data: UserProfile) => {
  const loaded = await loadRootAreas(true);
  if (!loaded) {
    areaValue.value = [];
    return;
  }
  const matchedProvince = areaOptions.value.find(
    (option) => option.label === data.province || String(option.value) === data.province,
  );
  const provincesToScan = matchedProvince ? [matchedProvince] : areaOptions.value;
  let matchedCity: AreaOption | undefined;
  let matchedDistrict: AreaOption | undefined;

  for (const province of provincesToScan) {
    const cityChildren = Array.isArray(province.children) ? province.children : [];
    matchedCity = cityChildren.find((option) => option.label === data.city || String(option.value) === data.city);
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
    areaDict.load(force),
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

const openEditDrawer = async () => {
  editProfileVisible.value = true;
  try {
    await loadDictionaries(true);
    areaOptions.value = [];
    await loadRootAreas();
  } catch (error) {
    console.error('Load area dictionaries failed:', error);
  }
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

    .password-form {
      :deep(.t-form__controls) {
        flex: 1;
        min-width: 0;
      }

      :deep(.t-input) {
        width: 100%;
      }
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
        justify-content: flex-end;
      }

      .password-form {
        :deep(.t-form__controls) {
          flex: 1;
          min-width: 0;
        }
      }
    }
  }
}
</style>
