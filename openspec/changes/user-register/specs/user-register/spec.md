## ADDED Requirements

### Requirement: 用户注册
系统 SHALL 允许用户通过表单注册账号，密码加密存储，重复账号返回 409。

#### Scenario: 注册成功
- **WHEN** 客户端发送 `POST /api/user/register`，body 含合法 `username`（或 `phone` 或 `email`）、`password`、`nickname`
- **THEN** 响应状态码为 201
- **AND** 响应体含 `id` 和 `username`（或注册时使用的唯一标识）
- **AND** 密码以 BCrypt 哈希形式存储，明文不落库

#### Scenario: 用户名已被注册
- **WHEN** 请求中的 `username` 已存在于数据库
- **THEN** 响应状态码为 409
- **AND** 响应体含 `error: USERNAME_TAKEN`

#### Scenario: 手机号已被注册
- **WHEN** 请求中的 `phone` 已存在于数据库
- **THEN** 响应状态码为 409
- **AND** 响应体含 `error: PHONE_TAKEN`

#### Scenario: 邮箱已被注册
- **WHEN** 请求中的 `email` 已存在于数据库
- **THEN** 响应状态码为 409
- **AND** 响应体含 `error: EMAIL_TAKEN`

#### Scenario: 必填字段缺失
- **WHEN** 请求 body 缺少 `password` 或 `nickname`，或 `username`/`phone`/`email` 三者均为空
- **THEN** 响应状态码为 400
- **AND** 响应体含 `error: INVALID_REQUEST`

#### Scenario: 手机号格式不合法
- **WHEN** 请求中的 `phone` 不符合 11 位国内手机号格式
- **THEN** 响应状态码为 400
- **AND** 响应体含 `error: INVALID_PHONE`
