package com.example.movieticket.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ShowResponse {

    Long id;
    Long screenId;
    String screenName;
    Long movieId;
    String movieTitle;
    Instant startTime;
    Instant endTime;
    String showType;
    BigDecimal basePrice;
    int totalSeats;
}
