## ADDED Requirements

### Requirement: 商品详情查询
系统 SHALL 提供按 ID 查询单个商品详情的接口，无需鉴权。

#### Scenario: 查询存在的商品
- **WHEN** 客户端发送 `GET /api/products/{id}`，id 对应一个 active 商品
- **THEN** 响应状态码为 200
- **AND** 响应体含 `id`、`name`、`mainImage`、`description`、`price`、`stock`、`salesCount`、`categoryId`、`categoryName`

#### Scenario: 商品不存在
- **WHEN** 指定 id 不存在于数据库
- **THEN** 响应状态码为 404
- **AND** 响应体含 `error: PRODUCT_NOT_FOUND`

#### Scenario: 商品已下架
- **WHEN** 指定 id 对应 status 不为 active 的商品
- **THEN** 响应状态码为 404
- **AND** 响应体含 `error: PRODUCT_NOT_FOUND`

#### Scenario: 无需 Token
- **WHEN** 请求不携带 Authorization header
- **THEN** 响应状态码为 200（不被 JWT 拦截）
