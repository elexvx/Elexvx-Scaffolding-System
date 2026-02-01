import { createPinia, setActivePinia } from 'pinia';
import { createPersistedState } from 'pinia-plugin-persistedstate';

const store = createPinia();
store.use(createPersistedState());
setActivePinia(store);

export { store };

export * from './modules/app';
export * from './modules/dictionary';
export * from './modules/notification';
export * from './modules/permission';
export * from './modules/setting';
export * from './modules/tabs-router';
export * from './modules/user';

export default store;
