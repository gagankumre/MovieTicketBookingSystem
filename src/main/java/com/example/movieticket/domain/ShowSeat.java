package com.example.movieticket.domain;

import com.example.movieticket.domain.enums.SeatStatus;
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
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Single source of truth for seat occupancy: exactly one row per (show, seat). All status
 * transitions run under a pessimistic lock (see SeatLockManager) with {@code version} as the
 * lost-update backstop; this is what makes double-allocation impossible while keeping seats
 * re-bookable after cancellation.
 */
@Entity
@Table(name = "show_seat",
        uniqueConstraints = @UniqueConstraint(name = "uk_show_seat_show_seat", columnNames = {"show_id", "seat_id"}),
        indexes = {
                @Index(name = "idx_show_seat_show", columnList = "show_id"),
                @Index(name = "idx_show_seat_hold", columnList = "current_hold_id")
        })
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShowSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status = SeatStatus.AVAILABLE;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "current_hold_id")
    private Long currentHoldId;

    @Column(name = "current_booking_id")
    private Long currentBookingId;

    @Version
    private long version;

    public ShowSeat(Show show, Seat seat, BigDecimal price) {
        this.show = show;
        this.seat = seat;
        this.price = price;
    }

    public boolean casStatus(SeatStatus expected, SeatStatus next) {
        if (this.status != expected) {
            return false;
        }
        this.status = next;
        return true;
    }

    public boolean block(Long holdId) {
        if (!casStatus(SeatStatus.AVAILABLE, SeatStatus.HELD)) {
            return false;
        }
        this.currentHoldId = holdId;
        return true;
    }

    public boolean confirm(Long bookingId) {
        if (!casStatus(SeatStatus.HELD, SeatStatus.BOOKED)) {
            return false;
        }
        this.currentHoldId = null;
        this.currentBookingId = bookingId;
        return true;
    }

    public boolean release() {
        if (this.status == SeatStatus.AVAILABLE) {
            return false;
        }
        this.status = SeatStatus.AVAILABLE;
        this.currentHoldId = null;
        this.currentBookingId = null;
        return true;
    }
}
