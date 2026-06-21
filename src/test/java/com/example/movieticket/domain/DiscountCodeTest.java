package com.example.movieticket.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.movieticket.support.factory.DiscountCodeFactory;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class DiscountCodeTest {

    @Test
    void percentDiscountComputed() {
        DiscountCode code = DiscountCodeFactory.percent(new BigDecimal("10"));

        assertThat(code.computeDiscount(new BigDecimal("250.00"))).isEqualByComparingTo("25.00");
    }

    @Test
    void percentDiscountCappedAtMax() {
        DiscountCode code = DiscountCodeFactory.percent(new BigDecimal("50"), new BigDecimal("100.00"));

        assertThat(code.computeDiscount(new BigDecimal("1000.00"))).isEqualByComparingTo("100.00");
    }

    @Test
    void flatDiscountNeverExceedsSubtotal() {
        DiscountCode code = DiscountCodeFactory.flat(new BigDecimal("500.00"));

        assertThat(code.computeDiscount(new BigDecimal("200.00"))).isEqualByComparingTo("200.00");
    }

    @Test
    void validWhenInsideWindowAndMeetsMinimum() {
        DiscountCode code = DiscountCodeFactory.percent(new BigDecimal("10"), null, new BigDecimal("100.00"));

        assertThat(code.isValid(DiscountCodeFactory.NOW, new BigDecimal("150.00"))).isTrue();
    }

    @Test
    void invalidBelowMinimumBookingAmount() {
        DiscountCode code = DiscountCodeFactory.percent(new BigDecimal("10"), null, new BigDecimal("100.00"));

        assertThat(code.isValid(DiscountCodeFactory.NOW, new BigDecimal("50.00"))).isFalse();
    }

    @Test
    void invalidOutsideValidityWindow() {
        DiscountCode code = DiscountCodeFactory.percent(new BigDecimal("10"));

        assertThat(code.isValid(DiscountCodeFactory.TO.plusSeconds(1), new BigDecimal("150.00"))).isFalse();
        assertThat(code.isValid(DiscountCodeFactory.FROM.minusSeconds(1), new BigDecimal("150.00"))).isFalse();
    }

    @Test
    void invalidWhenInactive() {
        DiscountCode code = DiscountCodeFactory.percent(new BigDecimal("10"));
        code.setActive(false);

        assertThat(code.isValid(DiscountCodeFactory.NOW, new BigDecimal("150.00"))).isFalse();
    }

    @Test
    void invalidWhenUsageLimitReached() {
        DiscountCode code = DiscountCodeFactory.percent(new BigDecimal("10"));
        code.setUsedCount(100);

        assertThat(code.isValid(DiscountCodeFactory.NOW, new BigDecimal("150.00"))).isFalse();
    }
}
