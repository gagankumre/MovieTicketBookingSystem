package com.example.movieticket.exception;

import org.springframework.http.HttpStatus;

/**
 * Base for domain failures. Each subtype declares the HTTP status it maps to; the global handler
 * translates it into the standard error response. Services throw these — never set HTTP status.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    public abstract HttpStatus getStatus();
}
