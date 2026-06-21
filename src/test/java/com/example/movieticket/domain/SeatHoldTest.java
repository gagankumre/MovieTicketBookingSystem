package com.example.movieticket.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.movieticket.domain.enums.HoldStatus;
import com.example.movieticket.support.factory.SeatHoldFactory;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SeatHoldTest {

    private static final Instant NOW = Instant.parse("2026-06-21T10:00:00Z");

    @Test
    void isExpiredTrueWhenNowAfterExpiry() {
        SeatHold hold = SeatHoldFactory.hold(NOW.minusSeconds(1));

        assertThat(hold.isExpired(NOW)).isTrue();
    }

    @Test
    void isExpiredFalseWhenNowBeforeExpiry() {
        SeatHold hold = SeatHoldFactory.hold(NOW.plusSeconds(60));

        assertThat(hold.isExpired(NOW)).isFalse();
    }

    @Test
    void isExpiredFalseExactlyAtExpiry() {
        SeatHold hold = SeatHoldFactory.hold(NOW);

        assertThat(hold.isExpired(NOW)).isFalse();
    }

    @Test
    void newHoldStartsActive() {
        assertThat(SeatHoldFactory.hold(NOW).getStatus()).isEqualTo(HoldStatus.ACTIVE);
    }

    @Test
    void casStatusOnlySucceedsFromExpectedState() {
        SeatHold hold = SeatHoldFactory.hold(NOW);

        assertThat(hold.casStatus(HoldStatus.EXPIRED, HoldStatus.RELEASED)).isFalse();
        assertThat(hold.casStatus(HoldStatus.ACTIVE, HoldStatus.CONVERTED)).isTrue();
        assertThat(hold.getStatus()).isEqualTo(HoldStatus.CONVERTED);
    }
}
