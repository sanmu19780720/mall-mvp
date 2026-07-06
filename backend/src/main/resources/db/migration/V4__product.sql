-- product-list change: product catalog tables.
-- Adds `categories` (browse taxonomy) and `products` (the sellable items backing the
-- list and detail endpoints). Seeds 3 categories and 5 demo products (one deliberately
-- offline) so the read APIs and their tests have deterministic data.
-- ANSI-standard SQL so it runs on both MySQL (prod) and H2 (test/CI), matching V1-V3.

CREATE TABLE IF NOT EXISTS categories (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    name       VARCHAR(64) NOT NULL,
    sort_order INT         NOT NULL DEFAULT 0,
    CONSTRAINT pk_categories PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS products (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    category_id BIGINT        NOT NULL,
    name        VARCHAR(128)  NOT NULL,
    main_image  VARCHAR(512),
    description VARCHAR(512),
    price       DECIMAL(10,2) NOT NULL DEFAULT 0,
    stock       INT           NOT NULL DEFAULT 0,
    sales_count INT           NOT NULL DEFAULT 0,
    status      VARCHAR(16)   NOT NULL DEFAULT 'active',
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_products PRIMARY KEY (id),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id)
);

-- Index the common browse filter (list is always scoped to active products by category).
CREATE INDEX idx_products_category_status ON products (category_id, status);

-- Seed categories with explicit ids so seeded products and tests can reference them.
INSERT INTO categories (id, name, sort_order) VALUES
    (1, '服装', 1),
    (2, '数码', 2),
    (3, '食品', 3);

-- Seed products across the categories. Product 4 is `offline` on purpose to exercise
-- the "下架商品返回 404" and "分类下无 active 商品" paths.
INSERT INTO products (id, category_id, name, main_image, description, price, stock, sales_count, status) VALUES
    (1, 1, '纯棉圆领T恤',   'https://cdn.example.com/p/tshirt.jpg',   '100% 纯棉，舒适透气的基础款圆领T恤。', 79.00,  100, 30,  'active'),
    (2, 1, '经典直筒牛仔裤', 'https://cdn.example.com/p/jeans.jpg',    '耐穿百搭的经典直筒牛仔裤。',           199.00, 50,  15,  'active'),
    (3, 2, '无线降噪耳机',   'https://cdn.example.com/p/earbuds.jpg',  '主动降噪，长续航的入耳式蓝牙耳机。',   299.00, 200, 120, 'active'),
    (4, 2, '清仓旧款手机',   'https://cdn.example.com/p/oldphone.jpg', '已下架清仓机型，仅供测试。',           999.00, 0,   5,   'offline'),
    (5, 3, '天然有机蜂蜜',   'https://cdn.example.com/p/honey.jpg',    '天然有机蜂蜜 500g，无添加。',          59.00,  300, 80,  'active');
