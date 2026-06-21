package com.example.movieticket.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class HoldResponse {

    Long holdId;
    Long showId;
    Instant expiresAt;
    List<HeldSeat> seats;
    BigDecimal totalAmount;
}
