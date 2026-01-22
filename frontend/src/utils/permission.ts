export function hasPerm(_code: string): boolean {
  // 仅保留页面访问权限（菜单）控制，功能级权限统一放行。
  return true;
}
