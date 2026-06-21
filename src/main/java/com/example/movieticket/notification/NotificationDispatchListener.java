package com.example.movieticket.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Dispatches a freshly-enqueued notification immediately and off-thread, once the booking
 * transaction has committed. The scheduled {@code OutboxDispatchJob} remains the retry/safety net
 * for anything still pending (e.g. if this attempt fails or the app restarts).
 */
@Component
@RequiredArgsConstructor
public class NotificationDispatchListener {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotificationEnqueued(NotificationEnqueuedEvent event) {
        notificationService.dispatchOne(event.outboxId());
    }
}
