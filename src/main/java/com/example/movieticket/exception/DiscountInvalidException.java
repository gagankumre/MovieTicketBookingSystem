package com.example.movieticket.exception;

import org.springframework.http.HttpStatus;

public class DiscountInvalidException extends DomainException {

    public DiscountInvalidException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
