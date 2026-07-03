# Tasks

## 1. 准备

- [ ] 1.1 从最新 `dev` 切分支 `task/scaffold-001-frontend`
- [ ] 1.2 确认 Node.js 已安装（`node -v`），版本需 >= 18；若未安装执行 `brew install node`
- [ ] 1.3 阅读 `openspec/changes/frontend-scaffold/` 下所有 artifact

## 2. 用户端脚手架（frontend/）

- [ ] 2.1 在项目根目录执行 `npm create vite@latest frontend -- --template vue`
- [ ] 2.2 进入 `frontend/`，安装依赖：`npm install vant vue-router pinia axios`
- [ ] 2.3 配置 Vant 按需引入：安装 `unplugin-vue-components unplugin-auto-import`，在 `vite.config.js` 中配置
- [ ] 2.4 新增 `frontend/src/api/request.js`：创建 Axios 实例，base URL 读取 `import.meta.env.VITE_API_BASE_URL`，请求拦截器注入 `Authorization: Bearer <token>`，响应拦截器处理 401 跳转 `/login`
- [ ] 2.5 新增 `frontend/.env.development`：写入 `VITE_API_BASE_URL=http://100.66.95.102:8080`
- [ ] 2.6 新增 `frontend/src/router/index.js`：配置 Vue Router，路由包含 `/`（HomeView 占位）和 `/login`（LoginView 占位）
- [ ] 2.7 新增 `frontend/src/stores/user.js`：Pinia store，存储 `token` 和 `userInfo`，提供 `setToken`、`clearToken` action
- [ ] 2.8 更新 `frontend/src/main.js`：注册 Vue Router 和 Pinia
- [ ] 2.9 执行 `npm run dev`，确认浏览器可访问 `/` 和 `/login` 无报错

## 3. 管理后台脚手架（admin-frontend/）

- [ ] 3.1 在项目根目录执行 `npm create vite@latest admin-frontend -- --template vue`
- [ ] 3.2 进入 `admin-frontend/`，安装依赖：`npm install element-plus vue-router pinia axios @element-plus/icons-vue`
- [ ] 3.3 配置 Element Plus 按需引入：安装 `unplugin-vue-components unplugin-auto-import`，在 `vite.config.js` 中配置
- [ ] 3.4 新增 `admin-frontend/src/api/request.js`：同用户端，base URL 读取环境变量，401 跳转 `/login`
- [ ] 3.5 新增 `admin-frontend/.env.development`：写入 `VITE_API_BASE_URL=http://100.66.95.102:8080`
- [ ] 3.6 新增 `admin-frontend/src/router/index.js`：配置 Vue Router，路由包含 `/`（Dashboard 占位）和 `/login`（LoginView 占位）
- [ ] 3.7 新增 `admin-frontend/src/stores/user.js`：同用户端
- [ ] 3.8 更新 `admin-frontend/src/main.js`：注册 Vue Router 和 Pinia
- [ ] 3.9 执行 `npm run dev`，确认浏览器可访问 `/` 和 `/login` 无报错

## 4. 收尾

- [ ] 4.1 在根目录 `.gitignore` 中确认 `node_modules/` 已被忽略（前端两个目录下各自的 `node_modules` 不提交）
- [ ] 4.2 `git add`（排除 node_modules）并 `git commit`（Conventional Commits，带 `#<issue号>`）
- [ ] 4.3 `git push -u origin task/scaffold-001-frontend`
- [ ] 4.4 `gh pr create --base dev`，描述写 `Closes #<issue号>`
