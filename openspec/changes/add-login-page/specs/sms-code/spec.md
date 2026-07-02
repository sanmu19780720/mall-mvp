## ADDED Requirements

### Requirement: 发送短信验证码
系统 SHALL 在收到请求后向指定手机号发送 6 位数字验证码，验证码有效期 5 分钟。

#### Scenario: 发送成功
- **WHEN** 客户端发送 `POST /api/auth/sms-code`，body 含合法 `phone`
- **THEN** 响应状态码为 200
- **AND** 验证码写入服务端存储（本期用内存 Map 模拟），绑定该手机号，5 分钟后过期

#### Scenario: 手机号格式不合法
- **WHEN** `phone` 不符合 11 位国内手机号格式
- **THEN** 响应状态码为 400
- **AND** 响应体含 `error` 字段，值为 `INVALID_PHONE`

#### Scenario: 发送频率限制
- **WHEN** 同一手机号在 60 秒内再次请求发送验证码
- **THEN** 响应状态码为 429
- **AND** 响应体含 `error` 字段，值为 `TOO_MANY_REQUESTS`
