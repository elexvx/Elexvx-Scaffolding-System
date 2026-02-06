<template>
  <confirm-drawer
    v-model:visible="visible"
    :title="t('pages.user.personalInfo.editTitle')"
    :on-close="handleClose"
    placement="right"
    size="760px"
    :footer="false"
  >
    <t-form
      ref="formRef"
      class="drawer-form--single"
      :data="formData"
      :rules="rules"
      :colon="true"
      label-width="120px"
      label-align="right"
      layout="vertical"
      @submit="handleConfirm"
    >
      <t-form-item label="姓名" name="name">
        <t-input v-model="formData.name" placeholder="请输入姓名" />
      </t-form-item>

      <t-form-item label="手机" name="mobile">
        <t-input v-model="formData.mobile" placeholder="请输入手机号码" />
      </t-form-item>

      <t-form-item label="座机" name="phone">
        <t-input v-model="formData.phone" placeholder="请输入座机号码" />
      </t-form-item>

      <t-form-item label="邮箱" name="email">
        <t-input v-model="formData.email" placeholder="请输入邮箱地址" />
      </t-form-item>

      <t-form-item label="座位号" name="seat">
        <t-input v-model="formData.seat" placeholder="请输入座位号" />
      </t-form-item>

      <t-form-item label="公司/部门" name="entity">
        <t-input v-model="formData.entity" placeholder="请输入公司或部门" />
      </t-form-item>

      <t-form-item label="直属上级" name="leader">
        <t-input v-model="formData.leader" placeholder="请输入直属上级姓名" />
      </t-form-item>

      <t-form-item label="职位" name="position">
        <t-input v-model="formData.position" placeholder="请输入职位" />
      </t-form-item>

      <t-form-item label="入职日期" name="joinDay">
        <t-date-picker v-model="formData.joinDay" placeholder="请选择入职日期" format="YYYY-MM-DD" />
      </t-form-item>

      <t-form-item label="所属团队" name="team">
        <t-textarea v-model="formData.team" placeholder="请输入所属团队信息" />
      </t-form-item>
    </t-form>

    <template #footer>
      <div class="drawer-footer">
        <t-button @click="handleClose">{{ t('common.cancel') || '取消' }}</t-button>
        <t-button theme="primary" :loading="loading" @click="handleConfirm">
          {{ t('common.confirm') || '确认' }}
        </t-button>
      </div>
    </template>
  </confirm-drawer>
</template>
<script setup lang="ts">
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, reactive, ref, watch } from 'vue';

import type { UserProfile, UserProfileUpdate } from '@/api/user';
import { updateMyProfile } from '@/api/user';
import ConfirmDrawer from '@/components/ConfirmDrawer.vue';
import { t } from '@/locales';

interface Props {
  visible?: boolean;
  userInfo?: UserProfile | null;
}

interface Emits {
  (e: 'update:visible', val: boolean): void;
  (e: 'update-success', data: UserProfile): void;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  userInfo: null,
});

const emit = defineEmits<Emits>();

const formRef = ref();
const loading = ref(false);

const formData = reactive<UserProfileUpdate>({
  name: '',
  mobile: '',
  phone: '',
  email: '',
  seat: '',
  entity: '',
  leader: '',
  position: '',
  joinDay: '',
  team: '',
});

const rules = {
  name: [{ required: true, message: '请输入姓名', type: 'error' as const }],
  email: [{ message: '邮箱格式不正确', type: 'warning' as const }],
};

const visible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val),
});

watch(
  () => props.userInfo,
  (newVal) => {
    if (newVal) {
      formData.name = newVal.name || '';
      formData.mobile = newVal.mobile || '';
      formData.phone = newVal.phone || '';
      formData.email = newVal.email || '';
      formData.seat = newVal.seat || '';
      formData.entity = newVal.entity || '';
      formData.leader = newVal.leader || '';
      formData.position = newVal.position || '';
      formData.joinDay = newVal.joinDay || '';
      formData.team = newVal.team || '';
    }
  },
  { immediate: true, deep: true },
);

const handleConfirm = async () => {
  const result = await formRef.value?.validate();
  if (!result) {
    return;
  }

  loading.value = true;
  try {
    const response = await updateMyProfile({ ...formData });
    MessagePlugin.success('用户信息更新成功');
    emit('update-success', response);
    visible.value = false;
  } catch (error) {
    MessagePlugin.error('用户信息更新失败，请稍后重试');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const handleClose = () => {
  visible.value = false;
};
</script>
<style lang="less" scoped>
.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 12px 0;
  border-top: 1px solid #f0f0f0;
  background-color: #fff;
}
</style>
