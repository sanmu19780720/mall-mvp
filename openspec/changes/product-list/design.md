## Context

当前 `product` 域为空，数据库无商品表。前端首页是占位符。商品列表/详情是用户端最高频的只读请求，无需鉴权，性能要求高于写接口。

## Goals / Non-Goals

**Goals:**
- 建 `categories` 和 `products` 表（V4 迁移）
- `GET /api/products`：分页列表，支持 `categoryId` 过滤，只返回 `status=active` 商品
- `GET /api/products/{id}`：单品详情，不存在返回 404
- 前端列表页 `/products`（Vant List 无限滚动）和详情页 `/products/:id`
- 首页加商品入口跳转

**Non-Goals:**
- 搜索（全文检索留后续）
- 商品管理后台（admin-frontend，另立 change）
- 图片上传（main_image 存 URL 字符串）
- 库存扣减（属于下单流程）

## Decisions

**1. 分页用 offset+limit，不用游标分页**
商品数量一期预计不超过千条，offset 性能足够；前端 Vant List 也是按 page 加载，实现最简单。

**2. 两个接口均无需 JWT**
商品浏览是匿名功能，在 `JwtFilter.shouldNotFilter` 放行 `/api/products/**`。

**3. price 用 DECIMAL(10,2) 存储**
避免浮点精度问题，Java 端用 `BigDecimal`。

**4. categories 表预置几条测试数据**
V4 迁移里直接 INSERT 几个分类（服装、数码、食品），方便开发和 QA 验证，无需额外接口。

**5. 不引入 JPA，继续用 JdbcTemplate**
与现有 user 域保持一致，避免引入新依赖。

## Risks / Trade-offs

- [H2 兼容] `DECIMAL` 和 `BIGINT` 在 H2 均支持，无风险
- [图片] main_image 存 URL，一期用占位图，后续接 OSS 只需改数据不改接口
- [offset 性能] 数据量大时会退化，但一期够用，后续可加游标分页
