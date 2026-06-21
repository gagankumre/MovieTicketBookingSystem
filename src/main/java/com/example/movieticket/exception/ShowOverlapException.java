package com.example.movieticket.exception;

import org.springframework.http.HttpStatus;

public class ShowOverlapException extends DomainException {

    public ShowOverlapException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }
}
