package com.mall.mvp.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SmsCodeStore} using a controllable {@link Clock}, so
 * time-dependent behaviour (5-minute expiry, 60-second resend limit) is deterministic.
 */
class SmsCodeStoreTest {

    /** A clock whose {@code instant()} we can advance by hand. */
    private static final class MutableClock extends Clock {
        private Instant instant;

        MutableClock(Instant start) {
            this.instant = start;
        }

        void advance(Duration by) {
            this.instant = this.instant.plus(by);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }

    @Test
    void issuedCodeVerifiesWithinTtl() {
        SmsCodeStore store = new SmsCodeStore(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        String code = store.issue("13800138000");
        assertTrue(store.verify("13800138000", code));
    }

    @Test
    void codeIsSingleUse() {
        SmsCodeStore store = new SmsCodeStore(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        String code = store.issue("13800138000");
        assertTrue(store.verify("13800138000", code));
        assertFalse(store.verify("13800138000", code), "code should be consumed after a successful verify");
    }

    @Test
    void codeExpiresAfterTtl() {
        MutableClock clock = new MutableClock(Instant.EPOCH);
        SmsCodeStore store = new SmsCodeStore(clock);
        String code = store.issue("13800138000");

        clock.advance(SmsCodeStore.CODE_TTL.plusSeconds(1));

        assertFalse(store.verify("13800138000", code), "expired code must be rejected");
    }

    @Test
    void resendWithinIntervalIsRejected() {
        MutableClock clock = new MutableClock(Instant.EPOCH);
        SmsCodeStore store = new SmsCodeStore(clock);
        store.issue("13800138000");

        assertThrows(SmsCodeStore.TooManyRequestsException.class, () -> store.issue("13800138000"));
    }

    @Test
    void resendAllowedAfterInterval() {
        MutableClock clock = new MutableClock(Instant.EPOCH);
        SmsCodeStore store = new SmsCodeStore(clock);
        store.issue("13800138000");

        clock.advance(SmsCodeStore.RESEND_INTERVAL.plusSeconds(1));

        // Should not throw.
        String code = store.issue("13800138000");
        assertTrue(store.verify("13800138000", code));
    }
}
