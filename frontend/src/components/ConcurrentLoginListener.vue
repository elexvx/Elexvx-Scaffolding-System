<template><span /></template>
<script setup lang="ts">
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';
import { onBeforeUnmount, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useUserStore } from '@/store';
import { request } from '@/utils/request';
import { clearTokenStorage } from '@/utils/secureToken';
import { buildSseUrl } from '@/utils/sse';

const userStore = useUserStore();
const router = useRouter();
const route = useRoute();
let source: EventSource | null = null;
let activeDialog: ReturnType<typeof DialogPlugin.confirm> | null = null;
let reconnectTimer: number | null = null;
const authPages = new Set(['/login', '/register', '/forgot']);
const isAuthPage = (path?: string) => Boolean(path && authPages.has(path));

const clearReconnect = () => {
  if (reconnectTimer) {
    window.clearTimeout(reconnectTimer);
    reconnectTimer = null;
  }
};

const closeSource = () => {
  if (source) {
    source.close();
    source = null;
  }
  clearReconnect();
};

const closeDialog = () => {
  if (activeDialog) {
    activeDialog.hide();
    activeDialog = null;
  }
};

const forceLogoutToLogin = async () => {
  userStore.token = '';
  userStore.tokenExpiresAt = null;
  clearTokenStorage();
  userStore.userInfo = { name: '', roles: [] };
  userStore.userInfoLoaded = false;
  try {
    await router.replace('/login');
  } finally {
    window.location.reload();
  }
};

const sendDecision = async (requestId: string, action: 'approve' | 'reject') => {
  try {
    await request.post({ url: '/auth/concurrent/decision', data: { requestId, action } });
    if (action === 'approve') {
      MessagePlugin.success('已确认，下线当前设备');
      await forceLogoutToLogin();
    } else {
      MessagePlugin.success('已拒绝该次登录');
    }
  } catch (err: any) {
    MessagePlugin.error(String(err?.message || '操作失败'));
  }
};

const showConcurrentDialog = (payload: any) => {
  if (isAuthPage(route.path)) return;
  const requestId = String(payload?.requestId || '');
  if (!requestId) return;
  const deviceInfo = payload?.deviceInfo || '未知设备';
  const ipAddress = payload?.ipAddress ? `IP: ${payload.ipAddress}` : '';
  const loginLocation = payload?.loginLocation ? `地点: ${payload.loginLocation}` : '';
  const detail = [deviceInfo, ipAddress, loginLocation].filter(Boolean).join('，');

  if (activeDialog) {
    closeDialog();
  }

  activeDialog = DialogPlugin.confirm({
    header: '多设备登录提醒',
    body: `您的账号正尝试在 ${detail} 登录。若非本人操作，请及时下线该设备并修改密码。`,
    confirmBtn: '已知晓，下线',
    cancelBtn: '不是本人操作',
    theme: 'warning',
    closeOnOverlayClick: false,
    onConfirm: async () => {
      closeDialog();
      await sendDecision(requestId, 'approve');
    },
    onCancel: async () => {
      closeDialog();
      await sendDecision(requestId, 'reject');
    },
    onClose: () => {
      closeDialog();
    },
  });
};

const openSource = (token: string) => {
  if (isAuthPage(route.path)) return;
  closeSource();
  const url = buildSseUrl('/auth/concurrent/stream', { Authorization: token });
  source = new EventSource(url);
  source.addEventListener('concurrent-login', (event: MessageEvent) => {
    try {
      const payload = JSON.parse(event.data || '{}');
      showConcurrentDialog(payload);
    } catch {
      showConcurrentDialog({});
    }
  });
  source.onerror = () => {
    closeSource();
    if (!userStore.token) return;
    reconnectTimer = window.setTimeout(() => {
      if (userStore.token) openSource(userStore.token);
    }, 3000);
  };
};

watch(
  () => userStore.token,
  (token) => {
    closeDialog();
    if (token && !isAuthPage(route.path)) {
      openSource(token);
    } else {
      closeSource();
    }
  },
  { immediate: true },
);

watch(
  () => route.path,
  (path) => {
    if (isAuthPage(path)) {
      closeDialog();
      closeSource();
      return;
    }
    if (userStore.token) {
      openSource(userStore.token);
    }
  },
);

onBeforeUnmount(() => {
  closeDialog();
  closeSource();
});
</script>
