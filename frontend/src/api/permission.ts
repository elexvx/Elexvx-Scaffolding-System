import type { MenuListResult } from '@/api/model/permissionModel';
import { request } from '@/utils/request';

const Api = {
  MenuList: '/get-menu-list-i18n',
};

export async function getMenuList(): Promise<MenuListResult> {
  const data = await request.get<MenuListResult>({ url: Api.MenuList });
  return data || { list: [] };
}
