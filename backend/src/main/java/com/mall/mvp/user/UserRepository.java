package com.mall.mvp.user;

import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * Read access to the {@code users} table.
 *
 * <p>Implemented with {@code JdbcTemplate} because the backend uses
 * {@code spring-boot-starter-jdbc} (no Spring Data JPA on the classpath).
 */
@Repository
public class UserRepository {

    private static final RowMapper<User> ROW_MAPPER = (rs, rowNum) -> new User(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("phone"),
            rs.getString("password_hash"),
            rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toInstant());

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<User> findByUsername(String username) {
        return queryOne("SELECT * FROM users WHERE username = ?", username);
    }

    public Optional<User> findByPhone(String phone) {
        return queryOne("SELECT * FROM users WHERE phone = ?", phone);
    }

    private Optional<User> queryOne(String sql, Object arg) {
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, ROW_MAPPER, arg));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
