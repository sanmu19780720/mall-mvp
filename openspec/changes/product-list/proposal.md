## Why

注册登录已完成，用户进入首页后无商品可浏览，购物主流程无法推进。商品列表和详情是用户端的核心入口，所有后续购物车、下单流程都依赖它。

## What Changes

- 新增 Flyway 迁移 `V4__product.sql`，建 `products` 表和 `categories` 表
- 新增后端 `product` 域：商品列表接口（分页 + 按分类筛选）、商品详情接口
- 新增前端商品列表页 `/products`、商品详情页 `/products/:id`
- 首页 `/` 展示商品列表入口

## Capabilities

### New Capabilities
- `product-list`：分页获取上架商品列表，支持按分类筛选
- `product-detail`：按 ID 获取单个商品详情

### Modified Capabilities

## Impact

- **数据库**：新增 `categories` 表（id、name、sort_order）和 `products` 表（id、category_id、name、main_image、description、price、stock、status、created_at）
- **后端**：新增 `com.mall.mvp.product` 包，`ProductController`（两个 GET 接口，无需鉴权）
- **前端**：新增 `ProductListView.vue`、`ProductDetailView.vue`，更新 router，首页加商品入口
- **依赖**：无新增
