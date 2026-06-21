package com.example.movieticket.payment;

import java.math.BigDecimal;

/** Abstraction over a payment provider. The project ships a deterministic mock implementation. */
public interface PaymentGateway {

    PaymentResult charge(BigDecimal amount, String method);

    PaymentResult refund(String reference, BigDecimal amount);
}
