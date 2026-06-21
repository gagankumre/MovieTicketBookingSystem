package com.example.movieticket.support.factory;

import com.example.movieticket.domain.DiscountCode;
import com.example.movieticket.domain.enums.DiscountType;
import java.math.BigDecimal;
import java.time.Instant;

public final class DiscountCodeFactory {

    public static final Instant NOW = Instant.parse("2026-06-21T10:00:00Z");
    public static final Instant FROM = NOW.minusSeconds(3600);
    public static final Instant TO = NOW.plusSeconds(3600);

    private DiscountCodeFactory() {
    }

    public static DiscountCode percent(BigDecimal value) {
        return percent(value, null, null);
    }

    public static DiscountCode percent(BigDecimal value, BigDecimal maxDiscount) {
        return percent(value, maxDiscount, null);
    }

    public static DiscountCode percent(BigDecimal value, BigDecimal maxDiscount, BigDecimal minBooking) {
        return new DiscountCode("SAVE", DiscountType.PERCENT, value, maxDiscount, minBooking, FROM, TO, 100);
    }

    public static DiscountCode flat(BigDecimal value) {
        return flat(value, null);
    }

    public static DiscountCode flat(BigDecimal value, BigDecimal minBooking) {
        return new DiscountCode("FLAT", DiscountType.FLAT, value, null, minBooking, FROM, TO, 100);
    }
}
