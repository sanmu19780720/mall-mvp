# Tasks

## 1. 准备（后端）

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
