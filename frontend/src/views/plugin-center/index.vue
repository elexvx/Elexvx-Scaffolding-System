<template>
  <div class="plugin-center">
    <h2>插件中心（single-spa + SystemJS）</h2>
    <p>默认禁用插件；启用后才会渲染插件前端模块。</p>
    <div ref="containerRef" class="plugin-slot" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { mountPlugin, unmountPlugin } from '@/plugins/systemjs-loader'

const containerRef = ref<HTMLElement>()
const warehouseRegistration = {
  pluginId: 'warehouse',
  routePath: '/plugins/warehouse/inbound',
  entryUrl: '/plugins/warehouse/main.js',
  exposedModule: 'warehouseApp',
}

onMounted(async () => {
  if (containerRef.value) {
    await mountPlugin(warehouseRegistration, containerRef.value)
  }
})

onUnmounted(async () => {
  if (containerRef.value) {
    await unmountPlugin(warehouseRegistration, containerRef.value)
  }
})
</script>

<style scoped>
.plugin-slot { min-height: 120px; border: 1px dashed #bbb; padding: 12px; }
</style>
