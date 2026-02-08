/**
 * 功能级权限判断工具。
 *
 * 当前策略：仅保留“页面访问权限（菜单）”控制，功能级权限统一放行。
 * 如需启用细粒度按钮/接口权限：
 * - 将 _code 映射到 userInfo.permissions（或后端返回的权限集合）
 * - 在页面或指令中以 hasPerm(code) 控制展示/禁用
 */
export function hasPerm(_code: string): boolean {
  // 仅保留页面访问权限（菜单）控制，功能级权限统一放行。
  return true;
}
