package com.example.movieticket.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One rule of the refund policy: if a cancellation happens at least {@code hoursBeforeShow} hours
 * before the show starts, {@code refundPercent} of the booking is refunded. The applicable rule is
 * the one with the largest matching {@code hoursBeforeShow}.
 */
@Entity
@Table(name = "refund_policy")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefundPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hours_before_show", nullable = false)
    private int hoursBeforeShow;

    @Column(name = "refund_percent", nullable = false)
    private int refundPercent;

    public RefundPolicy(int hoursBeforeShow, int refundPercent) {
        this.hoursBeforeShow = hoursBeforeShow;
        this.refundPercent = refundPercent;
    }
}
