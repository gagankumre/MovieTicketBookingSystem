package com.example.movieticket.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BookingSummaryResponse {

    Long bookingId;
    Long showId;
    String status;
    BigDecimal subtotal;
    BigDecimal discountAmount;
    BigDecimal totalAmount;
    String discountCode;
    Instant createdAt;
}
