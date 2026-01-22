import { defineStore } from 'pinia';

export const useAppStore = defineStore('app', {
  state: () => ({
    routeLoading: false,
    backendReady: false,
  }),
  actions: {
    setRouteLoading(loading: boolean) {
      this.routeLoading = loading;
    },
    setBackendReady(ready: boolean) {
      this.backendReady = ready;
    },
  },
});
