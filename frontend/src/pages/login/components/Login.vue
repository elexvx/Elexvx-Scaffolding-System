<template>
  <t-form
    ref="form"
    class="item-container"
    :class="[`login-${type}`]"
    :data="formData"
    :rules="formRules"
    :show-error-message="false"
    label-width="0"
    @submit="onSubmit"
  >
    <t-tabs v-model="type" class="login-tabs">
      <t-tab-panel
        v-if="settingStore.smsEnabled"
        value="phone"
        :label="t('pages.login.phoneLogin')"
        :destroy-on-hide="false"
      />
      <t-tab-panel
        v-if="settingStore.emailEnabled"
        value="email"
        :label="t('pages.login.emailLogin')"
        :destroy-on-hide="false"
      />
      <t-tab-panel value="password" :label="t('pages.login.accountLogin')" :destroy-on-hide="false" />
    </t-tabs>
    <template v-if="type === 'password'">
      <t-form-item name="account">
        <t-input v-model="accountValue" size="large" :placeholder="t('pages.login.input.account')">
          <template #prefix-icon>
            <t-icon name="user" />
          </template>
        </t-input>
      </t-form-item>

      <t-form-item name="password">
        <t-input
          v-model="passwordValue"
          size="large"
          :type="showPsw ? 'text' : 'password'"
          clearable
          :placeholder="t('pages.login.input.password')"
        >
          <template #prefix-icon>
            <t-icon name="lock-on" />
          </template>
          <template #suffix-icon>
            <t-icon :name="showPsw ? 'browse' : 'browse-off'" @click="showPsw = !showPsw" />
          </template>
        </t-input>
      </t-form-item>

      <t-form-item v-if="captchaEnabled" name="captcha">
        <div v-if="captchaType === 'image'" class="captcha-container">
          <t-input v-model="captchaValue" size="large" :placeholder="t('pages.login.required.verification')">
            <template #prefix-icon>
              <t-icon name="verify" />
            </template>
          </t-input>
          <img :src="captchaImage" class="captcha-image" @click="() => loadCaptcha()" />
        </div>
        <div v-else class="captcha-placeholder">
          <t-icon name="verify" />
          <span>点击登录后完成验证</span>
          <t-tag v-if="formData.captcha" theme="success" size="small">已完成验证</t-tag>
        </div>
      </t-form-item>
    </template>

    <!-- 手机号登录 -->
    <template v-if="type === 'phone'">
      <t-form-item name="phone">
        <t-input v-model="phoneValue" size="large" :placeholder="t('pages.login.input.phone')">
          <template #prefix-icon>
            <t-icon name="mobile" />
          </template>
        </t-input>
      </t-form-item>

      <t-form-item class="verification-code" name="verifyCode">
        <t-input v-model="verifyCodeValue" size="large" :placeholder="t('pages.login.input.verification')" />
        <t-button size="large" variant="outline" :disabled="countDown > 0" @click="sendCode">
          {{ countDown === 0 ? t('pages.login.sendVerification') : `${countDown}秒后可重发` }}
        </t-button>
      </t-form-item>
    </template>

    <!-- 邮箱登录 -->
    <template v-if="type === 'email'">
      <t-form-item name="email">
        <t-input v-model="emailValue" size="large" :placeholder="t('pages.login.input.email')">
          <template #prefix-icon>
            <t-icon name="mail" />
          </template>
        </t-input>
      </t-form-item>

      <t-form-item class="verification-code" name="verifyCode">
        <t-input v-model="verifyCodeValue" size="large" :placeholder="t('pages.login.input.verification')" />
        <t-button size="large" variant="outline" :disabled="countDown > 0" @click="sendCode">
          {{ countDown === 0 ? t('pages.login.sendVerification') : t('pages.login.resendAfter', [countDown]) }}
        </t-button>
      </t-form-item>
    </template>

    <!-- Protocol -->
    <t-form-item class="check-container" name="agreed">
      <agreement-check v-model="formData.agreed" />
    </t-form-item>

    <!-- Remember Account -->
    <t-form-item v-if="type === 'password'" class="check-container" name="checked">
      <t-checkbox v-model="formData.checked">{{ t('pages.login.remember') }}</t-checkbox>
    </t-form-item>

    <t-form-item class="btn-container">
      <t-button block size="large" type="submit"> {{ t('pages.login.signIn') }} </t-button>
    </t-form-item>

    <div class="switch-container">
      <span class="tip" @click="emit('register')">{{ t('pages.login.createAccount') }}</span>
      <span class="tip" @click="emit('forgot')">{{ t('pages.login.forget') }}</span>
    </div>
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
import type { FormInstanceFunctions, FormRule, SubmitContext } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, nextTick, onActivated, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import DragCaptcha from '@/components/DragCaptcha.vue';
import { useCounter } from '@/hooks';
import { t } from '@/locales';
import { useSettingStore, useUserStore } from '@/store';
import { request } from '@/utils/request';
import { setTokenExpireTimer } from '@/utils/tokenExpire';

