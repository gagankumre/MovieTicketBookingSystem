package com.example.movieticket.service;

import java.math.BigDecimal;

/** Result of applying a discount code during booking: the normalized code and the amount off. */
public record AppliedDiscount(String code, BigDecimal amount) {
}
