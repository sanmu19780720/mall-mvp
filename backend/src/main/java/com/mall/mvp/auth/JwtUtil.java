package com.mall.mvp.auth;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Signs and verifies JWT access tokens (HS256). Secret and lifetime come from
 * configuration with sensible dev defaults, so no Air-owned {@code application.yml}
 * change is required this iteration (override {@code jwt.secret} in real deployments).
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationSeconds;

    public JwtUtil(
            @Value("${jwt.secret:mall-mvp-dev-secret-please-override-in-prod-0123456789abcdef}") String secret,
            @Value("${jwt.expiration-seconds:86400}") long expirationSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    /** Issues a signed token for the given user, valid for {@link #getExpirationSeconds()}. */
    public String generateToken(Long userId, String subject) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationSeconds * 1000L);
        return Jwts.builder()
                .subject(subject)
                .claim("uid", userId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /** Parses and verifies a token, returning its claims. Throws on invalid/expired tokens. */
    public Claims parse(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
