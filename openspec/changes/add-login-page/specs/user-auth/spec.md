## ADDED Requirements

### Requirement: 账号密码登录
系统 SHALL 允许用户通过用户名和密码完成身份验证，成功后签发 JWT token。

#### Scenario: 账号密码正确
- **WHEN** 客户端发送 `POST /api/auth/login`，body 含合法 `username` 和 `password`
- **THEN** 响应状态码为 200
- **AND** 响应体含 `token` 字段（JWT 字符串）和 `expiresIn`（秒数）

#### Scenario: 密码错误
- **WHEN** 客户端发送 `POST /api/auth/login`，`password` 不匹配
- **THEN** 响应状态码为 401
- **AND** 响应体含 `error` 字段，值为 `INVALID_CREDENTIALS`

#### Scenario: 用户不存在
- **WHEN** 客户端发送 `POST /api/auth/login`，`username` 在数据库中不存在
- **THEN** 响应状态码为 401
- **AND** 响应体含 `error` 字段，值为 `INVALID_CREDENTIALS`（不泄露是用户名还是密码错误）

### Requirement: 手机号验证码登录
系统 SHALL 允许用户通过手机号和短信验证码完成身份验证，成功后签发 JWT token。

#### Scenario: 验证码正确
- **WHEN** 客户端发送 `POST /api/auth/login`，body 含合法 `phone` 和正确的 `smsCode`
- **THEN** 响应状态码为 200
- **AND** 响应体含 `token` 字段和 `expiresIn`

#### Scenario: 验证码错误或过期
- **WHEN** 客户端发送 `POST /api/auth/login`，`smsCode` 不匹配或已过期
- **THEN** 响应状态码为 401
- **AND** 响应体含 `error` 字段，值为 `INVALID_SMS_CODE`

### Requirement: JWT 鉴权
系统 SHALL 对受保护接口校验 Authorization 请求头中的 Bearer token。

#### Scenario: token 有效
- **WHEN** 请求头含 `Authorization: Bearer <valid-token>`
- **THEN** 请求通过鉴权，正常处理

#### Scenario: token 缺失或无效
- **WHEN** 请求头不含 token，或 token 格式错误 / 已过期
- **THEN** 响应状态码为 401
