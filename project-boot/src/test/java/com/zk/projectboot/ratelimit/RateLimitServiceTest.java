package com.zk.projectboot.ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitServiceTest {

    @Test
    void fixedWindowResetsAtTheNextBoundary() {
        MutableClock clock = new MutableClock(Instant.parse("2026-07-15T00:00:00Z"));
        RateLimitService service = new RateLimitService(clock);

        assertTrue(service.tryAcquire("register:127.0.0.1", 2, Duration.ofHours(1)));
        assertTrue(service.tryAcquire("register:127.0.0.1", 2, Duration.ofHours(1)));
        assertFalse(service.tryAcquire("register:127.0.0.1", 2, Duration.ofHours(1)));

        clock.advance(Duration.ofHours(1));

        assertTrue(service.tryAcquire("register:127.0.0.1", 2, Duration.ofHours(1)));
    }

    @Test
    void keysUseIndependentCounters() {
        MutableClock clock = new MutableClock(Instant.parse("2026-07-15T00:00:00Z"));
        RateLimitService service = new RateLimitService(clock);

        assertTrue(service.tryAcquire("login:127.0.0.1", 1, Duration.ofMinutes(10)));
        assertFalse(service.tryAcquire("login:127.0.0.1", 1, Duration.ofMinutes(10)));
        assertTrue(service.tryAcquire("login:127.0.0.2", 1, Duration.ofMinutes(10)));
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
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
}