import AgreementCheck from './AgreementCheck.vue';

const emit = defineEmits(['register', 'forgot']);
const userStore = useUserStore();
const settingStore = useSettingStore();
const INITIAL_DATA = {
  phone: '',
  email: '',
  account: '',
  password: '',
  verifyCode: '',
  checked: false,
  captcha: '',
  agreed: false,
};

const FORM_RULES: Record<string, FormRule[]> = {
  phone: [{ required: true, message: t('pages.login.required.phone'), type: 'error' as const }],
  email: [
    { required: true, message: t('pages.login.required.email'), type: 'error' as const },
    { email: true, message: t('pages.login.invalid.email'), type: 'error' as const },
  ],
  account: [
    { required: true, message: t('pages.login.required.account'), type: 'error' as const },
    { validator: (val) => /^[\w@.-]+$/.test(val), message: t('pages.login.invalid.account'), type: 'error' as const },
  ],
  password: [{ required: true, message: t('pages.login.required.password'), type: 'error' as const }],
  verifyCode: [{ required: true, message: t('pages.login.required.verification'), type: 'error' as const }],
  agreed: [{ validator: (val) => Boolean(val), message: t('pages.login.agreeTerms'), type: 'error' as const }],
};

const type = ref('password');

const form = ref<FormInstanceFunctions>();
const formData = ref({ ...INITIAL_DATA });
const showPsw = ref(false);
const showDragCaptchaDialog = ref(false);
const pendingDragSubmit = ref(false);

const sanitizeTrim = (value: string) => String(value ?? '').trim();
const sanitizeNoSpace = (value: string) => String(value ?? '').replace(/\s+/g, '');
const sanitizeCaptcha = (value: string) => value.replace(/[^a-z0-9]/gi, '');
const accountValue = computed({
  get: () => formData.value.account,
  set: (value) => {
    formData.value.account = sanitizeTrim(String(value ?? ''));
  },
});
const phoneValue = computed({
  get: () => formData.value.phone,
  set: (value) => {
    formData.value.phone = sanitizeNoSpace(String(value ?? ''));
  },
});
const emailValue = computed({
  get: () => formData.value.email,
  set: (value) => {
    formData.value.email = sanitizeNoSpace(String(value ?? ''));
  },
});
const passwordValue = computed({
  get: () => formData.value.password,
  set: (value) => {
    formData.value.password = sanitizeTrim(String(value ?? ''));
  },
});
const verifyCodeValue = computed({
  get: () => formData.value.verifyCode,
  set: (value) => {
    formData.value.verifyCode = sanitizeNoSpace(String(value ?? ''));
  },
});
const captchaValue = computed({
  get: () => formData.value.captcha,
  set: (value) => {
    formData.value.captcha = sanitizeCaptcha(String(value ?? ''));
  },
});

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
  if (type.value !== 'password') return;
  const seconds = typeof expiresInSeconds === 'number' && Number.isFinite(expiresInSeconds) ? expiresInSeconds : null;
  const ttl = (seconds != null ? seconds * 1000 : CAPTCHA_REFRESH_FALLBACK_MS) - CAPTCHA_REFRESH_LEEWAY_MS;
  const delay = Math.max(ttl, 10 * 1000);
  captchaRefreshTimer = window.setTimeout(() => {
    captchaRefreshTimer = null;
    if (type.value === 'password') {
      void loadCaptcha({ silent: true });
    }
  }, delay);
};

const [countDown, handleCounter] = useCounter();

const resolveLoginType = () => {
  if (type.value === 'phone' && !settingStore.smsEnabled) return;
  if (type.value === 'email' && !settingStore.emailEnabled) return;
  return type.value;
};

const getDefaultLoginType = () => {
  if (settingStore.smsEnabled) return 'phone';
  if (settingStore.emailEnabled) return 'email';
  return 'password';
};

const syncLoginType = () => {
  if (!resolveLoginType()) {
    type.value = getDefaultLoginType();
  }
};

watch(
  () => [settingStore.smsEnabled, settingStore.emailEnabled],
  () => {
    syncLoginType();
  },
  { immediate: true },
);

watch(
  type,
  (val) => {
    if (val === 'password') loadCaptcha();
    else clearCaptchaRefreshTimer();
  },
  { immediate: false },
);

const router = useRouter();
const route = useRoute();

const resolveLoginRedirect = (redirect?: string) => {
  const fallbackHome = settingStore.defaultHome || '/user/index';
  if (!redirect) return fallbackHome;
  const decoded = decodeURIComponent(redirect);
  if ((decoded === '/' || decoded === '/user/index') && fallbackHome !== '/user/index') {
    return fallbackHome;
  }
  return decoded;
};

