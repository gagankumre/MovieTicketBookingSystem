package com.example.movieticket.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Historical line item linking a booking to one show seat, with the price paid at booking time.
 * Unique per (booking, showSeat) only — a cancelled-then-rebooked seat yields a new row.
 */
@Entity
@Table(name = "booking_seat", uniqueConstraints = @UniqueConstraint(
        name = "uk_booking_seat", columnNames = {"booking_id", "show_seat_id"}))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookingSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "show_seat_id", nullable = false)
    private ShowSeat showSeat;

    @Column(name = "price_paid", nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePaid;

    public BookingSeat(Booking booking, ShowSeat showSeat, BigDecimal pricePaid) {
        this.booking = booking;
        this.showSeat = showSeat;
        this.pricePaid = pricePaid;
    }
}
