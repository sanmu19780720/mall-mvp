## Why

购物商城需要用户身份体系作为所有业务功能（下单、查看订单、个人中心）的前置条件。登录是用户进入系统的第一道门，必须在商品、交易等模块之前完成。

## What Changes

- 新增后端登录接口 `POST /api/auth/login`，支持账号密码和手机号验证码两种方式，成功后返回 JWT token
- 新增后端发送验证码接口 `POST /api/auth/sms-code`
- 新增前端登录页面，含账号密码 tab 和手机号验证码 tab
- 新增 Flyway 迁移脚本 `V2__user.sql`，创建 `users` 表
- JWT token 写入后续所有受保护接口的鉴权基础

## Capabilities

### New Capabilities
- `user-auth`: 用户登录与身份验证，含账号密码登录、手机号验证码登录、JWT 签发与校验
- `sms-code`: 短信验证码发送与验证（本期可用内存/假实现替代真实短信服务）

### Modified Capabilities
（无）

## Impact

- **后端**：新增 `auth` 包（Controller / Service / Repository），`user` 包（User 实体、UserRepository）
- **数据库**：新增 `users` 表（V2 迁移），字段含 id / username / phone / password_hash / created_at
- **依赖**：引入 `spring-boot-starter-security`（或手动 JWT 库，如 `jjwt`）
- **前端**：新增登录页面（技术栈待定，本期可为简单 HTML + Fetch API）
- **测试**：MockMvc 覆盖两种登录路径的成功与失败场景
