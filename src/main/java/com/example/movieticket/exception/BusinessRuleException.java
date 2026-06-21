package com.example.movieticket.exception;

import org.springframework.http.HttpStatus;

/** A request that is well-formed but violates a business precondition (e.g. publishing a show on a
 * screen that has no seat layout). */
public class BusinessRuleException extends DomainException {

    public BusinessRuleException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }
}
