import { request } from '@/utils/request';

export interface ModuleDescriptor {
  key: string;
  name: string;
  source: string;
  license: string;
  version: string;
  enabled: boolean;
}

export function fetchModuleList() {
  return request.get<ModuleDescriptor[]>({ url: '/system/modules' });
}
