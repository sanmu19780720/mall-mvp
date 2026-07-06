# Tasks

## 后端（role:backend）

### 1. 准备
- [ ] 从最新 `dev` 切分支 `task/product-001-backend`
- [ ] 阅读 `openspec/changes/product-list/` 下所有 artifact

### 2. 数据库迁移
- [ ] 新增 `backend/src/main/resources/db/migration/V4__product.sql`：
  - 建 `categories` 表：`id BIGINT AUTO_INCREMENT PRIMARY KEY`、`name VARCHAR(64) NOT NULL`、`sort_order INT DEFAULT 0`
  - 建 `products` 表：`id`、`category_id`、`name VARCHAR(128)`、`main_image VARCHAR(512)`、`description VARCHAR(512)`、`price DECIMAL(10,2)`、`stock INT`、`sales_count INT DEFAULT 0`、`status VARCHAR(16) DEFAULT 'active'`、`created_at`
  - INSERT 3 个测试分类：服装、数码、食品
  - INSERT 5 条测试商品（覆盖至少 2 个分类）

### 3. 后端 product 域
- [ ] 新增 `com.mall.mvp.product.Category` record
- [ ] 新增 `com.mall.mvp.product.Product` record（含 categoryName 用于详情）
- [ ] 新增 `com.mall.mvp.product.ProductRepository`（JdbcTemplate）：
  - `findActive(categoryId, page, size)` 分页查询
  - `countActive(categoryId)` 总数
  - `findById(id)` 单品（join category 取 categoryName）
- [ ] 新增 `com.mall.mvp.product.ProductController`：
  - `GET /api/products`：参数 `page(默认0)`、`size(默认10,最大100)`、`categoryId(可选)`，返回分页结构
  - `GET /api/products/{id}`：返回详情或 404 PRODUCT_NOT_FOUND
- [ ] 在 `JwtFilter.shouldNotFilter` 放行 `/api/products/**`（授权修改 auth 包）

### 4. 测试
- [ ] `ProductControllerTest`：列表分页、按分类筛选、空结果、无 Token 200、size 超限 400
- [ ] `ProductControllerTest`：详情成功、商品不存在 404、下架商品 404
- [ ] `mvn test` 全绿

### 5. 交付（后端）
- [ ] `git commit`（带 `#<issue号>`）
- [ ] `git push -u origin task/product-001-backend`
- [ ] `gh pr create --base dev`，描述写 `Closes #<issue号>` 并粘贴 `mvn test` 结果

---

## 前端（role:frontend）

> 前置：后端 PR 已合并到 dev

### 1. 准备
- [ ] 从最新 `dev` 切分支 `task/product-002-frontend`
- [ ] 阅读 `openspec/changes/product-list/specs/`

### 2. 商品列表页
- [ ] 新增 `frontend/src/views/ProductListView.vue`
  - 顶部分类 Tab（`van-tabs`），从 `GET /api/products?categoryId=x` 加载
  - 商品卡片列表（图片、名称、价格），使用 `van-list` 无限滚动
  - 点击商品卡片跳转 `/products/:id`
- [ ] 新增 `frontend/src/api/product.js`，封装两个接口调用

### 3. 商品详情页
- [ ] 新增 `frontend/src/views/ProductDetailView.vue`
  - 展示主图、名称、价格、库存、描述
  - 商品不存在时展示"商品不存在"提示

### 4. 路由 & 首页
- [ ] 在 `router/index.js` 注册 `/products` 和 `/products/:id`
- [ ] 更新 `HomeView.vue`，加"逛逛商品"入口按钮跳转 `/products`

### 5. 交付（前端）
- [ ] `git commit`（带 `#<issue号>`）
- [ ] `git push -u origin task/product-002-frontend`
- [ ] `gh pr create --base dev`，描述写 `Closes #<issue号>`

---

## QA（role:qa）

> 前置：后端和前端 PR 均已合并，后端服务在 Air（100.66.95.102:8080）运行

### 1. 准备
- [ ] 从最新 `dev` 切分支 `task/product-003-qa`
- [ ] 阅读 `openspec/changes/product-list/specs/`

### 2. 验收测试
- [ ] 商品列表：第一页返回 200，含分页结构
- [ ] 商品列表：按分类筛选
- [ ] 商品列表：无 Token 返回 200
- [ ] 商品列表：size 超 100 返回 400
- [ ] 商品详情：存在的商品返回 200，含所有字段
- [ ] 商品详情：不存在的 id 返回 404 PRODUCT_NOT_FOUND
- [ ] 商品详情：无 Token 返回 200

### 3. 交付（QA）
- [ ] 新增 `qa/product-test-report.md`
- [ ] `git commit`（带 `#<issue号>`）
- [ ] `git push -u origin task/product-003-qa`
- [ ] `gh pr create --base dev`，描述写 `Closes #<issue号>`
