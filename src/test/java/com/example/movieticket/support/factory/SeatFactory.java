package com.example.movieticket.support.factory;

import com.example.movieticket.domain.Screen;
import com.example.movieticket.domain.Seat;
import com.example.movieticket.domain.enums.SeatCategory;

public final class SeatFactory {

    private SeatFactory() {
    }

    public static Seat seat(Screen screen, String rowLabel, int seatNumber, SeatCategory category) {
        return new Seat(screen, rowLabel, seatNumber, category);
    }

    public static Seat withId(Long id, Seat seat) {
        seat.setId(id);
        return seat;
    }
}
