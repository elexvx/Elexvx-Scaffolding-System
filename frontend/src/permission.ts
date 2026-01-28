import 'nprogress/nprogress.css'; // progress bar style

import NProgress from 'nprogress'; // progress bar
import { MessagePlugin } from 'tdesign-vue-next';
import type { RouteRecordRaw } from 'vue-router';

import router from '@/router';
import { getPermissionStore, useAppStore, useNotificationStore, useSettingStore, useUserStore } from '@/store';
import { PAGE_NOT_FOUND_ROUTE } from '@/utils/route/constant';
import { clearTokenStorage } from '@/utils/secureToken';
import { handleTokenExpired, isLocalTokenExpired, setTokenExpireTimer } from '@/utils/tokenExpire';

NProgress.configure({ showSpinner: false });

const AUTH_PAGES = new Set(['/login', '/register', '/forgot']);
const isAuthPage = (path?: string) => Boolean(path && AUTH_PAGES.has(path));
let userStoreHydrated = false;
const hydrateUserStore = async (userStore: ReturnType<typeof useUserStore>) => {
  if (!userStoreHydrated) {
    userStoreHydrated = true;
    if (typeof (userStore as any).$hydrate === 'function') {
      (userStore as any).$hydrate({ runHooks: true });
    }
  }
  if (!userStore.token) {
    await userStore.restoreTokenFromStorage();
  }
};

// 应用启动时检查和恢复 token 过期定时器
const bootstrapTokenState = async () => {
  const userStore = useUserStore();
  await hydrateUserStore(userStore);
  if (userStore.token) {
    if (isLocalTokenExpired()) {
      userStore.token = '';
      userStore.tokenExpiresAt = null;
      userStore.userInfoLoaded = false;
      clearTokenStorage();
      return;
    }
    // 从本地存储的 token 重新配置过期定时器
    // 如果 token 中包含 exp 字段（JWT），会自动计算过期时间
    const expiresAt = (() => {
      try {
        const parts = userStore.token.split('.');
        if (parts.length === 3) {
          const payload = parts[1];
          const padded = payload + '='.repeat((4 - (payload.length % 4)) % 4);
          const decoded = atob(padded);
          const data = JSON.parse(decoded);
          if (data.exp) {
            return data.exp * 1000; // 转换为毫秒
          }
        }
      } catch (error) {
        console.error('Failed to extract token expire time:', error);
      }
      return null;
    })();

    const persistedExpiresAt = userStore.tokenExpiresAt;
    const finalExpiresAt = expiresAt || persistedExpiresAt;
    if (finalExpiresAt) {
      const now = Date.now();
      const expiresIn = Math.max(Math.floor((finalExpiresAt - now) / 1000), 0);
      if (expiresIn > 0) {
        setTokenExpireTimer(userStore.token, expiresIn);
      } else if (expiresIn <= 0) {
        // token 已过期
        userStore.token = '';
        userStore.tokenExpiresAt = null;
        userStore.userInfoLoaded = false;
        clearTokenStorage();
      }
    }
  }
};
bootstrapTokenState();

router.onError((error, to) => {
  const msg = String((error as any)?.message || error || '');
  console.error('[router error]', error);
  useAppStore().setRouteLoading(false);
  NProgress.done();
  const isChunkError =
    msg.includes('Failed to fetch dynamically imported module') ||
    msg.includes('Importing a module script failed') ||
    msg.includes('ChunkLoadError') ||
    msg.includes('Loading chunk');
  if (isChunkError) MessagePlugin.error('页面资源加载失败，请刷新后重试');
  else if (msg) MessagePlugin.error(msg);
  else MessagePlugin.error('页面加载失败，请稍后重试');

  if (to?.path && !to.path.startsWith('/result/')) {
    router.replace('/result/500').catch(() => {});
  }
});

router.beforeEach(async (to, from, next) => {
  NProgress.start();
  useAppStore().setRouteLoading(true);

  const permissionStore = getPermissionStore();
  const { whiteListRouters } = permissionStore;

  const userStore = useUserStore();
  await hydrateUserStore(userStore);

  if (userStore.token) {
    // 检查本地 token 是否过期
    if (isLocalTokenExpired()) {
      handleTokenExpired();
      NProgress.done();
      return;
    }

    if (isAuthPage(to.path)) {
      try {
        await userStore.getUserInfo();
        useNotificationStore().startSocket();
        const { asyncRoutes } = permissionStore;
        if (asyncRoutes && asyncRoutes.length === 0) {
          const routeList = await permissionStore.buildAsyncRoutes(userStore.userInfo);
          routeList.forEach((item: RouteRecordRaw) => {
            router.addRoute(item);
          });
        }
        const settingStore = useSettingStore();
        const fallbackHome = settingStore.defaultHome || '/user/index';
        const redirectValue = Array.isArray(to.query.redirect) ? to.query.redirect[0] : to.query.redirect;
        let target = fallbackHome;
        if (redirectValue) {
          try {
            const decoded = decodeURIComponent(String(redirectValue));
            target =
              (decoded === '/' || decoded === '/user/index') && fallbackHome !== '/user/index' ? fallbackHome : decoded;
          } catch {
            target = fallbackHome;
          }
        }
        next({ path: target, replace: true });
        return;
      } catch (error: any) {
        const msg = String(error?.message || '');
        if (!msg.includes('[401]')) MessagePlugin.error(msg);
        next();
        NProgress.done();
        return;
      }
    }
    try {
      await userStore.getUserInfo();
      useNotificationStore().startSocket();

      const { asyncRoutes } = permissionStore;

      if (asyncRoutes && asyncRoutes.length === 0) {
        const routeList = await permissionStore.buildAsyncRoutes(userStore.userInfo);
        routeList.forEach((item: RouteRecordRaw) => {
          router.addRoute(item);
        });

        const resolved = router.resolve(to.fullPath);
        if (resolved.name === PAGE_NOT_FOUND_ROUTE.name) {
          MessagePlugin.error('权限不足，请联系管理员开通');
          const settingStore = useSettingStore();
          next(settingStore.defaultHome || '/result/403');
          return;
        }
        next({ path: to.fullPath, replace: true, query: to.query });
        return;
      }
      if (to.name === PAGE_NOT_FOUND_ROUTE.name) {
        MessagePlugin.error('权限不足，请联系管理员开通');
        const settingStore = useSettingStore();
        next(settingStore.defaultHome || '/');
        return;
      }
      if (to.name && router.hasRoute(to.name)) {
        next();
      } else {
        MessagePlugin.error('权限不足，请联系管理员开通');
        const settingStore = useSettingStore();
        next(settingStore.defaultHome || '/');
      }
    } catch (error: any) {
      const msg = String(error?.message || '');
      // 401 已在 requestCatchHook 统一提示并跳转
      if (!msg.includes('[401]')) MessagePlugin.error(msg);
      next({
        path: '/login',
        query: { redirect: to.fullPath },
      });
      NProgress.done();
    }
  } else {
    /* white list router */
    if (whiteListRouters.includes(to.path)) {
      next();
    } else {
      next({
        path: '/login',
        query: { redirect: to.fullPath },
      });
    }
    NProgress.done();
  }
});

router.afterEach((to) => {
  useAppStore().setRouteLoading(false);
  if (isAuthPage(to.path)) {
    const userStore = useUserStore();
    const permissionStore = getPermissionStore();

    if (!userStore.token) {
      permissionStore.restoreRoutes();
    }
  }
  NProgress.done();
});
