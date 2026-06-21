package com.example.movieticket.support.factory;

import com.example.movieticket.domain.Movie;
import com.example.movieticket.domain.Screen;
import com.example.movieticket.domain.Show;
import com.example.movieticket.domain.enums.ShowType;
import java.math.BigDecimal;
import java.time.Instant;

public final class ShowFactory {

    private ShowFactory() {
    }

    public static Show show(Screen screen, Movie movie, Instant startTime, Instant endTime,
                            ShowType showType, BigDecimal basePrice) {
        return new Show(screen, movie, startTime, endTime, showType, basePrice);
    }

    public static Show withId(Long id, Show show) {
        show.setId(id);
        return show;
    }
}
