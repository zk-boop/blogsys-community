package com.zk.projectboot.ratelimit;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class RateLimitService {

    private final Clock clock;
    private final ConcurrentMap<String, WindowCounter> counters = new ConcurrentHashMap<>();

    public RateLimitService(Clock clock) {
        this.clock = clock;
    }

    public boolean tryAcquire(String key, int limit, Duration window) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Rate-limit key must not be blank");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Rate limit must be positive");
        }

        long windowMillis = window.toMillis();
        if (windowMillis <= 0) {
            throw new IllegalArgumentException("Rate-limit window must be positive");
        }

        long nowMillis = clock.millis();
        long windowStart = nowMillis - Math.floorMod(nowMillis, windowMillis);
        AtomicBoolean acquired = new AtomicBoolean(false);

        counters.compute(key, (ignored, current) -> {
            if (current == null || current.windowStart != windowStart) {
                acquired.set(true);
                return new WindowCounter(windowStart, 1);
            }
            if (current.count >= limit) {
                return current;
            }
            current.count++;
            acquired.set(true);
            return current;
        });

        return acquired.get();
    }

    private static final class WindowCounter {
        private final long windowStart;
        private int count;

        private WindowCounter(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
