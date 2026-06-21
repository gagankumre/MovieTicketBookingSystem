package com.example.movieticket.domain;

import com.example.movieticket.domain.enums.NotificationStatus;
import com.example.movieticket.domain.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Transactional-outbox row for asynchronous notification delivery. Written in the same transaction
 * as the booking/cancellation (so it is durable and the booking flow never blocks on sending), then
 * dispatched and retried off-thread by a scheduled job.
 */
@Entity
@Table(name = "notification_outbox", indexes = @Index(name = "idx_outbox_status", columnList = "status"))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    /** Correlation/dedup key (e.g. SHOW_REMINDER:bookingId) so a notification is enqueued once. */
    @Column(name = "reference")
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(nullable = false)
    private int attempts;

    public NotificationOutbox(NotificationType type, String recipient, String payload, String reference) {
        this.type = type;
        this.recipient = recipient;
        this.payload = payload;
        this.reference = reference;
    }

    public void markSent() {
        this.status = NotificationStatus.SENT;
    }

    public void recordFailedAttempt(int maxAttempts) {
        this.attempts++;
        if (this.attempts >= maxAttempts) {
            this.status = NotificationStatus.FAILED;
        }
    }
}
