package com.example.movieticket.support.factory;

import com.example.movieticket.domain.ShowSeat;
import java.math.BigDecimal;

public final class ShowSeatFactory {

    public static final BigDecimal DEFAULT_PRICE = new BigDecimal("200.00");

    private ShowSeatFactory() {
    }

    public static ShowSeat availableSeat() {
        return availableSeat(DEFAULT_PRICE);
    }

    public static ShowSeat availableSeat(BigDecimal price) {
        return new ShowSeat(null, null, price);
    }
}
