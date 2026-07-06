package com.mall.mvp.product;

/**
 * Product category record backing the {@code categories} table (V4).
 *
 * <p>Plain immutable record populated by {@link ProductRepository} via
 * {@code JdbcTemplate} (the project ships {@code spring-boot-starter-jdbc}, no JPA).
 */
public record Category(
        Long id,
        String name,
        Integer sortOrder) {
}
