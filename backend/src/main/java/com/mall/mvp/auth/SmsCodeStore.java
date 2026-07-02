package com.mall.mvp.auth;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * In-memory store for SMS verification codes.
 *
 * <p>This iteration has no real SMS gateway and no Redis: codes live in a
 * {@link ConcurrentHashMap}, expire after {@link #CODE_TTL}, and a phone may only
 * request a new code once per {@link #RESEND_INTERVAL}. The interface is designed so
 * a Redis-backed implementation can replace it later without touching callers.
 *
 * <p>A {@link Clock} is injected so expiry/rate-limit behaviour is unit-testable.
 */
@Component
public class SmsCodeStore {

    static final Duration CODE_TTL = Duration.ofMinutes(5);
    static final Duration RESEND_INTERVAL = Duration.ofSeconds(60);

    /** Raised when a phone asks for a new code within {@link #RESEND_INTERVAL}. */
    static final class TooManyRequestsException extends RuntimeException {
    }

    private record Entry(String code, long issuedAtMillis) {
    }

    private final Clock clock;
    private final SecureRandom random = new SecureRandom();
    private final ConcurrentHashMap<String, Entry> store = new ConcurrentHashMap<>();

    public SmsCodeStore() {
        this(Clock.systemDefaultZone());
    }

    SmsCodeStore(Clock clock) {
        this.clock = clock;
    }

    /**
     * Generates, stores and returns a fresh 6-digit code for the phone.
     *
     * @throws TooManyRequestsException if a code was issued less than {@link #RESEND_INTERVAL} ago
     */
    public String issue(String phone) {
        long now = clock.millis();
        Entry existing = store.get(phone);
        if (existing != null && now - existing.issuedAtMillis() < RESEND_INTERVAL.toMillis()) {
            throw new TooManyRequestsException();
        }
        String code = String.format("%06d", random.nextInt(1_000_000));
        store.put(phone, new Entry(code, now));
        return code;
    }

    /**
     * Verifies a code for the phone. On success the code is consumed (single-use).
     * Expired codes are removed and rejected.
     */
    public boolean verify(String phone, String code) {
        Entry entry = store.get(phone);
        if (entry == null) {
            return false;
        }
        if (clock.millis() - entry.issuedAtMillis() >= CODE_TTL.toMillis()) {
            store.remove(phone);
            return false;
        }
        boolean matches = entry.code().equals(code);
        if (matches) {
            store.remove(phone);
        }
        return matches;
    }

    /** Test-only: peek the currently stored code for a phone without consuming it. */
    String peek(String phone) {
        Entry entry = store.get(phone);
        return entry == null ? null : entry.code();
    }
}
