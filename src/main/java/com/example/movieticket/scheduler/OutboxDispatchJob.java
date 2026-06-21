package com.example.movieticket.scheduler;

import com.example.movieticket.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Periodically delivers pending outbox notifications (and retries failed ones). */
@Component
@RequiredArgsConstructor
public class OutboxDispatchJob {

    private final NotificationService notificationService;

    @Scheduled(fixedDelayString = "${app.notification.dispatch-interval-ms}")
    public void dispatch() {
        notificationService.dispatchPending();
    }
}
