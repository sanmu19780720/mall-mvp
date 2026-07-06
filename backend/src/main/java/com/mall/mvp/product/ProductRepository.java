package com.mall.mvp.product;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * Read access to the {@code products} / {@code categories} tables.
 *
 * <p>Implemented with {@code JdbcTemplate} because the backend uses
 * {@code spring-boot-starter-jdbc} (no Spring Data JPA on the classpath). The public
 * catalog only exposes reads, so this repository is read-only.
 */
@Repository
public class ProductRepository {

    /** {@code active} is the only browsable status; everything else is hidden from the catalog. */
    static final String ACTIVE_STATUS = "active";

    /** List rows do not carry {@code categoryName} (only the detail query joins categories). */
    private static final RowMapper<Product> LIST_ROW_MAPPER = (rs, rowNum) -> new Product(
            rs.getLong("id"),
            rs.getLong("category_id"),
            null,
            rs.getString("name"),
            rs.getString("main_image"),
            rs.getString("description"),
            rs.getBigDecimal("price"),
            rs.getInt("stock"),
            rs.getInt("sales_count"),
            rs.getString("status"),
            rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toInstant());

    private static final RowMapper<Product> DETAIL_ROW_MAPPER = (rs, rowNum) -> new Product(
            rs.getLong("id"),
            rs.getLong("category_id"),
            rs.getString("category_name"),
            rs.getString("name"),
            rs.getString("main_image"),
            rs.getString("description"),
            rs.getBigDecimal("price"),
            rs.getInt("stock"),
            rs.getInt("sales_count"),
            rs.getString("status"),
            rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toInstant());

    private final JdbcTemplate jdbc;

    public ProductRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Returns one page of active products, newest first, optionally scoped to a category.
     *
     * @param categoryId when non-null, restricts to that category; when null, all categories
     * @param page zero-based page index (assumed already validated as {@code >= 0})
     * @param size page size (assumed already validated as {@code 1..100})
     */
    public List<Product> findActive(Long categoryId, int page, int size) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, category_id, name, main_image, description, price, stock, "
                        + "sales_count, status, created_at FROM products WHERE status = ?");
        List<Object> args = new ArrayList<>();
        args.add(ACTIVE_STATUS);
        if (categoryId != null) {
            sql.append(" AND category_id = ?");
            args.add(categoryId);
        }
        sql.append(" ORDER BY id DESC LIMIT ? OFFSET ?");
        args.add(size);
        args.add(page * size);
        return jdbc.query(sql.toString(), LIST_ROW_MAPPER, args.toArray());
    }

    /** Counts active products, optionally scoped to a category (for pagination totals). */
    public long countActive(Long categoryId) {
        String sql = "SELECT COUNT(*) FROM products WHERE status = ?";
        Long count;
        if (categoryId != null) {
            count = jdbc.queryForObject(sql + " AND category_id = ?", Long.class, ACTIVE_STATUS, categoryId);
        } else {
            count = jdbc.queryForObject(sql, Long.class, ACTIVE_STATUS);
        }
        return count == null ? 0L : count;
    }

    /**
     * Looks up a single <em>active</em> product by id, joining its category name. Offline
     * (non-active) products are intentionally excluded so they surface as "not found".
     */
    public Optional<Product> findActiveById(Long id) {
        String sql = "SELECT p.id, p.category_id, c.name AS category_name, p.name, p.main_image, "
                + "p.description, p.price, p.stock, p.sales_count, p.status, p.created_at "
                + "FROM products p JOIN categories c ON c.id = p.category_id "
                + "WHERE p.id = ? AND p.status = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, DETAIL_ROW_MAPPER, id, ACTIVE_STATUS));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
