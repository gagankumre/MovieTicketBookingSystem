package com.example.movieticket.web.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class HeldSeat {

    Long showSeatId;
    String rowLabel;
    int seatNumber;
    BigDecimal price;
}
