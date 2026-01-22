<template>
  <div class="security-setting">
    <t-card title="安全设置" :bordered="false">
      <t-tabs v-model="activeTab">
        <t-tab-panel value="token" label="Token 策略" :destroy-on-hide="false">
          <div class="security-content">
            <t-form :data="form" layout="vertical" label-align="right" label-width="140px" @submit.prevent>
              <!-- 统一的表单行间距与注释预留 -->
              <t-row :gutter="[24, 16]">
                <t-col :xs="24" :sm="12">
                  <t-form-item label="会话有效期（分钟）" help="用户登录后会话保持有效的时长">
                    <t-input-number
                      v-model="form.sessionTimeoutMinutes"
                      :min="1"
                      :step="30"
                      style="max-width: 500px; width: 100%"
                    />
                  </t-form-item>
                </t-col>

                <t-col :xs="24" :sm="12">
                  <t-form-item label="Token 有效期（分钟）" help="Access Token 的有效时间">
                    <t-input-number
                      v-model="form.tokenTimeoutMinutes"
                      :min="1"
                      :step="30"
                      style="max-width: 500px; width: 100%"
                    />
                  </t-form-item>
                </t-col>

                <t-col :xs="24" :sm="12">
                  <t-form-item label="过期刷新时限（分钟）" help="Token 过期后允许刷新的宽限期">
                    <t-input-number
                      v-model="form.tokenRefreshGraceMinutes"
                      :min="1"
                      :step="10"
                      style="max-width: 500px; width: 100%"
                    />
                  </t-form-item>
                </t-col>

                <t-col :span="24">
                  <t-form-item>
                    <t-button theme="primary" :loading="saving" @click="saveSettings">保存设置</t-button>
                  </t-form-item>
                </t-col>
              </t-row>
            </t-form>
          </div>
        </t-tab-panel>

        <t-tab-panel value="captcha" label="验证码设置" :destroy-on-hide="false">
          <div class="security-content">
            <t-form :data="form" layout="vertical" label-align="right" label-width="140px" @submit.prevent>
              <t-row :gutter="[24, 16]">
                <t-col :span="24">
                  <t-form-item label="启用人机验证码" help="开启后用户在登录、注册等环节需要完成验证码验证">
                    <t-switch v-model="form.captchaEnabled" @change="handleCaptchaEnabledChange" />
                  </t-form-item>
                </t-col>

                <t-col :xs="24" :sm="12">
                  <t-form-item label="验证码类型" help="选择系统使用的验证码方式">
                    <t-radio-group v-model="form.captchaType" :disabled="!form.captchaEnabled">
                      <t-radio-button value="drag">图形拖动验证码</t-radio-button>
                      <t-radio-button value="image">图片验证码</t-radio-button>
                    </t-radio-group>
                  </t-form-item>
                </t-col>

                <template v-if="form.captchaType === 'drag'">
                  <t-col :xs="24" :sm="12">
                    <t-form-item label="拖动验证码宽度（px）" help="拖拽图片验证码的显示宽度">
                      <t-input-number
                        v-model="form.dragCaptchaWidth"
                        :min="200"
                        :step="10"
                        :disabled="!form.captchaEnabled"
                        style="max-width: 500px; width: 100%"
                      />
                    </t-form-item>
                  </t-col>
                  <t-col :xs="24" :sm="12">
                    <t-form-item label="拖动验证码高度（px）" help="拖拽图片验证码的显示高度">
                      <t-input-number
                        v-model="form.dragCaptchaHeight"
                        :min="100"
                        :step="10"
                        :disabled="!form.captchaEnabled"
                        style="max-width: 500px; width: 100%"
                      />
                    </t-form-item>
                  </t-col>
                  <t-col :xs="24" :sm="12">
                    <t-form-item label="拖动通过阈值（%）" help="用户拖动误差允许的范围百分比">
                      <t-input-number
                        v-model="form.dragCaptchaThreshold"
                        :min="70"
                        :max="100"
                        :step="1"
                        :disabled="!form.captchaEnabled"
                        style="max-width: 500px; width: 100%"
                      />
                    </t-form-item>
                  </t-col>
                </template>

                <template v-else>
                  <t-col :xs="24" :sm="12">
                    <t-form-item label="验证码长度" help="图片验证码的字符数量">
                      <t-input-number
                        v-model="form.imageCaptchaLength"
                        :min="4"
                        :max="8"
                        :disabled="!form.captchaEnabled"
                        style="max-width: 500px; width: 100%"
                      />
                    </t-form-item>
                  </t-col>
                  <t-col :xs="24" :sm="12">
                    <t-form-item label="干扰线条数" help="图片验证码中的干扰线数量">
                      <t-input-number
                        v-model="form.imageCaptchaNoiseLines"
                        :min="0"
                        :max="12"
                        :disabled="!form.captchaEnabled"
                        style="max-width: 500px; width: 100%"
                      />
                    </t-form-item>
                  </t-col>
                </template>

                <t-col :span="24">
                  <t-form-item>
                    <t-button theme="primary" :loading="saving" @click="saveSettings">保存设置</t-button>
                  </t-form-item>
                </t-col>
              </t-row>
            </t-form>
          </div>

          <!-- 关闭验证码确认弹窗 -->
          <t-dialog
            v-model:visible="disableConfirmVisible"
            header="确认关闭人机验证码"
            :confirm-btn="{ content: '我已知晓，关闭', theme: 'danger' }"
            :cancel-btn="{ content: '保持开启' }"
            @confirm="confirmDisableCaptcha"
            @cancel="cancelDisableCaptcha"
          >
            <div style="display: flex; flex-direction: column; gap: 16px; padding-top: 8px">
              <t-alert theme="error" variant="outline">
                <template #message>
                  <span style="font-weight: 600">关闭验证码可能导致严重的安全隐患</span>
                </template>
              </t-alert>
              <div
                style="
                  background: var(--td-bg-color-container-hover);
                  padding: 12px 16px;
                  border-radius: var(--td-radius-default);
                "
              >
                <div style="margin-bottom: 8px; color: var(--td-text-color-secondary)">潜在风险包括：</div>
                <ul style="padding-left: 20px; margin: 0; line-height: 1.8; color: var(--td-text-color-primary)">
                  <li>系统更易遭受<strong>自动化暴力破解</strong>攻击</li>
                  <li>恶意用户可能<strong>批量注册</strong>垃圾账号</li>
                  <li>API 接口可能被<strong>滥用刷量</strong>，导致服务不稳定</li>
                </ul>
              </div>
            </div>
          </t-dialog>
        </t-tab-panel>

        <t-tab-panel value="defense" label="防御阈值" :destroy-on-hide="false">
          <div class="security-content">
            <t-form :data="form" layout="vertical" label-align="right" label-width="140px" @submit.prevent>
              <t-row :gutter="[24, 16]">
                <t-col :xs="24" :sm="12">
                  <t-form-item label="统计窗口（分钟）" help="用于统计高频访问的时间窗口大小">
                    <t-input-number
                      v-model="form.defenseWindowMinutes"
                      :min="1"
                      :step="1"
                      style="max-width: 500px; width: 100%"
                    />
                  </t-form-item>
                </t-col>
                <t-col :xs="24" :sm="12">
                  <t-form-item label="最大验证次数" help="统计窗口内允许的最大验证请求次数（超过将拦截）">
                    <t-input-number
                      v-model="form.defenseMaxRequests"
                      :min="10"
                      :step="10"
                      style="max-width: 500px; width: 100%"
                    />
                  </t-form-item>
                </t-col>
                <t-col :xs="24" :sm="12">
                  <t-form-item label="最大错误次数" help="统计窗口内允许的最大验证失败次数（超过将拦截）">
                    <t-input-number
                      v-model="form.defenseMaxErrors"
                      :min="3"
                      :step="1"
                      style="max-width: 500px; width: 100%"
                    />
                  </t-form-item>
                </t-col>
                <t-col :span="24">
                  <t-form-item>
                    <t-button theme="primary" :loading="saving" @click="saveSettings">保存设置</t-button>
                  </t-form-item>
                </t-col>
              </t-row>
            </t-form>
          </div>
        </t-tab-panel>

        <t-tab-panel value="password" label="密码规范设置" :destroy-on-hide="false">
          <div class="security-content">
            <t-form :data="form" layout="vertical" label-align="right" label-width="140px" @submit="saveSettings">
              <t-row :gutter="[24, 16]">
                <t-col :xs="24" :sm="12">
                  <t-form-item label="最短长度" help="用户密码允许的最少字符数">
                    <t-input-number
                      v-model="form.passwordMinLength"
                      :min="6"
                      :max="32"
                      style="max-width: 500px; width: 100%"
                    />
                  </t-form-item>
                </t-col>
                <t-col :xs="24" :sm="12">
                  <t-form-item label="必须包含大写字母" help="强制密码中必须包含大写字母 (A-Z)">
                    <t-switch v-model="form.passwordRequireUppercase" />
                  </t-form-item>
                </t-col>
                <t-col :xs="24" :sm="12">
                  <t-form-item label="必须包含小写字母" help="强制密码中必须包含小写字母 (a-z)">
                    <t-switch v-model="form.passwordRequireLowercase" />
                  </t-form-item>
                </t-col>
                <t-col :xs="24" :sm="12">
                  <t-form-item label="必须包含特殊字符" help="强制密码中必须包含特殊字符 (!@#$%^&*)">
                    <t-switch v-model="form.passwordRequireSpecial" />
                  </t-form-item>
                </t-col>
                <t-col :xs="24" :sm="12">
                  <t-form-item label="允许连续字符" help="是否允许密码中包含连续字符 (如 123, abc)">
                    <t-switch v-model="form.passwordAllowSequential" />
                  </t-form-item>
                </t-col>
                <t-col :span="24">
                  <t-form-item>
                    <t-button theme="primary" :loading="saving" @click="saveSettings">保存设置</t-button>
                  </t-form-item>
                </t-col>
              </t-row>
            </t-form>
          </div>
        </t-tab-panel>
      </t-tabs>
    </t-card>
  </div>
