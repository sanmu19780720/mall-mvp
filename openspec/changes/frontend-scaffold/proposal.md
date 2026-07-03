## Why

后续所有前端 change（用户端、商家后台、管理后台）都需要一个统一的前端工程基础，当前仓库只有一个静态 `login.html`，无法支撑 Vue 3 组件化开发。现在是搭脚手架的最佳时机，避免后续每个 change 都要处理工程基础问题。

## What Changes

- 新增 `frontend/` 目录：Vue 3 + Vite + Vant，用户端 H5 商城
- 新增 `admin-frontend/` 目录：Vue 3 + Vite + Element Plus，商家后台与管理员后台共用
- 两个项目均配置：Vue Router、Pinia、Axios（含请求拦截器、JWT 自动注入、401 跳转登录）
- 配置统一的 API base URL 指向后端（`http://100.66.95.102:8080`）
- 现有 `frontend/login.html` 保留但不再作为主入口，后续由 Vue 路由接管

## Capabilities

### New Capabilities
- `frontend-user-scaffold`：用户端 Vue 3 工程脚手架，含路由骨架、Axios 封装、Vant 基础配置
- `frontend-admin-scaffold`：管理后台 Vue 3 工程脚手架，含路由骨架、Axios 封装、Element Plus 基础配置

### Modified Capabilities

## Impact

- **前端**：新增 `frontend/` 和 `admin-frontend/` 两个 Vue 3 项目
- **依赖**：Node.js / npm（mini 上需提前安装）
- **后端**：无改动
- **后续 change**：所有前端功能模块在此脚手架基础上开发
