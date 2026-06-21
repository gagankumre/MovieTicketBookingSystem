package com.example.movieticket.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedActionException extends DomainException {

    public UnauthorizedActionException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.FORBIDDEN;
    }
}
