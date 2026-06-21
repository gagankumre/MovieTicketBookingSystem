package com.example.movieticket.scheduler;

import com.example.movieticket.service.HoldExpiryService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically releases expired seat holds. Interval is {@code app.hold.sweep-interval-ms}. The
 * actual work lives in {@link HoldExpiryService} so it can be invoked deterministically in tests.
 */
@Component
@RequiredArgsConstructor
public class HoldSweeperJob {

    private final HoldExpiryService holdExpiryService;

    @Scheduled(fixedDelayString = "${app.hold.sweep-interval-ms}")
    public void sweepExpiredHolds() {
        holdExpiryService.expireDueHolds(Instant.now());
    }
}
