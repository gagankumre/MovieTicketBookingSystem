package com.example.movieticket.notification;

/** Published when a notification is written to the outbox, so it can be dispatched promptly
 * after the enclosing transaction commits. */
public record NotificationEnqueuedEvent(Long outboxId) {
}
