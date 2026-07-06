package com.mall.mvp.product;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Product record backing the {@code products} table (V4).
 *
 * <p>{@code categoryName} is not a column on {@code products}; it is populated by a JOIN
 * against {@code categories} for the detail query and left {@code null} for list rows
 * (the list response does not include it). Populated by {@link ProductRepository} via
 * {@code JdbcTemplate} (the project ships {@code spring-boot-starter-jdbc}, no JPA).
 */
public record Product(
        Long id,
        Long categoryId,
        String categoryName,
        String name,
        String mainImage,
        String description,
        BigDecimal price,
        Integer stock,
        Integer salesCount,
        String status,
        Instant createdAt) {
}
