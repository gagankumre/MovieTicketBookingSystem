package com.example.movieticket.exception;

import org.springframework.http.HttpStatus;

public class HoldExpiredException extends DomainException {

    public HoldExpiredException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }
}
