package com.example.movieticket.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.movieticket.domain.enums.HoldStatus;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SeatHoldTest {

    private static final Instant NOW = Instant.parse("2026-06-21T10:00:00Z");

    @Test
    void isExpiredTrueWhenNowAfterExpiry() {
        SeatHold hold = new SeatHold(null, null, NOW.minusSeconds(1));

        assertThat(hold.isExpired(NOW)).isTrue();
    }

    @Test
    void isExpiredFalseWhenNowBeforeExpiry() {
        SeatHold hold = new SeatHold(null, null, NOW.plusSeconds(60));

        assertThat(hold.isExpired(NOW)).isFalse();
    }

    @Test
    void isExpiredFalseExactlyAtExpiry() {
        SeatHold hold = new SeatHold(null, null, NOW);

        assertThat(hold.isExpired(NOW)).isFalse();
    }

    @Test
    void newHoldStartsActive() {
        assertThat(new SeatHold(null, null, NOW).getStatus()).isEqualTo(HoldStatus.ACTIVE);
    }

    @Test
    void casStatusOnlySucceedsFromExpectedState() {
        SeatHold hold = new SeatHold(null, null, NOW);

        assertThat(hold.casStatus(HoldStatus.EXPIRED, HoldStatus.RELEASED)).isFalse();
        assertThat(hold.casStatus(HoldStatus.ACTIVE, HoldStatus.CONVERTED)).isTrue();
        assertThat(hold.getStatus()).isEqualTo(HoldStatus.CONVERTED);
    }
}
