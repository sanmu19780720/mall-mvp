package com.mall.mvp.user;

import java.sql.PreparedStatement;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/**
 * Read/write access to the {@code users} table.
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
            rs.getString("nickname"),
            rs.getString("email"),
            rs.getString("status"),
            rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toInstant());

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<User> findById(Long id) {
        return queryOne("SELECT * FROM users WHERE id = ?", id);
    }

    public Optional<User> findByUsername(String username) {
        return queryOne("SELECT * FROM users WHERE username = ?", username);
    }

    public Optional<User> findByPhone(String phone) {
        return queryOne("SELECT * FROM users WHERE phone = ?", phone);
    }

    public boolean existsByUsername(String username) {
        // No identifier supplied means nothing to collide with; skip the query.
        if (username == null) {
            return false;
        }
        return exists("SELECT COUNT(*) FROM users WHERE username = ?", username);
    }

    public boolean existsByPhone(String phone) {
        if (phone == null) {
            return false;
        }
        return exists("SELECT COUNT(*) FROM users WHERE phone = ?", phone);
    }

    public boolean existsByEmail(String email) {
        return exists("SELECT COUNT(*) FROM users WHERE email = ?", email);
    }

    /**
     * Inserts a new user and returns it re-read from the database so DB-populated
     * columns ({@code id}, {@code created_at}) are reflected. The {@code id} on the
     * passed-in record is ignored.
     */
    public User save(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            // Name the generated-key column explicitly: H2 otherwise returns the whole
            // row and KeyHolder.getKey() would fail with "multiple keys".
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO users (username, phone, password_hash, nickname, email, status) "
                            + "VALUES (?, ?, ?, ?, ?, ?)",
                    new String[] {"id"});
            ps.setString(1, user.username());
            ps.setString(2, user.phone());
            ps.setString(3, user.passwordHash());
            ps.setString(4, user.nickname());
            ps.setString(5, user.email());
            ps.setString(6, user.status());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return findById(id).orElseThrow(
                () -> new IllegalStateException("Inserted user " + id + " could not be re-read"));
    }

    private boolean exists(String sql, Object arg) {
        Integer count = jdbc.queryForObject(sql, Integer.class, arg);
        return count != null && count > 0;
    }

    private Optional<User> queryOne(String sql, Object arg) {
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, ROW_MAPPER, arg));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
