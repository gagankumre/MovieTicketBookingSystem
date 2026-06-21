package com.example.movieticket.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Outbox row for asynchronous notification delivery: written off the booking transaction so
 * confirmation/cancellation messaging never blocks the booking flow, and retried on failure.
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

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private int attempts;

    public NotificationOutbox(String type, String recipient, String payload, String status) {
        this.type = type;
        this.recipient = recipient;
        this.payload = payload;
        this.status = status;
    }
}
