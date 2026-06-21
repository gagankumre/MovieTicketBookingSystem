package com.example.movieticket.exception;

import org.springframework.http.HttpStatus;

public class SeatUnavailableException extends DomainException {

    public SeatUnavailableException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }
}
