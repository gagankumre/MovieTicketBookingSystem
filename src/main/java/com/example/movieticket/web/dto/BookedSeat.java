package com.example.movieticket.web.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BookedSeat {

    Long showSeatId;
    String rowLabel;
    int seatNumber;
    BigDecimal pricePaid;
}
