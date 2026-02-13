# 打印中心模块包

## 构建 ZIP
```bash
cd module-packages/print
./build-zip.sh
```
产物：`module-packages/print/dist/module-print.zip`

## 安装步骤
1. 启动主系统后端与前端。
2. 登录系统，进入 **系统管理 -> 模块管理 -> 上传模块包**，上传 `module-print.zip` 并安装/启用。
3. 安装完成后，菜单出现 **打印中心**（iframe 地址 `/modules/print/index.html`）。
4. 在打印中心创建模板并保存/发布。
5. 业务页面使用 `PrintButton` 组件：选择模板 -> 预览 -> 浏览器打印 / 生成 PDF 留档。

## 验证点
- `/modules/print/index.html` 可访问。
- `/api/module-api/print/templates` 可返回模板。
- `/api/module-api/print/render/pdf` 成功返回 `fileUrl` 与 `jobId`。
