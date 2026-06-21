package com.example.movieticket.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.movieticket.domain.enums.DiscountType;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class DiscountCodeTest {

    private static final Instant NOW = Instant.parse("2026-06-21T10:00:00Z");
    private static final Instant FROM = NOW.minusSeconds(3600);
    private static final Instant TO = NOW.plusSeconds(3600);

    private DiscountCode percent(BigDecimal value, BigDecimal maxDiscount, BigDecimal minBooking) {
        return new DiscountCode("SAVE", DiscountType.PERCENT, value, maxDiscount, minBooking, FROM, TO, 100);
    }

    private DiscountCode flat(BigDecimal value, BigDecimal minBooking) {
        return new DiscountCode("FLAT", DiscountType.FLAT, value, null, minBooking, FROM, TO, 100);
    }

    @Test
    void percentDiscountComputed() {
        DiscountCode code = percent(new BigDecimal("10"), null, null);

        assertThat(code.computeDiscount(new BigDecimal("250.00"))).isEqualByComparingTo("25.00");
    }

    @Test
    void percentDiscountCappedAtMax() {
        DiscountCode code = percent(new BigDecimal("50"), new BigDecimal("100.00"), null);

        assertThat(code.computeDiscount(new BigDecimal("1000.00"))).isEqualByComparingTo("100.00");
    }

    @Test
    void flatDiscountNeverExceedsSubtotal() {
        DiscountCode code = flat(new BigDecimal("500.00"), null);

        assertThat(code.computeDiscount(new BigDecimal("200.00"))).isEqualByComparingTo("200.00");
    }

    @Test
    void validWhenInsideWindowAndMeetsMinimum() {
        DiscountCode code = percent(new BigDecimal("10"), null, new BigDecimal("100.00"));

        assertThat(code.isValid(NOW, new BigDecimal("150.00"))).isTrue();
    }

    @Test
    void invalidBelowMinimumBookingAmount() {
        DiscountCode code = percent(new BigDecimal("10"), null, new BigDecimal("100.00"));

        assertThat(code.isValid(NOW, new BigDecimal("50.00"))).isFalse();
    }

    @Test
    void invalidOutsideValidityWindow() {
        DiscountCode code = percent(new BigDecimal("10"), null, null);

        assertThat(code.isValid(TO.plusSeconds(1), new BigDecimal("150.00"))).isFalse();
        assertThat(code.isValid(FROM.minusSeconds(1), new BigDecimal("150.00"))).isFalse();
    }

    @Test
    void invalidWhenInactive() {
        DiscountCode code = percent(new BigDecimal("10"), null, null);
        code.setActive(false);

        assertThat(code.isValid(NOW, new BigDecimal("150.00"))).isFalse();
    }

    @Test
    void invalidWhenUsageLimitReached() {
        DiscountCode code = percent(new BigDecimal("10"), null, null);
        code.setUsedCount(100);

        assertThat(code.isValid(NOW, new BigDecimal("150.00"))).isFalse();
    }
}
