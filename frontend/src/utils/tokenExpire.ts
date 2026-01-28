import { MessagePlugin } from 'tdesign-vue-next';

import router from '@/router';
import { useNotificationStore, useUserStore } from '@/store';
import { clearTokenStorage } from '@/utils/secureToken';

/**
 * Token 过期检测和处理工具
 */

/** Token 过期通知标记 */
let tokenExpireNotified = false;
const authPages = new Set(['/login', '/register', '/forgot']);
const isAuthPage = (path?: string) => Boolean(path && authPages.has(path));

/**
 * 设置 token 过期时间
 * @param token token 字符串
 * @param expiresIn 过期时间（秒）
 */
export function setTokenExpireTimer(token: string, expiresIn: number) {
  // 清除之前的定时器
  clearTokenExpireTimer();
  tokenExpireNotified = false;

  if (!token || !expiresIn || expiresIn <= 0) {
    return;
  }

  // 提前 5 分钟提示用户
  const notifyTime = Math.max((expiresIn - 300) * 1000, 1000);

  // 设置过期提示定时器
  const timerId = setTimeout(() => {
    notifyTokenExpire();
  }, notifyTime);

  // 保存定时器 ID 用于后续清除
  sessionStorage.setItem('tokenExpireTimerId', String(timerId));
}

/**
 * 通知 token 即将过期
 */
function notifyTokenExpire() {
  const currentPath = router.currentRoute.value?.path;

  // 避免重复提示和在登录页提示
  if (tokenExpireNotified || isAuthPage(currentPath)) {
    return;
  }

  tokenExpireNotified = true;

  // 显示提示信息
  MessagePlugin.warning({
    content: '您的登录状态即将过期，请及时刷新或重新登录',
    duration: 3000,
  });

  // 1.5 秒后自动跳转登录页
  setTimeout(() => {
    handleTokenExpired();
  }, 1500);
}

/**
 * 处理 token 过期
 */
export function handleTokenExpired() {
  const userStore = useUserStore();
  const currentPath = router.currentRoute.value?.path;

  // 避免重复处理和在登录页处理
  if (isAuthPage(currentPath)) {
    return;
  }

  // 清除 token 和用户信息
  userStore.token = '';
  userStore.tokenExpiresAt = null;
  userStore.userInfoLoaded = false;
  clearTokenStorage();
  useNotificationStore().stopSocket();

  // 显示提示信息
  MessagePlugin.warning('登录状态已过期，请重新登录');

  // 跳转到登录页
  router.replace({
    path: '/login',
    query: { redirect: currentPath },
  });
}

/**
 * 清除 token 过期定时器
 */
export function clearTokenExpireTimer() {
  const timerId = sessionStorage.getItem('tokenExpireTimerId');
  if (timerId) {
    clearTimeout(Number(timerId));
    sessionStorage.removeItem('tokenExpireTimerId');
  }
}

/**
 * 检查本地存储的 token 是否过期
 * @returns true 表示 token 已过期，false 表示有效
 */
export function isLocalTokenExpired(): boolean {
  const userStore = useUserStore();
  const token = userStore.token;

  if (!token) {
    return false;
  }

  // 尝试从 token 解析过期时间（JWT 格式）
  const expiresAt = extractTokenExpireTime(token);
  const storedExpiresAt = userStore.tokenExpiresAt;

  if (expiresAt || storedExpiresAt) {
    const now = Date.now();
    return now > (expiresAt || storedExpiresAt || 0);
  }

  // 如果无法解析 token 过期时间，则认为有效
  return false;
}

/**
 * 从 JWT token 中提取过期时间
 * @param token JWT token
 * @returns 过期时间戳（毫秒）或 null
 */
function extractTokenExpireTime(token: string): number | null {
  try {
    // JWT 格式: header.payload.signature
    const parts = token.split('.');
    if (parts.length !== 3) {
      return null;
    }

    // 解码 payload（第二部分）
    const payload = parts[1];
    // 添加补齐的 padding
    const padded = payload + '='.repeat((4 - (payload.length % 4)) % 4);
    const decoded = atob(padded);
    const data = JSON.parse(decoded);

    // JWT 中的 exp 字段是秒级时间戳，需要转换为毫秒
    if (data.exp) {
      return data.exp * 1000;
    }

    return null;
  } catch (error) {
    console.error('Failed to extract token expire time:', error);
    return null;
  }
}

/**
 * 检查 token 是否即将过期（5 分钟内）
 * @returns true 表示即将过期
 */
export function isTokenExpiringSoon(): boolean {
  const userStore = useUserStore();
  const token = userStore.token;

  if (!token) {
    return false;
  }

  const expiresAt = extractTokenExpireTime(token) || userStore.tokenExpiresAt;

  if (expiresAt) {
    const now = Date.now();
    const timeRemaining = expiresAt - now;
    // 5 分钟 = 300000 毫秒
    return timeRemaining > 0 && timeRemaining < 300000;
  }

  return false;
}
