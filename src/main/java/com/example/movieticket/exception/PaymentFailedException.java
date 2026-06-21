package com.example.movieticket.exception;

import org.springframework.http.HttpStatus;

public class PaymentFailedException extends DomainException {

    public PaymentFailedException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.PAYMENT_REQUIRED;
    }
}
