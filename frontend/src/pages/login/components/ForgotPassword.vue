<template>
  <t-form
    ref="form"
    class="item-container"
    :data="formData"
    :rules="FORM_RULES"
    :show-error-message="false"
    label-width="0"
    @submit="onSubmit"
  >
    <t-form-item name="account">
      <t-input v-model="accountValue" size="large" placeholder="请输入账号">
        <template #prefix-icon>
          <t-icon name="user" />
        </template>
      </t-input>
    </t-form-item>

    <t-form-item name="phone">
      <t-input v-model="phoneValue" size="large" placeholder="请输入手机号（不支持空格）">
        <template #prefix-icon>
          <t-icon name="mobile" />
        </template>
      </t-input>
    </t-form-item>

    <t-form-item class="verification-code" name="verifyCode">
      <t-input v-model="verifyCodeValue" size="large" placeholder="请输入验证码" />
      <t-button size="large" variant="outline" :disabled="countDown > 0" @click="sendCode">
        {{ countDown === 0 ? '发送验证码' : `${countDown}秒后可重发` }}
      </t-button>
    </t-form-item>

    <t-form-item name="newPassword">
      <t-input v-model="newPasswordValue" size="large" type="password" clearable placeholder="请输入新密码">
        <template #prefix-icon>
          <t-icon name="lock-on" />
        </template>
      </t-input>
    </t-form-item>

    <t-form-item name="confirmPassword">
      <t-input v-model="confirmPasswordValue" size="large" type="password" clearable placeholder="请确认新密码">
        <template #prefix-icon>
          <t-icon name="lock-on" />
        </template>
      </t-input>
    </t-form-item>

    <t-form-item class="check-container" name="checked">
      <agreement-check v-model="formData.checked" />
    </t-form-item>

    <t-form-item class="btn-container split-buttons">
      <t-button size="large" theme="default" variant="text" @click="emit('back')">返回登录</t-button>
      <t-button size="large" type="submit" :loading="submitting"> 找回密码 </t-button>
    </t-form-item>
  </t-form>
</template>
<script setup lang="ts">
import type { FormInstanceFunctions, FormRule, SubmitContext } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, ref } from 'vue';

import { resetPassword, sendSmsCode } from '@/api/auth';
import { useCounter } from '@/hooks';

import AgreementCheck from './AgreementCheck.vue';

const emit = defineEmits(['back', 'reset-success']);
const form = ref<FormInstanceFunctions>();
const submitting = ref(false);

const formData = ref({
  account: '',
  phone: '',
  verifyCode: '',
  newPassword: '',
  confirmPassword: '',
  checked: false,
});

const sanitizeTrim = (value: string) => String(value ?? '').trim();
const sanitizeNoSpace = (value: string) => String(value ?? '').replace(/\s+/g, '');
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
const verifyCodeValue = computed({
  get: () => formData.value.verifyCode,
  set: (value) => {
    formData.value.verifyCode = sanitizeNoSpace(String(value ?? ''));
  },
});
const newPasswordValue = computed({
  get: () => formData.value.newPassword,
  set: (value) => {
    formData.value.newPassword = sanitizeTrim(String(value ?? ''));
  },
});
const confirmPasswordValue = computed({
  get: () => formData.value.confirmPassword,
  set: (value) => {
    formData.value.confirmPassword = sanitizeTrim(String(value ?? ''));
  },
});

const FORM_RULES: Record<string, FormRule[]> = {
  account: [{ required: true, message: '账号必填', type: 'error' }],
  phone: [
    { required: true, message: '请输入手机号（不支持空格）', type: 'error' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确（不支持空格）', type: 'error' },
  ],
  verifyCode: [{ required: true, message: '验证码必填', type: 'error' }],
  newPassword: [
    { required: true, message: '请输入新密码', type: 'error' },
    { min: 6, message: '至少 6 位字符', type: 'error' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', type: 'error' },
    {
      validator: (val) => val === formData.value.newPassword,
      message: '两次输入不一致',
      type: 'error',
    },
  ],
};

const [countDown, handleCounter] = useCounter();

const sendCode = async () => {
  const res = await form.value?.validate({ fields: ['phone'] });
  if (res !== true) {
    MessagePlugin.error('请输入正确手机号');
    return;
  }
  try {
    await sendSmsCode({ phone: formData.value.phone });
    handleCounter();
    MessagePlugin.success('验证码已发送');
  } catch (err: any) {
    MessagePlugin.error(String(err?.message || '验证码发送失败'));
  }
};

const onSubmit = async (ctx: SubmitContext) => {
  if (ctx.validateResult !== true) {
    MessagePlugin.error(ctx.firstError || '请完善找回信息');
    return;
  }
  if (!formData.value.checked) {
    MessagePlugin.error('请先同意相关条款');
    return;
  }
  submitting.value = true;
  try {
    await resetPassword({
      account: formData.value.account,
      phone: formData.value.phone,
      code: formData.value.verifyCode,
      newPassword: formData.value.newPassword,
      confirmPassword: formData.value.confirmPassword,
    });
    MessagePlugin.success('密码已重置，请使用新密码登录');
    emit('reset-success');
    form.value?.reset();
  } catch (err: any) {
    MessagePlugin.error(String(err?.message || '重置失败'));
  } finally {
    submitting.value = false;
  }
};
</script>
<style lang="less" scoped>
@import '../index.less';

.verification-code {
  :deep(.t-input) {
    flex: 1;
  }

  :deep(.t-button) {
    margin-left: 8px;
  }
}
</style>
