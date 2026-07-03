## ADDED Requirements

### Requirement: 管理后台前端工程可正常启动
管理后台前端工程 SHALL 基于 Vue 3 + Vite + Element Plus，执行 `npm run dev` 后可在浏览器访问。

#### Scenario: 工程启动成功
- **WHEN** 在 `admin-frontend/` 目录执行 `npm run dev`
- **THEN** 本地开发服务器启动，浏览器可访问首页占位路由（`/`）

### Requirement: Axios 封装含 JWT 自动注入
管理后台 SHALL 提供统一的 Axios 实例，自动注入 JWT token 并处理 401 响应。

#### Scenario: 请求自动携带 token
- **WHEN** localStorage 中存在 `token` 字段
- **THEN** 所有 API 请求的 `Authorization` 头自动设置为 `Bearer <token>`

#### Scenario: 401 自动跳转登录
- **WHEN** 后端返回 HTTP 401
- **THEN** 前端自动清除 localStorage 中的 token 并跳转到 `/login`

### Requirement: Vue Router 骨架含基础路由
管理后台 SHALL 配置 Vue Router，包含首页和登录页占位路由。

#### Scenario: 路由可正常跳转
- **WHEN** 用户访问 `/` 或 `/login`
- **THEN** 对应占位页面正常渲染，无报错
