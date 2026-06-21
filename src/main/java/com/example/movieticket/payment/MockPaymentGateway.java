package com.example.movieticket.payment;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Deterministic stand-in for a real PSP. Charges succeed with a generated reference, except when
 * the payment method is {@link #DECLINE_METHOD} — the hook used to exercise the payment-failure
 * (402) path in tests and demos. Refunds always succeed.
 */
@Slf4j
@Component
public class MockPaymentGateway implements PaymentGateway {

    public static final String DECLINE_METHOD = "DECLINE";

    @Override
    public PaymentResult charge(BigDecimal amount, String method) {
        if (DECLINE_METHOD.equalsIgnoreCase(method)) {
            log.info("Mock gateway declining charge of {}", amount);
            return PaymentResult.declined("Payment declined by gateway");
        }
        String reference = "PAY-" + UUID.randomUUID();
        log.info("Mock gateway charged {} via {} ref={}", amount, method, reference);
        return PaymentResult.success(reference);
    }

    @Override
    public PaymentResult refund(String reference, BigDecimal amount) {
        String refundReference = "RFND-" + UUID.randomUUID();
        log.info("Mock gateway refunded {} for {} ref={}", amount, reference, refundReference);
        return PaymentResult.success(refundReference);
    }
}
