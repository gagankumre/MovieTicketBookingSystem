package com.example.movieticket.support.factory;

import com.example.movieticket.domain.SeatHold;
import java.time.Instant;

public final class SeatHoldFactory {

    private SeatHoldFactory() {
    }

    public static SeatHold hold(Instant expiresAt) {
        return new SeatHold(null, null, expiresAt);
    }
}
