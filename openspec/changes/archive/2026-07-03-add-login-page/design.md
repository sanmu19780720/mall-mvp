## Context

购物商城一期首个用户身份模块。后端骨架已就绪（Spring Boot 3.2，单模块按包分域），尚无任何业务接口。前端技术栈本期未锁定，以最小依赖为原则。

## Goals / Non-Goals

**Goals**
- 账号密码 + 手机号验证码两种登录方式
- JWT 签发与校验基础设施，供后续模块复用
- 前端登录页（最小实现，能跑通流程即可）

**Non-Goals**
- 真实短信服务对接（本期用内存模拟）
- 注册、找回密码、第三方登录
- 权限/角色体系（RBAC 留后续 change）

## Decisions

**1. JWT 库选用 jjwt（0.12.x）**
Spring Security 自带支持较重，jjwt 轻量且无额外框架侵入。不引入 Spring Security，手写 JWT Filter 挂在 Spring MVC 拦截器链。

**2. 验证码存储用 ConcurrentHashMap（内存）**
本期不引入 Redis，内存 Map 足够验证流程。真实短信/Redis 留后续 change 替换，接口不变。

**3. 前端用原生 HTML + Fetch API**
不引入前端框架，避免增加 CI 构建复杂度。放在 `frontend/` 目录，静态文件由 Spring Boot 托管或独立打开均可。

**4. 密码存储用 BCrypt**
`spring-security-crypto` 单独引入 BCrypt 工具，不依赖整个 Spring Security。

**5. 登录接口统一为 `POST /api/auth/login`，通过 body 字段区分模式**
```json
// 账号密码
{ "username": "foo", "password": "bar" }
// 手机号验证码
{ "phone": "13800138000", "smsCode": "123456" }
```
两种模式互斥，缺少必要字段返回 400。

## Risks / Trade-offs

- [内存验证码重启丢失] → 可接受，本期为开发验证阶段，后续替换 Redis
- [无刷新 token 机制] → JWT 过期用户需重新登录，本期 token 有效期设 24h 缓解
- [前端无路由守卫] → 静态页面简单实现，受保护页面由后端 401 兜底

## Migration Plan

1. 新增 `V2__user.sql`（Flyway），创建 `users` 表
2. 本地/CI 首次启动自动执行迁移，无需手动操作
3. 回滚：删除 `users` 表（数据丢失可接受，本期为开发环境）

## Open Questions

- 前端页面是否需要跳转到首页（商品列表）？本期首页尚未实现，登录成功后可先展示 token 或跳转 `/`
