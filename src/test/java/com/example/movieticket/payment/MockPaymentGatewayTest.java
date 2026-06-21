package com.example.movieticket.payment;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MockPaymentGatewayTest {

    private final MockPaymentGateway gateway = new MockPaymentGateway();

    @Test
    void chargeSucceedsWithReferenceForNormalMethod() {
        PaymentResult result = gateway.charge(new BigDecimal("450.00"), "CARD");

        assertThat(result.success()).isTrue();
        assertThat(result.reference()).startsWith("PAY-");
        assertThat(result.failureReason()).isNull();
    }

    @Test
    void chargeIsDeclinedForDeclineMethod() {
        PaymentResult result = gateway.charge(new BigDecimal("450.00"), MockPaymentGateway.DECLINE_METHOD);

        assertThat(result.success()).isFalse();
        assertThat(result.reference()).isNull();
        assertThat(result.failureReason()).isNotBlank();
    }

    @Test
    void refundAlwaysSucceeds() {
        PaymentResult result = gateway.refund("PAY-123", new BigDecimal("450.00"));

        assertThat(result.success()).isTrue();
        assertThat(result.reference()).startsWith("RFND-");
    }
}
