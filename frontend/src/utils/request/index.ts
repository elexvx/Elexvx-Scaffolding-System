// axios配置  可自行根据项目进行更改，只需更改该文件即可，其他文件可以不动
import type { AxiosInstance } from 'axios';
import isString from 'lodash/isString';
import merge from 'lodash/merge';
import { DialogPlugin, MessagePlugin } from 'tdesign-vue-next';

import { ContentTypeEnum } from '@/constants';
import router from '@/router';
import { useUserStore } from '@/store';
import { resolveApiHost } from '@/utils/apiHost';
import { clearTokenStorage } from '@/utils/secureToken';

import { VAxios } from './Axios';
import type { AxiosTransform, CreateAxiosOptions } from './AxiosTransform';
import { formatRequestDate, joinTimestamp, setObjToUrlParams } from './utils';

let hasNotifiedUnauthorized = false;
let forcedLogoutDialog: ReturnType<typeof DialogPlugin.confirm> | null = null;

// 如果是mock模式 或 没启用直连代理 就不配置host 会走本地Mock拦截 或 Vite 代理
const host = resolveApiHost();

// 数据处理，方便区分多种处理方式
const transform: AxiosTransform = {
  // 处理请求数据。如果数据不是预期格式，可直接抛出错误
  transformRequestHook: (res, options) => {
    const { isTransformResponse, isReturnNativeResponse } = options;

    // 如果204无内容直接返回
    const method = res.config.method?.toLowerCase();
    if (res.status === 204 && ['put', 'patch', 'delete'].includes(method)) {
      return res;
    }

    // 是否返回原生响应头 比如：需要获取响应头时使用该属性
    if (isReturnNativeResponse) {
      return res;
    }
    // 不进行任何处理，直接返回
    // 用于页面代码可能需要直接获取code，data，message这些信息时开启
    if (!isTransformResponse) {
      return res.data;
    }

    // 错误的时候返回
    const { data } = res;
    if (!data) {
      throw new Error('请求接口错误');
    }

    //  这里 code为 后台统一的字段，需要在 types.ts内修改为项目自己的接口返回格式
    const { code, message } = data;

    // 这里逻辑可以根据项目进行修改
    const hasSuccess = data && code === 0;
    if (hasSuccess) {
      return data.data;
    }

    // 使用后端返回的错误信息，如果没有则使用默认信息
    const errorMsg = message || `请求失败，错误码: ${code}`;
    throw new Error(`${errorMsg} [${code}]`);
  },

  // 捕获 transformRequestHook 抛出的错误，聚合到统一结果页
  requestCatchHook: async (e) => {
    const msg = String(e?.message || '');
    // 尝试从错误信息中提取错误码 [401] 或 错误码: 401 格式
    const m = msg.match(/\[(\d{3})\]/) || msg.match(/错误码:\s*(\d{3})/);
    const code = m ? Number(m[1]) : undefined;

    // 只有在特定的严重错误时才跳转页面，登录失败等一般错误不跳转
    if (code === 401) {
      const user = useUserStore();
      const humanMsg = msg.replace(/\s*\[\d{3}\]\s*$/, '').trim();
      const redirectToLogin = () => {
        if (router.currentRoute.value.path !== '/login') {
          router.replace({
            path: '/login',
            query: { redirect: router.currentRoute.value.fullPath },
          });
          return true;
        }
        return false;
      };

      const isForcedOffline = /强制下线|管理员|账号在其他地方登录|被迫下线|挤下线|顶下线|异地登录/.test(humanMsg);

      const showForcedOfflineDialog = (message: string) => {
        if (forcedLogoutDialog) return;
        forcedLogoutDialog = DialogPlugin.confirm({
          header: '账号已被下线',
          body: message || '当前账号已在其他设备登录，请重新登录',
          confirmBtn: '确定',
          cancelBtn: null,
          closeOnOverlayClick: false,
          closeOnEscKeydown: false,
          onConfirm: () => {
            forcedLogoutDialog?.hide();
            forcedLogoutDialog = null;
            redirectToLogin();
          },
          onClose: () => {
            forcedLogoutDialog?.hide();
            forcedLogoutDialog = null;
          },
        });
      };

      if (isForcedOffline) {
        hasNotifiedUnauthorized = true;
        // 清除本地token
        user.token = '';
        user.tokenExpiresAt = null;
        user.userInfo = { name: '', avatar: '', roles: [] };
        user.userInfoLoaded = false;
        clearTokenStorage();

        // 先跳转到登录页
        redirectToLogin();
        // 再显示弹窗
        showForcedOfflineDialog(humanMsg);

        try {
          await user.logout();
        } catch {}
      } else if (!hasNotifiedUnauthorized && router.currentRoute.value.path !== '/login') {
        hasNotifiedUnauthorized = true;
        MessagePlugin.warning('登录已过期，请重新登录');

        // 清除本地token
        user.token = '';
        user.tokenExpiresAt = null;
        user.userInfo = { name: '', avatar: '', roles: [] };
        user.userInfoLoaded = false;
        clearTokenStorage();
        // 尝试调用退出接口，不等待
        user.logout().catch(() => {});

        // 延迟跳转，让用户看清提示
        setTimeout(() => {
          redirectToLogin();
          hasNotifiedUnauthorized = false;
        }, 1500);
      }
    } else if (code === 403) {
      const humanMsg = msg.replace(/\s*\[\d{3}\]\s*$/, '').trim();
      MessagePlugin.error(humanMsg || '权限不足，请联系管理员开通');
    } else if (code === 422) {
      const humanMsg = msg.replace(/\s*\[\d{3}\]\s*$/, '').trim();
      if (humanMsg) {
        MessagePlugin.warning(humanMsg);
      }
    } else if (code != null && code >= 500) {
      const humanMsg = msg.replace(/\s*\[\d{3}\]\s*$/, '').trim();
      MessagePlugin.error(humanMsg || '服务器错误，请稍后重试');
    }
    // 不再自动跳转到错误页面，让业务代码自己处理错误提示
    return Promise.reject(e);
  },

  // 请求前处理配置
  beforeRequestHook: (config, options) => {
    const { apiUrl, isJoinPrefix, urlPrefix, joinParamsToUrl, formatDate, joinTime = true } = options;

    // 添加接口前缀
    if (isJoinPrefix && urlPrefix && isString(urlPrefix)) {
      config.url = `${urlPrefix}${config.url}`;
    }

    // 将baseUrl拼接
    if (apiUrl && isString(apiUrl)) {
      config.url = `${apiUrl}${config.url}`;
    }
    const params = config.params || {};
    const data = config.data || false;

    if (formatDate && data && !isString(data)) {
      formatRequestDate(data);
    }
    if (config.method?.toUpperCase() === 'GET') {
      if (!isString(params)) {
        // 给 get 请求加上时间戳参数，避免从缓存中拿数据。
        config.params = Object.assign(params || {}, joinTimestamp(joinTime, false));
      } else {
        // 兼容restful风格
        config.url = `${config.url + params}${joinTimestamp(joinTime, true)}`;
        config.params = undefined;
      }
    } else if (!isString(params)) {
      if (formatDate) {
        formatRequestDate(params);
      }
      if (
        Reflect.has(config, 'data') &&
        config.data &&
        (Object.keys(config.data).length > 0 || data instanceof FormData)
      ) {
        config.data = data;
        config.params = params;
      } else {
        // 非GET请求如果没有提供data，则将params视为data
        config.data = params;
        config.params = undefined;
      }
      if (joinParamsToUrl) {
        config.url = setObjToUrlParams(config.url as string, { ...config.params, ...config.data });
      }
    } else {
      // 兼容restful风格
      config.url += params;
      config.params = undefined;
    }
    return config;
  },

  // 请求拦截器处理
  requestInterceptors: (config, options) => {
    // 请求之前处理config
    const userStore = useUserStore();
    const { token } = userStore;

    if (token) hasNotifiedUnauthorized = false;
    if (token && (config as Recordable)?.requestOptions?.withToken !== false) {
      (config as Recordable).headers.Authorization = options.authenticationScheme
        ? `${options.authenticationScheme} ${token}`
        : token;
    }
    const data = (config as Recordable)?.data;
    if (typeof FormData !== 'undefined' && data instanceof FormData) {
      (config as Recordable).headers = (config as Recordable).headers || {};
      delete (config as Recordable).headers['Content-Type'];
      delete (config as Recordable).headers['content-type'];
    }
    const currentPath = router.currentRoute.value?.path;
    if (currentPath) {
      (config as Recordable).headers = (config as Recordable).headers || {};
      (config as Recordable).headers['X-Page-Path'] = currentPath;
    }
    return config;
  },

  // 响应拦截器处理
  responseInterceptors: (res) => {
    return res;
  },

  // 响应错误处理
  responseInterceptorsCatch: (error: any, instance: AxiosInstance) => {
    const { config } = error;
    const status = error?.response?.status as number | undefined;

    if (status === 401) {
      // 401 错误由 requestCatchHook 统一处理
      return Promise.reject(error);
    }

    if (status === 403) {
      const serverMsg = error?.response?.data?.message;
      MessagePlugin.error(serverMsg || '权限不足，请联系管理员开通');
    }

    // 对于客户端错误(4xx)，不进行重试，直接返回错误
    if (status && status >= 400 && status < 500) {
      return Promise.reject(error);
    }

    // 只对网络错误和服务器错误(5xx)进行重试
    if (!config || !config.requestOptions.retry) return Promise.reject(error);

    config.retryCount = config.retryCount || 0;

    if (config.retryCount >= config.requestOptions.retry.count) {
      // 重试次数用尽后，不自动跳转页面，让业务代码处理
      return Promise.reject(error);
    }

    config.retryCount += 1;

    const backoff = new Promise((resolve) => {
      setTimeout(() => {
        resolve(config);
      }, config.requestOptions.retry.delay || 1);
    });
    config.headers = { ...config.headers, 'Content-Type': ContentTypeEnum.Json };
    return backoff.then((config) => instance.request(config));
  },
};

