package com.example.movieticket.domain;

import com.example.movieticket.domain.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String method;

    @Column(name = "gateway_ref")
    private String gatewayRef;

    public Payment(Booking booking, BigDecimal amount, String method) {
        this.booking = booking;
        this.amount = amount;
        this.method = method;
        this.status = PaymentStatus.SUCCESS;
    }

    public void markSuccess(String gatewayRef) {
        this.status = PaymentStatus.SUCCESS;
        this.gatewayRef = gatewayRef;
    }

    public void markRefunded() {
        this.status = PaymentStatus.REFUNDED;
    }
}
