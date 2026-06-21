package com.example.movieticket.domain;

import com.example.movieticket.domain.enums.HoldStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A time-bound reservation over one or more {@link ShowSeat}s. The covered seats are found via
 * {@code ShowSeat.currentHoldId} while the hold is {@link HoldStatus#ACTIVE}.
 */
@Entity
@Table(name = "seat_hold",
        indexes = @Index(name = "idx_seat_hold_status_expires", columnList = "status, expires_at"))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeatHold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HoldStatus status = HoldStatus.ACTIVE;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public SeatHold(User user, Show show, Instant expiresAt) {
        this.user = user;
        this.show = show;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }

    public boolean casStatus(HoldStatus expected, HoldStatus next) {
        if (this.status != expected) {
            return false;
        }
        this.status = next;
        return true;
    }
}
