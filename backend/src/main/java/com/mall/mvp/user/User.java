package com.mall.mvp.user;

import java.time.Instant;

/**
 * User identity record backing the {@code users} table (V2 baseline, extended by V3
 * with {@code nickname}, {@code email}, and {@code status}).
 *
 * <p>The project ships with {@code spring-boot-starter-jdbc} (no JPA), so this is a
 * plain immutable record populated by {@link UserRepository} via {@code JdbcTemplate}
 * rather than a JPA {@code @Entity}.
 */
public record User(
        Long id,
        String username,
        String phone,
        String passwordHash,
        String nickname,
        String email,
        String status,
        Instant createdAt) {
}
