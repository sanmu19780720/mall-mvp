## Context

当前仓库前端只有一个静态 `frontend/login.html`，无法支撑后续组件化开发。需要在此基础上建立两个独立的 Vue 3 工程：用户端（H5）和管理后台（PC）。两者共用同一套后端 API，但面向不同用户群体，UI 框架和路由结构也不同。

## Goals / Non-Goals

**Goals:**
- 建立 `frontend/`（用户端）和 `admin-frontend/`（管理后台）两个 Vue 3 + Vite 工程
- 配置 Axios 封装：统一 base URL、JWT 自动注入 Authorization 头、401 自动跳转登录页
- 配置 Vue Router 骨架：首页、登录页占位路由
- 配置 Pinia：基础 store 骨架（用户信息）
- 工程可正常启动（`npm run dev`）并访问

**Non-Goals:**
- 不实现任何业务页面（登录、商品等由后续 change 完成）
- 不配置 SSR、PWA、单元测试框架
- 不处理图片上传、富文本等复杂依赖

## Decisions

**1. 两个独立工程而非 monorepo**
用户端和管理后台技术差异明显（Vant vs Element Plus），独立工程更简单，mini 可以独立运行。后续如需共享组件再考虑提取。

**2. Vite 而非 Vue CLI**
Vue CLI 已进入维护模式，Vite 是官方推荐，构建速度更快，配置更简单。

**3. Axios 封装放在 `src/api/request.js`**
统一拦截器配置，后续各模块只需引用此实例，不重复配置 base URL 和 JWT 注入。

**4. API base URL 通过 `.env` 文件配置**
开发环境指向 `http://100.66.95.102:8080`，避免硬编码。

## Risks / Trade-offs

- [Node.js 环境] Mini 1 上需要有 Node.js，若未安装需要先装 → 在 tasks 里加前置检查步骤
- [现有 login.html] 保留不删除，不影响后续静态访问 → 后续前端 change 视情况迁移或废弃
