export interface PluginFrontendRegistration {
  pluginId: string;
  routePath: string;
  entryUrl: string;
  exposedModule: string;
}

export async function mountPlugin(registration: PluginFrontendRegistration, container: HTMLElement) {
  const system = (window as any).System;
  if (!system) {
    throw new Error('SystemJS 未加载');
  }
  const module = await system.import(registration.entryUrl);
  if (typeof module.mount === 'function') {
    await module.mount(container, { routePath: registration.routePath });
  }
}

export async function unmountPlugin(registration: PluginFrontendRegistration, container: HTMLElement) {
  const system = (window as any).System;
  if (!system) {
    return;
  }
  const module = await system.import(registration.entryUrl);
  if (typeof module.unmount === 'function') {
    await module.unmount(container);
  }
}
