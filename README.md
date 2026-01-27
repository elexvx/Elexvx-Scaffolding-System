# Elexvx ® Inc 脚手架系统

Elexvx 脚手架系统 是由 Elexvx ® Inc 推出的一套面向企业后台的脚手架/管理系统，涵盖用户与权限管理、系统配置、消息公告、文件与对象存储、短信/邮箱验证等基础能力，帮助快速搭建业务后台。

## 技术栈

- 前端：Vue 3、TypeScript、Vite、Pinia、Vue Router、TDesign Vue Next、ECharts、Axios
- 后端：Spring Boot 3.3、MyBatis、Sa-Token、Druid、MySQL、Redis（可选）、Spring Mail、SpringDoc OpenAPI
- 其他：Flyway（当前默认关闭）、AJ Captcha

## 快速开始

### 环境准备

- Node.js >= 18.18.0，npm >= 9
- JDK 17+，Maven 3.8+
- MySQL 8.x
- Redis（可选，用于监控等功能）

### 初始化数据库

- 可导入 `database/tdesign_init.sql` 作为初始结构与示例数据
- 或直接启动后端自动建表（数据库不存在会自动创建）

### 启动后端

```bash
cd backend
mvn spring-boot:run
```

- 默认端口：`8080`
- 上下文路径：`/api`
- 数据库配置：`backend/src/main/resources/application.yml`
- 可用环境变量覆盖：`MYSQL_URL`、`MYSQL_USER`、`MYSQL_PASSWORD`

### 启动前端

```bash
cd frontend
npm install
npm run dev
```

- 访问：`http://localhost:3002`
- 开发环境 `/api` 会代理到 `http://127.0.0.1:8080`

### 生产构建（可选）

```bash
cd frontend
npm run build
cd ../backend
mvn -DskipTests package
```

## 目录说明

- `frontend/` 管理后台前端（Vue 3 + Vite），页面、路由、状态管理、API 请求等
- `backend/` 后端服务（Spring Boot），接口、业务逻辑、数据访问与配置
- `database/` 初始化 SQL、表结构与基础数据脚本
- `scripts/` 项目辅助脚本（构建/部署/运维相关）

## 更新与业务隔离

为避免系统更新覆盖业务代码，建议将业务代码放在独立目录中，并通过配置或接口联动。  
详细说明请参考：`docs/UPDATE_GUIDE.md`。
