package com.example.movieticket.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends DomainException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }
}
