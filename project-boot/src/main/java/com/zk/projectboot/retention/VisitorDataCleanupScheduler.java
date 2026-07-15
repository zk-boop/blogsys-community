package com.zk.projectboot.retention;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;

@Component
@ConditionalOnProperty(name = "cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class VisitorDataCleanupScheduler {

    private final VisitorDataCleanupService cleanupService;
    private final Clock clock;

    public VisitorDataCleanupScheduler(VisitorDataCleanupService cleanupService, Clock clock) {
        this.cleanupService = cleanupService;
        this.clock = clock;
    }

    @Scheduled(cron = "${cleanup.cron:0 30 3 * * *}", zone = "Asia/Shanghai")
    public void cleanupExpiredVisitors() {
        cleanupService.cleanupExpiredVisitors(clock.instant().minus(Duration.ofDays(7)));
    }
}
