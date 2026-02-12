# 插件三段隔离 + 打印中心落地说明

## 1. 目录/模块划分
- `backend/src/main/java/com/tencent/tdesign/plugin`：后端插件安装、生命周期、扩展点接口。
- `backend/src/main/java/com/tencent/tdesign/print`：打印中心统一模型、渲染器、服务。
- `backend/src/main/resources/db/migration/core`：核心表 Flyway 迁移。
- `backend/src/main/resources/db/migration/plugins/warehouse`：示例插件 warehouse schema。
- `frontend/src/plugins`：SystemJS 动态加载器。
- `frontend/src/views/plugin-center`：插件壳页面示例。
- `docs/plugins/warehouse-plugin`：示例插件包内容（manifest/menus/permissions/print）。

## 2. 插件启用/禁用流程
1. 上传插件包 `.elexvx-plugin` 到 `/api/plugins/install`。
2. 服务端计算 SHA-256，并解析 `manifest.yml`。
3. 根据 `plug_{pluginId}` 创建 schema，执行 Flyway migration。
4. 通过 PF4J 加载并启动后端插件。
5. 由前端注册表发布 SystemJS 入口后，shell 动态挂载。
6. 禁用执行 `/api/plugins/{pluginId}/disable`，停止 PF4J 插件并置为 DISABLED。

> 默认行为：不开启插件时不加载任何插件前后端能力。

## 3. 打印模板规范
- `PDFBOX_LAYOUT`：JSON 坐标模板，支持文字/图片/二维码等元素。
- `XSL_FO`：FOP 模板，适合报表分页与跨页表头。
- `XLSX`：POI 输出同源数据报表。
- 统一 API：
  - `GET /api/print/definitions`
  - `POST /api/print/jobs`
  - `GET /api/print/jobs/{jobId}/file`

## 4. 示例插件（warehouse）
- DB：`plug_warehouse.warehouse_inbound`
- 后端：注册 `warehouse.inbound.note` 打印定义。
- 前端：`/plugins/warehouse/main.js` 由 SystemJS 动态加载。
- 打印：`print/inbound-layout.json` 作为入库单模板。
