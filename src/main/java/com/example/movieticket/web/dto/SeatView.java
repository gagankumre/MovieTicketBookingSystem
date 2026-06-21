package com.example.movieticket.web.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SeatView {

    Long showSeatId;
    String rowLabel;
    int seatNumber;
    String category;
    String status;
    BigDecimal price;
}
