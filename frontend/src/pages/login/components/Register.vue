<template>
  <t-form
    ref="form"
    class="item-container"
    :data="formData"
    :rules="formRules"
    :show-error-message="false"
    label-width="0"
    @submit="onSubmit"
  >
    <t-form-item name="name">
      <t-input v-model="formData.name" size="large" placeholder="请输入姓名">
        <template #prefix-icon>
          <t-icon name="user" />
        </template>
      </t-input>
    </t-form-item>

    <t-form-item name="phone">
      <t-input v-model="formData.phone" :maxlength="11" size="large" placeholder="请输入手机号">
        <template #prefix-icon>
          <t-icon name="call" />
        </template>
      </t-input>
    </t-form-item>

    <t-form-item name="email">
      <t-input v-model="formData.email" type="text" size="large" placeholder="请输入邮箱">
        <template #prefix-icon>
          <t-icon name="mail" />
        </template>
      </t-input>
    </t-form-item>

    <t-form-item name="idCard">
      <t-input v-model="formData.idCard" type="text" size="large" placeholder="请输入身份证号码">
        <template #prefix-icon>
          <t-icon name="user" />
        </template>
      </t-input>
    </t-form-item>

    <t-form-item name="password">
      <t-input
        v-model="formData.password"
        size="large"
        :type="showPsw ? 'text' : 'password'"
        clearable
        placeholder="请输入密码"
      >
        <template #prefix-icon>
          <t-icon name="lock-on" />
        </template>
        <template #suffix-icon>
          <t-icon :name="showPsw ? 'browse' : 'browse-off'" @click="showPsw = !showPsw" />
        </template>
      </t-input>
    </t-form-item>

    <t-form-item name="confirmPassword">
      <t-input
        v-model="formData.confirmPassword"
        size="large"
        :type="showConfirmPsw ? 'text' : 'password'"
        clearable
        placeholder="请确认密码"
      >
        <template #prefix-icon>
          <t-icon name="lock-on" />
        </template>
        <template #suffix-icon>
          <t-icon :name="showConfirmPsw ? 'browse' : 'browse-off'" @click="showConfirmPsw = !showConfirmPsw" />
        </template>
      </t-input>
    </t-form-item>

    <t-form-item v-if="captchaEnabled" name="captcha">
      <div v-if="captchaType === 'image'" class="captcha-container">
        <t-input v-model="captchaValue" size="large" placeholder="请输入验证码">
          <template #prefix-icon>
            <t-icon name="verify" />
          </template>
        </t-input>
        <img :src="captchaImage" class="captcha-image" @click="() => loadCaptcha()" />
      </div>
      <div v-else class="captcha-placeholder">
        <t-icon name="verify" />
        <span>点击注册后完成验证</span>
        <t-tag v-if="formData.captcha" theme="success" size="small">已完成验证</t-tag>
      </div>
    </t-form-item>

    <t-form-item class="check-container" name="checked">
      <agreement-check v-model="formData.checked" />
    </t-form-item>

    <t-form-item class="btn-container split-buttons">
      <t-button size="large" theme="default" variant="text" @click="emit('back')">返回登录</t-button>
      <t-button size="large" type="submit" :loading="submitting"> 注册 </t-button>
    </t-form-item>
  </t-form>
  <t-dialog
    v-model:visible="showDragCaptchaDialog"
    header="完成安全验证"
    :footer="false"
    :close-on-overlay-click="false"
    :width="`${dragWidth + 70}px`"
  >
    <drag-captcha
      v-if="showDragCaptchaDialog"
      :key="dragRefreshKey"
      :width="dragWidth"
      :height="dragHeight"
      @success="handleDragSuccess"
      @refresh="handleDragRefresh"
    />
  </t-dialog>
</template>
<script setup lang="ts">
import type { FormRule, SubmitContext } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue';

import { register } from '@/api/auth';
import DragCaptcha from '@/components/DragCaptcha.vue';
import { request } from '@/utils/request';

import AgreementCheck from './AgreementCheck.vue';

const emit = defineEmits(['register-success', 'back']);

