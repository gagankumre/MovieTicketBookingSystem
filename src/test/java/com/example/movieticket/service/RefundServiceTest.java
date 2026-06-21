package com.example.movieticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.movieticket.domain.RefundPolicy;
import com.example.movieticket.repository.RefundPolicyRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

    private static final Instant SHOW_START = Instant.parse("2026-07-01T18:00:00Z");
    private static final BigDecimal AMOUNT = new BigDecimal("200.00");

    @Mock
    private RefundPolicyRepository refundPolicyRepository;

    private RefundService refundService;

    @BeforeEach
    void setUp() {
        refundService = new RefundService(refundPolicyRepository, null);
        // policy: >=48h -> 100%, >=6h -> 50%, >=0h -> 0%
        when(refundPolicyRepository.findAllByOrderByHoursBeforeShowDesc()).thenReturn(List.of(
                new RefundPolicy(48, 100),
                new RefundPolicy(6, 50),
                new RefundPolicy(0, 0)));
    }

    private BigDecimal refundWhenHoursBefore(long hours) {
        return refundService.computeRefund(AMOUNT, SHOW_START, SHOW_START.minus(hours, ChronoUnit.HOURS));
    }

    @Test
    void fullRefundWellBeforeShow() {
        assertThat(refundWhenHoursBefore(72)).isEqualByComparingTo("200.00");
    }

    @Test
    void fullRefundExactlyAtTopThreshold() {
        assertThat(refundWhenHoursBefore(48)).isEqualByComparingTo("200.00");
    }

    @Test
    void halfRefundInMiddleBand() {
        assertThat(refundWhenHoursBefore(24)).isEqualByComparingTo("100.00");
    }

    @Test
    void halfRefundExactlyAtMiddleThreshold() {
        assertThat(refundWhenHoursBefore(6)).isEqualByComparingTo("100.00");
    }

    @Test
    void noRefundJustBelowMiddleThreshold() {
        assertThat(refundWhenHoursBefore(5)).isEqualByComparingTo("0.00");
    }

    @Test
    void noRefundAfterShowStarted() {
        BigDecimal refund = refundService.computeRefund(AMOUNT, SHOW_START, SHOW_START.plus(1, ChronoUnit.HOURS));
        assertThat(refund).isEqualByComparingTo("0.00");
    }
}
