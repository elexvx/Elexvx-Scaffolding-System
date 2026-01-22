import { defineStore } from 'pinia';

import { usePermissionStore } from '@/store';
import type { LoginResponse, UserInfo } from '@/types/interface';
import { clearTokenStorage, saveToken } from '@/utils/secureToken';
import { clearTokenExpireTimer } from '@/utils/tokenExpire';

const InitUserInfo: UserInfo = {
  id: undefined,
  name: '', // 用户名，用于展示在页面右上角头像处
  avatar: '',
  roles: [], // 前端权限模型使用 如果使用请配置modules/permission-fe.ts使用
  assignedRoles: [],
  roleSimulated: false,
  permissions: [],
};

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    tokenExpiresAt: null as number | null,
    userInfo: { ...InitUserInfo },
    userInfoLoaded: false,
  }),
  getters: {
    roles: (state) => {
      return state.userInfo?.roles;
    },
  },
  actions: {
    async login(userInfo: Record<string, unknown>): Promise<LoginResponse> {
      const { request } = await import('@/utils/request');
      const res = await request.post<LoginResponse>({ url: '/auth/login', data: userInfo }, { withToken: false });
      if (res?.status === 'ok' && res.token) {
        this.token = res.token;
        this.tokenExpiresAt = res.expiresIn ? Date.now() + res.expiresIn * 1000 : null;
        this.userInfoLoaded = false;
        await saveToken(res.token);
        const { useTabsRouterStore } = await import('@/store/modules/tabs-router');
        if (typeof window !== 'undefined') {
          window.localStorage.removeItem('tabsRouter');
        }
        useTabsRouterStore().$reset();
      }
      return res;
    },
    async confirmLogin(requestId: string, requestKey: string): Promise<LoginResponse> {
      const { request } = await import('@/utils/request');
      const res = await request.post<LoginResponse>(
        { url: '/auth/login/confirm', data: { requestId, requestKey } },
        { withToken: false },
      );
      if (res?.status === 'ok' && res.token) {
        this.token = res.token;
        this.tokenExpiresAt = res.expiresIn ? Date.now() + res.expiresIn * 1000 : null;
        this.userInfoLoaded = false;
        await saveToken(res.token);
        const { useTabsRouterStore } = await import('@/store/modules/tabs-router');
        if (typeof window !== 'undefined') {
          window.localStorage.removeItem('tabsRouter');
        }
        useTabsRouterStore().$reset();
      }
      return res;
    },
    async loginBySms(payload: { phone: string; code: string; force?: boolean }): Promise<LoginResponse> {
      const { request } = await import('@/utils/request');
      const res = await request.post<LoginResponse>({ url: '/auth/login/sms', data: payload }, { withToken: false });
      if (res?.status === 'ok' && res.token) {
        this.token = res.token;
        this.tokenExpiresAt = res.expiresIn ? Date.now() + res.expiresIn * 1000 : null;
        this.userInfoLoaded = false;
        await saveToken(res.token);
        const { useTabsRouterStore } = await import('@/store/modules/tabs-router');
        if (typeof window !== 'undefined') {
          window.localStorage.removeItem('tabsRouter');
        }
        useTabsRouterStore().$reset();
      }
      return res;
    },
    async loginByEmail(payload: { email: string; code: string; force?: boolean }): Promise<LoginResponse> {
      const { request } = await import('@/utils/request');
      const res = await request.post<LoginResponse>({ url: '/auth/login/email', data: payload }, { withToken: false });
      if (res?.status === 'ok' && res.token) {
        this.token = res.token;
        this.tokenExpiresAt = res.expiresIn ? Date.now() + res.expiresIn * 1000 : null;
        this.userInfoLoaded = false;
        await saveToken(res.token);
        const { useTabsRouterStore } = await import('@/store/modules/tabs-router');
        if (typeof window !== 'undefined') {
          window.localStorage.removeItem('tabsRouter');
        }
        useTabsRouterStore().$reset();
      }
      return res;
    },
    async restoreTokenFromStorage() {
      if (this.token) return this.token;
      const { loadToken } = await import('@/utils/secureToken');
      const token = await loadToken();
      if (token) {
        this.token = token;
      }
      return token;
    },
    async getUserInfo(force = false) {
      if (this.userInfoLoaded && !force) return;
      const { request } = await import('@/utils/request');
      const info = await request.get<UserInfo>({ url: '/auth/user' });
      this.userInfo = info;
      this.userInfoLoaded = true;
    },
    async logout() {
      const { request } = await import('@/utils/request');
      clearTokenExpireTimer();
      await request.post({ url: '/auth/logout' });
      this.token = '';
      this.tokenExpiresAt = null;
      this.userInfo = { ...InitUserInfo };
      this.userInfoLoaded = false;
      const { useTabsRouterStore } = await import('@/store/modules/tabs-router');
      if (typeof window !== 'undefined') {
        window.localStorage.removeItem('tabsRouter');
      }
      useTabsRouterStore().$reset();
      clearTokenStorage();
    },
  },
  persist: {
    afterRestore: () => {
      const permissionStore = usePermissionStore();
      permissionStore.initRoutes();
    },
    key: 'user',
    paths: ['tokenExpiresAt', 'userInfo'],
    storage: typeof window === 'undefined' ? undefined : localStorage,
  },
});
