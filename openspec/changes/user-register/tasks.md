# Tasks

## 前端（role:frontend）

### 1. 准备

- [ ] 1.1 从最新 `dev` 切分支 `task/register-002-frontend`
- [ ] 1.2 阅读 `openspec/changes/user-register/` 下所有 artifact
- [ ] 1.3 确认后端 PR #14 已合并到 `dev`（注册接口已可用）

### 2. 注册页面

- [ ] 2.1 在 `frontend/src/views/` 新增 `RegisterView.vue`
  - 表单字段：手机号（或用户名）、密码、昵称
  - 使用 Vant 组件（`van-form`、`van-field`、`van-button`）
  - 手机号格式前端校验
  - 调用 `POST /api/user/register`，成功后跳转 `/login`
  - 失败时展示具体错误（用户名/手机号重复等）
- [ ] 2.2 在 `frontend/src/router/index.js` 注册路由 `/register`
- [ ] 2.3 在登录页 `LoginView.vue` 底部加"没有账号？去注册"跳转链接

### 3. 交付（前端）

- [ ] 3.1 `git commit`（Conventional Commits，带 `#<issue号>`）
- [ ] 3.2 `git push -u origin task/register-002-frontend`
- [ ] 3.3 `gh pr create --base dev`，描述写 `Closes #<issue号>`

---

## 后端（role:backend）

### 1. 准备（后端）

- [ ] 1.1 从最新 `dev` 切分支 `task/register-001-backend`
- [ ] 1.2 阅读 `openspec/changes/user-register/` 下所有 artifact

## 2. 数据库迁移

- [ ] 2.1 新增 `backend/src/main/resources/db/migration/V3__user_extend.sql`，为 `users` 表追加字段：
  - `nickname VARCHAR(64)` 允许 NULL
  - `email VARCHAR(128)` 允许 NULL，UNIQUE
  - `status VARCHAR(16) NOT NULL DEFAULT 'active'`
- [ ] 2.2 启动服务或运行 `mvn test` 验证迁移成功

## 3. 后端 — user 域扩展

- [ ] 3.1 更新 `com.mall.mvp.user.User` record，新增 `nickname`、`email`、`status` 字段
- [ ] 3.2 扩展 `com.mall.mvp.user.UserRepository`：
  - `existsByUsername(String username)`
  - `existsByPhone(String phone)`
  - `existsByEmail(String email)`
  - `save(User user)` 插入新用户，返回自增 id
- [ ] 3.3 新增 `com.mall.mvp.user.UserController`：`POST /api/user/register`
  - 校验必填字段（password、nickname、至少一个唯一标识）
  - 校验手机号格式（若提供）
  - 查重（username/phone/email 各自独立，409 含具体错误码）
  - BCrypt 加密密码后写库
  - 成功返回 201 + `{id, username}`

## 4. 测试

- [ ] 4.1 新增 `UserControllerTest`：注册成功（201）
- [ ] 4.2 新增 `UserControllerTest`：用户名重复（409 USERNAME_TAKEN）
- [ ] 4.3 新增 `UserControllerTest`：手机号重复（409 PHONE_TAKEN）
- [ ] 4.4 新增 `UserControllerTest`：必填字段缺失（400）、手机号格式错误（400）
- [ ] 4.5 运行 `mvn test`，全部通过

## 5. 交付（后端）

- [ ] 5.1 `git commit`（Conventional Commits，带 `#<issue号>`）
- [ ] 5.2 `git push -u origin task/register-001-backend`
- [ ] 5.3 `gh pr create --base dev`，描述写 `Closes #<issue号>` 并粘贴 `mvn test` 结果

---

## QA（role:qa）

> 前置条件：后端 PR #14 和前端注册页 PR 均已合并到 `dev`，后端服务在 Air（100.66.95.102:8080）启动。

### 1. 准备

- [ ] 1.1 从最新 `dev` 切分支 `task/register-003-qa`
- [ ] 1.2 阅读 `openspec/changes/user-register/specs/user-register/spec.md`

### 2. 验收测试

- [ ] 2.1 注册成功 → 201，返回 id
- [ ] 2.2 用户名重复 → 409 USERNAME_TAKEN
- [ ] 2.3 手机号重复 → 409 PHONE_TAKEN
- [ ] 2.4 邮箱重复 → 409 EMAIL_TAKEN
- [ ] 2.5 缺少 password → 400 INVALID_REQUEST
- [ ] 2.6 三个唯一标识均为空 → 400 INVALID_REQUEST
- [ ] 2.7 手机号格式错误 → 400 INVALID_PHONE
- [ ] 2.8 不带 Token 直接请求 → 201（不被 JWT 拦截）
- [ ] 2.9 前端注册页：填写表单 → 成功跳转登录页
- [ ] 2.10 前端注册页：重复手机号 → 展示错误提示

### 3. 交付（QA）

- [ ] 3.1 在 `qa/` 目录新增 `register-test-report.md`，记录每条用例结果
- [ ] 3.2 `git commit`（带 `#<issue号>`）
- [ ] 3.3 `git push -u origin task/register-003-qa`
- [ ] 3.4 `gh pr create --base dev`，描述写 `Closes #<issue号>`
