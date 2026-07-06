## ADDED Requirements

### Requirement: 商品列表分页查询
系统 SHALL 提供商品列表接口，支持分页和分类筛选，无需鉴权。

#### Scenario: 获取第一页商品列表
- **WHEN** 客户端发送 `GET /api/products?page=0&size=10`
- **THEN** 响应状态码为 200
- **AND** 响应体含 `content`（商品数组）、`totalElements`、`totalPages`、`page`、`size`
- **AND** 只返回 `status=active` 的商品
- **AND** 每个商品含 `id`、`name`、`mainImage`、`price`、`salesCount`、`categoryId`

#### Scenario: 按分类筛选
- **WHEN** 客户端发送 `GET /api/products?categoryId=1&page=0&size=10`
- **THEN** 响应状态码为 200
- **AND** 返回的商品均属于 `categoryId=1` 的分类

#### Scenario: 分类下无商品
- **WHEN** 指定 categoryId 下没有 active 商品
- **THEN** 响应状态码为 200
- **AND** `content` 为空数组，`totalElements` 为 0

#### Scenario: 无需 Token
- **WHEN** 请求不携带 Authorization header
- **THEN** 响应状态码为 200（不被 JWT 拦截）

#### Scenario: size 参数超出上限
- **WHEN** `size` 大于 100
- **THEN** 响应状态码为 400