const INITIAL_DATA = {
  name: '',
  phone: '',
  email: '',
  idCard: '',
  password: '',
  confirmPassword: '',
  captcha: '',
  checked: false,
};

const form = ref();
const formData = ref({ ...INITIAL_DATA });

const FORM_RULES: Record<string, FormRule[]> = {
  name: [{ required: true, message: '请输入姓名', type: 'error' as const }],
  phone: [
    { required: true, message: '请输入手机号', type: 'error' as const },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', type: 'error' as const },
  ],
  email: [
    { required: true, message: '请输入邮箱', type: 'error' as const },
    { email: true, message: '邮箱格式不正确', type: 'warning' as const },
  ],
  idCard: [{ required: true, message: '请输入身份证号码', type: 'error' as const }],
  password: [
    { required: true, message: '请输入密码', type: 'error' as const },
    { min: 6, max: 64, message: '密码长度应为6-64位', type: 'error' as const },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', type: 'error' as const },
    { validator: (val) => val === formData.value.password, message: '两次密码输入不一致', type: 'error' as const },
  ],
};

const submitting = ref(false);
const showPsw = ref(false);
const showConfirmPsw = ref(false);
const showDragCaptchaDialog = ref(false);
const pendingDragSubmit = ref(false);
const sanitizeCaptcha = (value: string) => value.replace(/[^a-z0-9]/gi, '');
const captchaValue = computed({
  get: () => formData.value.captcha,
  set: (value) => {
    formData.value.captcha = sanitizeCaptcha(String(value ?? ''));
  },
});

const captchaId = ref('');
const captchaImage = ref('');
const captchaEnabled = ref(true);
const captchaType = ref<'image' | 'drag'>('image');
const dragWidth = ref(310);
const dragHeight = ref(155);
const dragRefreshKey = ref(0);

const CAPTCHA_REFRESH_FALLBACK_MS = 120 * 1000;
const CAPTCHA_REFRESH_LEEWAY_MS = 5 * 1000;
let captchaRefreshTimer: number | null = null;

const clearCaptchaRefreshTimer = () => {
  if (captchaRefreshTimer) {
    window.clearTimeout(captchaRefreshTimer);
    captchaRefreshTimer = null;
  }
};

const scheduleCaptchaRefresh = (expiresInSeconds?: number) => {
  clearCaptchaRefreshTimer();
  const seconds = typeof expiresInSeconds === 'number' && Number.isFinite(expiresInSeconds) ? expiresInSeconds : null;
  const ttl = (seconds != null ? seconds * 1000 : CAPTCHA_REFRESH_FALLBACK_MS) - CAPTCHA_REFRESH_LEEWAY_MS;
  const delay = Math.max(ttl, 10 * 1000);
  captchaRefreshTimer = window.setTimeout(() => {
    captchaRefreshTimer = null;
    void loadCaptcha({ silent: true });
  }, delay);
};

const bumpDragCaptcha = () => {
  dragRefreshKey.value += 1;
};

const handleDragSuccess = (payload: { captchaVerification: string; token: string }) => {
  formData.value.captcha = payload.captchaVerification;
  captchaId.value = payload.token;
  showDragCaptchaDialog.value = false;
  if (pendingDragSubmit.value) {
    pendingDragSubmit.value = false;
    nextTick(() => {
      form.value?.submit?.();
    });
  }
};

const handleDragRefresh = () => {
  formData.value.captcha = '';
  captchaId.value = '';
};

const openDragCaptcha = () => {
  if (captchaType.value !== 'drag') return;
  bumpDragCaptcha();
  showDragCaptchaDialog.value = true;
};
const loadCaptcha = async (opts?: { silent?: boolean }) => {
  try {
    const res = await request.get<{
      id: string;
      image: string;
      expiresIn?: number;
      type?: 'image' | 'drag';
      width?: number;
      height?: number;
      enabled?: boolean;
    }>({ url: '/auth/captcha' }, { isTransformResponse: true, withToken: false });

    captchaEnabled.value = res.enabled !== false;
    if (!captchaEnabled.value) {
      formData.value.captcha = '';
      return;
    }

    captchaType.value = res.type || 'image';
    if (captchaType.value === 'image') {
      captchaId.value = res.id;
      captchaImage.value = res.image;
    } else {
      captchaId.value = '';
      captchaImage.value = '';
      bumpDragCaptcha();
    }
    dragWidth.value = res.width || 310;
    dragHeight.value = res.height || 155;
    formData.value.captcha = '';
    scheduleCaptchaRefresh(res.expiresIn);
  } catch (err: any) {
    scheduleCaptchaRefresh();
    if (!opts?.silent) {
      MessagePlugin.error(String(err?.message || '验证码加载失败'));
    }
  }
};