function createAxios(opt?: Partial<CreateAxiosOptions>) {
  return new VAxios(
    merge(
      <CreateAxiosOptions>{
        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication#authentication_schemes
        // 例如: authenticationScheme: 'Bearer'
        authenticationScheme: '',
        // 超时
        timeout: 10 * 1000,
        // 携带Cookie
        withCredentials: true,
        // 头信息
        headers: { 'Content-Type': ContentTypeEnum.Json },
        // 数据处理方式
        transform,
        // 配置项，下面的选项都可以在独立的接口请求中覆盖
        requestOptions: {
          // 接口地址
          apiUrl: host,
          // 是否自动添加接口前缀
          isJoinPrefix: true,
          // 接口前缀
          // 例如: https://www.baidu.com/api
          // urlPrefix: '/api'
          urlPrefix: import.meta.env.VITE_API_URL_PREFIX,
          // 是否返回原生响应头 比如：需要获取响应头时使用该属性
          isReturnNativeResponse: false,
          // 需要对返回数据进行处理
          isTransformResponse: true,
          // post请求的时候添加参数到url
          joinParamsToUrl: false,
          // 格式化提交参数时间
          formatDate: true,
          // 是否加入时间戳
          joinTime: true,
          // 是否忽略请求取消令牌
          // 如果启用，则重复请求时不进行处理
          // 如果禁用，则重复请求时会取消当前请求
          ignoreCancelToken: true,
          // 是否携带token
          withToken: true,
          // 重试
          retry: {
            count: 3,
            delay: 1000,
          },
        },
      },
      opt || {},
    ),
  );
}
export const request = createAxios();
