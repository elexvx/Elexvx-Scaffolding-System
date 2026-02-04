import { request } from '@/utils/request';

export interface ModuleRegistryItem {
  moduleKey: string;
  name: string;
  version: string;
  enabled: boolean;
  installState: string;
  installedAt?: string;
}

export function fetchModules() {
  return request.get<ModuleRegistryItem[]>({ url: '/system/modules' });
}

export function enableModule(moduleKey: string) {
  return request.post<ModuleRegistryItem>({ url: `/system/modules/${moduleKey}/enable` });
}

export function disableModule(moduleKey: string) {
  return request.post<ModuleRegistryItem>({ url: `/system/modules/${moduleKey}/disable` });
}

export function installModule(moduleKey: string) {
  return request.post<ModuleRegistryItem>({ url: `/system/modules/${moduleKey}/install` });
}

export function uninstallModule(moduleKey: string) {
  return request.post<ModuleRegistryItem>({ url: `/system/modules/${moduleKey}/uninstall` });
}
