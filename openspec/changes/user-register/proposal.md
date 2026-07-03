## Why

登录模块已完成，但目前用户只能用预置的 demo 账号登录，无法自助注册。用户注册是购物商城的基础能力，所有后续业务（购物车、订单）都依赖真实用户账号。

## What Changes

- 新增后端注册接口 `POST /api/user/register`，支持用户名/手机号/邮箱注册
- 新增 Flyway 迁移 `V3__user_extend.sql`，为 `users` 表补充 `nickname`、`email`、`status` 字段
- 新增前端注册页面 `/register`，表单提交后跳转登录页

## Capabilities

### New Capabilities
- `user-register`：用户自助注册，含字段校验、重复检查、密码 BCrypt 加密存储

### Modified Capabilities

## Impact

- **后端**：新增 `com.mall.mvp.user.UserController`，扩展 `UserRepository` 支持按用户名/手机/邮箱查重
- **数据库**：V3 迁移为 `users` 表新增 `nickname`、`email`、`status` 字段
- **前端**：`frontend/` 新增注册页 `/register`
- **依赖**：无新增