onMounted(() => {
  loadCaptcha();
});

onBeforeUnmount(() => {
  clearCaptchaRefreshTimer();
});

const formRules = computed(() => ({
  ...FORM_RULES,
  captcha:
    captchaEnabled.value && captchaType.value === 'image'
      ? [{ required: true, message: '请输入验证码', type: 'error' as const }]
      : [],
}));

const buildRegisterPayload = () => {
  const account = formData.value.phone;
  return {
    account: account?.trim() || '',
    password: formData.value.password,
    confirmPassword: formData.value.confirmPassword,
    name: formData.value.name?.trim() || '',
    email: formData.value.email?.trim() || '',
    idCard: formData.value.idCard?.trim() || '',
    mobile: formData.value.phone?.trim() || '',
    captchaId: captchaId.value,
    captchaCode: formData.value.captcha,
  };
};

const normalizeRegisterErrorMessage = (message: string) => {
  const cleaned = String(message || '').replace(/\s*\[\d{3}\]\s*$/, '').trim();
  if (!cleaned) return '';
  if (!/^参数校验失败[:：]/.test(cleaned)) return cleaned;
  const raw = cleaned.replace(/^参数校验失败[:：]\s*/, '');
  const parts = raw.split(/[;；]/).map((item) => item.trim()).filter(Boolean);
  const mapped = parts.map((item) => {
    const lower = item.toLowerCase();
    if (lower.includes('id card') && lower.includes('required')) return '身份证号码不能为空';
    if (lower.includes('password') && lower.includes('6-64')) return '密码长度应为6-64位';
    return item;
  });
  return mapped.join('；');
};

const onSubmit = async (ctx: SubmitContext) => {
  if (ctx.validateResult !== true) {
    MessagePlugin.error(ctx.firstError || '请完善注册信息');
    return;
  }
  if (!formData.value.checked) {
    MessagePlugin.error('注册前请同意用户协议和隐私协议');
    return;
  }

  const payload = buildRegisterPayload();
  if (!payload.account) {
    MessagePlugin.error('请输入手机号');
    return;
  }
  if (captchaEnabled.value && captchaType.value === 'drag' && !formData.value.captcha) {
    pendingDragSubmit.value = true;
    openDragCaptcha();
    return;
  }

  submitting.value = true;
  try {
    await register(payload);
    MessagePlugin.success('注册成功，请使用账号登录');
    emit('register-success');
    form.value.reset();
    loadCaptcha();
  } catch (err: any) {
    const normalized = normalizeRegisterErrorMessage(err?.message);
    MessagePlugin.error(normalized || '注册失败');
    loadCaptcha();
  } finally {
    submitting.value = false;
  }
};
</script>
<style lang="less" scoped>
@import '../index.less';

.captcha-container {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;

  :deep(.t-input) {
    flex: 1;
    width: auto;
  }
}

.captcha-image {
  width: 120px;
  height: 40px;
  cursor: pointer;
  border-radius: var(--td-radius-default);
  flex-shrink: 0;
}

.captcha-placeholder {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 0 12px;
  height: 40px;
  border: 1px dashed var(--td-component-stroke);
  border-radius: var(--td-radius-default);
  color: var(--td-text-color-secondary);
}

.agreement-link {
  margin: 0 4px;
}

.agreement-content {
  max-height: 60vh;
  overflow: auto;
  padding: 4px 8px;
  color: var(--td-text-color-primary);
  line-height: 1.6;

  :deep(.agreement-empty) {
    color: var(--td-text-color-placeholder);
  }
}
</style>
