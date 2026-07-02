# Tasks

## 1. 准备

- [x] 1.1 从最新 `dev` 切分支 `task/auth-001-backend`（原计划名 `task/auth-001-login`）
- [x] 1.2 阅读 `openspec/changes/add-login-page/` 下所有 artifact
- [x] 1.3 在 `backend/pom.xml` 中添加依赖：`jjwt-api`、`jjwt-impl`、`jjwt-jackson`、`spring-security-crypto`（已由 Air 完成）

## 2. 数据库迁移

- [x] 2.1 新增 `backend/src/main/resources/db/migration/V2__user.sql`，创建 `users` 表（字段：id, username, phone, password_hash, created_at）
- [x] 2.2 迁移在测试启动时由 Flyway 执行并通过（H2，`mvn test` 中验证）

## 3. 后端 — user 域

- [x] 3.1 新增 `com.mall.mvp.user.User`（改用不可变 record，见下方说明）
- [x] 3.2 新增 `com.mall.mvp.user.UserRepository`（改用 `JdbcTemplate`，见下方说明）

> 说明：pom.xml（Air 定稿）只含 `spring-boot-starter-jdbc`，未引入 `spring-boot-starter-data-jpa`，因此 user 域按现有依赖用 `JdbcTemplate` 实现，未使用 JPA。

## 4. 后端 — auth 域

- [x] 4.1 新增 `com.mall.mvp.auth.JwtUtil`：JWT 签发与校验，有效期 24h
- [x] 4.2 新增 `com.mall.mvp.auth.SmsCodeStore`：内存 Map 存储验证码，支持 5 分钟过期和 60 秒频率限制
- [x] 4.3 新增 `com.mall.mvp.auth.AuthService`：封装账号密码登录、手机号验证码登录逻辑（BCrypt 校验密码）
- [x] 4.4 新增 `com.mall.mvp.auth.AuthController`：
  - `POST /api/auth/sms-code`（发送验证码）
  - `POST /api/auth/login`（两种模式统一入口）
- [x] 4.5 新增 `com.mall.mvp.auth.JwtFilter`（+ `AuthConfig` 注册）：拦截受保护接口，校验 Bearer token，401 兜底

## 5. 测试

- [x] 5.1 新增 `AuthControllerTest`，MockMvc 覆盖：账号密码正确、密码错误、用户不存在
- [x] 5.2 新增 `AuthControllerTest` 场景：验证码正确、验证码错误；过期由 `SmsCodeStoreTest` 用可控 Clock 覆盖
- [x] 5.3 新增 `AuthControllerTest` 场景：发送验证码成功、手机号格式错误、频率限制
- [x] 5.4 本地运行 `mvn test`，全部通过（22 tests，含 `JwtFilterTest`）

## 6. 前端

- [x] 6.1 新增 `frontend/login.html`，含两个 Tab：账号密码 / 手机号验证码
- [x] 6.2 账号密码 Tab：表单提交调用 `POST /api/auth/login`，成功后将 token 存入 localStorage 并提示"登录成功"
- [x] 6.3 手机号验证码 Tab：点击"发送验证码"调用 `POST /api/auth/sms-code`，填入验证码后提交登录
- [ ] 6.4 本地打开 `login.html`，手动验证两种登录路径（需运行中的服务 + 浏览器，后端流程已由集成测试覆盖）

## 7. 交付

- [ ] 7.1 `git commit`（Conventional Commits，带 `#<issue号>`）
- [ ] 7.2 `git push -u origin task/auth-001-login`
- [ ] 7.3 `gh pr create --base dev`，描述写 `Closes #<issue号>` 并粘贴 `mvn test` 结果摘要
- [ ] 7.4 在 GitHub Project 看板把卡片从 In Progress 移到 Review