/**
 * 发送验证码
 */
const sendCode = async () => {
  const isPhone = type.value === 'phone';
  const isEmail = type.value === 'email';
  if (!isPhone && !isEmail) return;

  const field = isPhone ? 'phone' : 'email';
  const res = await form.value.validate({ fields: [field] });
  if (res !== true) {
    MessagePlugin.error(isPhone ? t('pages.login.invalid.phone') : t('pages.login.invalid.email'));
    return;
  }
  try {
    if (isPhone) {
      await request.post({ url: '/auth/sms/send', data: { phone: formData.value.phone } }, { withToken: false });
    } else {
      await request.post({ url: '/auth/email/send', data: { email: formData.value.email } }, { withToken: false });
    }
    MessagePlugin.success(t('pages.login.verificationSent'));
    handleCounter();
  } catch (err: any) {
    MessagePlugin.error(String(err?.message || t('pages.login.sendingFailed')));
  }
};

const captchaId = ref('');
const captchaImage = ref('');
const captchaEnabled = ref(true);
const captchaType = ref<'image' | 'drag'>('image');
const dragWidth = ref(310);
const dragHeight = ref(155);
const dragRefreshKey = ref(0);

const formRules = computed(() => ({
  ...FORM_RULES,
  captcha:
    captchaEnabled.value && captchaType.value === 'image'
      ? [{ required: true, message: t('pages.login.required.verification'), type: 'error' as const }]
      : [],
}));

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
      MessagePlugin.error(String(err?.message || '获取验证码失败'));
    }
  }
};
onMounted(() => {
  loadCaptcha();
  const savedAccount = localStorage.getItem('remember_account');
  if (savedAccount && savedAccount !== 'admin') {
    formData.value.account = sanitizeTrim(savedAccount);
    formData.value.checked = true;
  } else if (savedAccount === 'admin') {
    localStorage.removeItem('remember_account');
  }
});
onActivated(() => {
  if (type.value === 'password') loadCaptcha();
});
onBeforeUnmount(() => {
  clearCaptchaRefreshTimer();
});

const onSubmit = async (ctx: SubmitContext) => {
  if (ctx.validateResult !== true) {
    MessagePlugin.error(ctx.firstError || '请完善登录信息');
    return;
  }
  try {
    if (type.value === 'phone') {
      const res = await userStore.loginBySms({ phone: formData.value.phone, code: formData.value.verifyCode });
      if (res?.status === 'ok') {
        // 设置 token 过期定时器
        if (res.token && res.expiresIn) {
          setTokenExpireTimer(res.token, res.expiresIn);
        }
        await settingStore.loadUiSetting();
        MessagePlugin.success(t('pages.login.loginSuccess'));
        const redirectUrl = resolveLoginRedirect(route.query.redirect as string);
        router.push(redirectUrl);
        return;
      }
      throw new Error(t('pages.login.loginFailed'));
    }

    if (type.value === 'email') {
      const res = await userStore.loginByEmail({ email: formData.value.email, code: formData.value.verifyCode });
      if (res?.status === 'ok') {
        // 设置 token 过期定时器
        if (res.token && res.expiresIn) {
          setTokenExpireTimer(res.token, res.expiresIn);
        }
        await settingStore.loadUiSetting();
        MessagePlugin.success(t('pages.login.loginSuccess'));
        const redirectUrl = resolveLoginRedirect(route.query.redirect as string);
        router.push(redirectUrl);
        return;
      }
      throw new Error(t('pages.login.loginFailed'));
    }

    if (captchaEnabled.value && captchaType.value === 'drag' && !formData.value.captcha) {
      pendingDragSubmit.value = true;
      openDragCaptcha();
      return;
    }
    const payload = { ...formData.value, captchaId: captchaId.value, captchaCode: formData.value.captcha };
    const res = await userStore.login(payload);
    if (res?.status === 'ok') {
      // 设置 token 过期定时器
      if (res.token && res.expiresIn) {
        setTokenExpireTimer(res.token, res.expiresIn);
      }
      await settingStore.loadUiSetting();
      MessagePlugin.success(t('pages.login.loginSuccess'));
      if (formData.value.checked) {
        localStorage.setItem('remember_account', formData.value.account);
      } else {
        localStorage.removeItem('remember_account');
      }
      const redirectUrl = resolveLoginRedirect(route.query.redirect as string);
      router.push(redirectUrl);
      return;
    }
    throw new Error(t('pages.login.loginFailed'));
  } catch (e: any) {
    MessagePlugin.error(String(e?.message || t('pages.login.loginFailed')));
    loadCaptcha();
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

.captcha-image {
  width: 120px;
  height: 40px;
  cursor: pointer;
  border-radius: var(--td-radius-default);
  flex-shrink: 0;
}

.login-tabs {
  margin-bottom: 12px;
}
</style>
