package com.example.movieticket.payment;

/** Outcome of a gateway charge or refund: success carries a gateway reference, failure a reason. */
public record PaymentResult(boolean success, String reference, String failureReason) {

    public static PaymentResult success(String reference) {
        return new PaymentResult(true, reference, null);
    }

    public static PaymentResult declined(String failureReason) {
        return new PaymentResult(false, null, failureReason);
    }
}
