package com.example.movieticket.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ShowSummaryResponse {

    Long id;
    Long movieId;
    String movieTitle;
    Long screenId;
    String screenName;
    String theaterName;
    String cityName;
    Instant startTime;
    Instant endTime;
    String showType;
    BigDecimal basePrice;
}
