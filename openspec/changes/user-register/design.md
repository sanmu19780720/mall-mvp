## Context

当前 `users` 表只有 `id/username/phone/password_hash/created_at` 五个字段，缺少 `nickname`、`email`、`status`。注册接口需要先补字段再实现业务逻辑。现有 `UserRepository` 只有按 username/phone 查询，需扩展查重方法。

## Goals / Non-Goals

**Goals:**
- V3 迁移为 `users` 表补充 `nickname`、`email`、`status` 字段
- `POST /api/user/register` 接口支持三种注册方式（用户名/手机号/邮箱均可作为唯一标识）
- 密码 BCrypt 加密，重复账号返回 409
- 前端注册页表单校验 + 注册成功跳转登录页

**Non-Goals:**
- 不做邮箱/手机验证码验证（PRD 明确一期暂不实现）
- 不做登录态自动跳转（注册成功后手动跳转登录页即可）

## Decisions

**1. username/phone/email 三者至少填一个作为登录标识**
与现有登录接口保持一致，login 支持三种方式，register 同样灵活，但至少提供一个唯一标识。

**2. 注册时必填 password 和 nickname**
nickname 是用户可见名称，password 是安全必须项，两者不可为空。

**3. V3 迁移只追加字段，不改已有字段**
`nickname` 允许 NULL（兼容已有 demo 账号），`email` 允许 NULL，`status` 默认 `active`。

**4. 查重逻辑：username/phone/email 各自独立查重**
任意一个已被注册则返回 409，错误体说明具体哪个字段冲突。

## Risks / Trade-offs

- [V3 迁移与 H2 兼容] `status` 用 VARCHAR 而非 ENUM，保证 H2 测试兼容 → 应用层用枚举校验
- [demo 账号字段为 NULL] nickname/email 为 NULL 的老数据不影响新注册逻辑