</template>
<script setup lang="ts">
import { MessagePlugin } from 'tdesign-vue-next';
import { onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { request } from '@/utils/request';

type CaptchaType = 'drag' | 'image';

interface SecuritySettingsForm {
  sessionTimeoutMinutes: number;
  tokenTimeoutMinutes: number;
  tokenRefreshGraceMinutes: number;
  captchaEnabled: boolean;
  captchaType: CaptchaType;
  dragCaptchaWidth: number;
  dragCaptchaHeight: number;
  dragCaptchaThreshold: number;
  imageCaptchaLength: number;
  imageCaptchaNoiseLines: number;
  passwordMinLength: number;
  passwordRequireUppercase: boolean;
  passwordRequireLowercase: boolean;
  passwordRequireSpecial: boolean;
  passwordAllowSequential: boolean;
  defenseWindowMinutes: number;
  defenseMaxRequests: number;
  defenseMaxErrors: number;
}

const route = useRoute();
const router = useRouter();
const activeTab = ref<'token' | 'captcha' | 'password' | 'defense'>(
  (route.query.tab as 'token' | 'captcha' | 'password' | 'defense') || 'token',
);

watch(activeTab, (val) => {
  router.replace({ query: { ...route.query, tab: val } });
});

const saving = ref(false);
const disableConfirmVisible = ref(false);

const form = ref<SecuritySettingsForm>({
  sessionTimeoutMinutes: 1440,
  tokenTimeoutMinutes: 1440,
  tokenRefreshGraceMinutes: 60,
  captchaEnabled: true,
  captchaType: 'image',
  dragCaptchaWidth: 280,
  dragCaptchaHeight: 160,
  dragCaptchaThreshold: 98,
  imageCaptchaLength: 5,
  imageCaptchaNoiseLines: 8,
  passwordMinLength: 6,
  passwordRequireUppercase: false,
  passwordRequireLowercase: false,
  passwordRequireSpecial: false,
  passwordAllowSequential: true,
  defenseWindowMinutes: 5,
  defenseMaxRequests: 100,
  defenseMaxErrors: 10,
});

const handleCaptchaEnabledChange = (value: string | number | boolean) => {
  if (!value) {
    // 用户尝试关闭，先恢复为开启状态，弹窗确认
    form.value.captchaEnabled = true;
    disableConfirmVisible.value = true;
  }
};

const confirmDisableCaptcha = () => {
  form.value.captchaEnabled = false;
  disableConfirmVisible.value = false;
};

const cancelDisableCaptcha = () => {
  disableConfirmVisible.value = false;
};

const loadSettings = async () => {
  // 使用 /system/ui 统一承载安全设置字段，避免多套配置入口
  const data = await request.get<any>({ url: '/system/ui' });
  if (!data) return;
  form.value = {
    sessionTimeoutMinutes: data.sessionTimeoutMinutes ?? form.value.sessionTimeoutMinutes,
    tokenTimeoutMinutes: data.tokenTimeoutMinutes ?? form.value.tokenTimeoutMinutes,
    tokenRefreshGraceMinutes: data.tokenRefreshGraceMinutes ?? form.value.tokenRefreshGraceMinutes,
    captchaEnabled: data.captchaEnabled ?? form.value.captchaEnabled,
    captchaType: (data.captchaType as CaptchaType) || form.value.captchaType,
    dragCaptchaWidth: data.dragCaptchaWidth ?? form.value.dragCaptchaWidth,
    dragCaptchaHeight: data.dragCaptchaHeight ?? form.value.dragCaptchaHeight,
    dragCaptchaThreshold: data.dragCaptchaThreshold ?? form.value.dragCaptchaThreshold,
    imageCaptchaLength: data.imageCaptchaLength ?? form.value.imageCaptchaLength,
    imageCaptchaNoiseLines: data.imageCaptchaNoiseLines ?? form.value.imageCaptchaNoiseLines,
    passwordMinLength: data.passwordMinLength ?? form.value.passwordMinLength,
    passwordRequireUppercase: data.passwordRequireUppercase ?? form.value.passwordRequireUppercase,
    passwordRequireLowercase: data.passwordRequireLowercase ?? form.value.passwordRequireLowercase,
    passwordRequireSpecial: data.passwordRequireSpecial ?? form.value.passwordRequireSpecial,
    passwordAllowSequential: data.passwordAllowSequential ?? form.value.passwordAllowSequential,
    defenseWindowMinutes: data.defenseWindowMinutes ?? form.value.defenseWindowMinutes,
    defenseMaxRequests: data.defenseMaxRequests ?? form.value.defenseMaxRequests,
    defenseMaxErrors: data.defenseMaxErrors ?? form.value.defenseMaxErrors,
  };
};

const saveSettings = async () => {
  saving.value = true;
  try {
    await request.post({ url: '/system/ui', data: { ...form.value } });
    await loadSettings();
    MessagePlugin.success('安全设置已保存');
  } catch (err: any) {
    MessagePlugin.error(err?.message || '保存失败');
  } finally {
    saving.value = false;
  }
};

onMounted(() => {
  loadSettings();
});
</script>
<style lang="less" scoped>
.security-setting {
  display: flex;
  flex-direction: column;
  gap: 16px;

  :deep(.t-card__body) {
    padding: 8px 24px 24px;
  }
}

.security-content {
  padding-top: 24px;
}
</style>
