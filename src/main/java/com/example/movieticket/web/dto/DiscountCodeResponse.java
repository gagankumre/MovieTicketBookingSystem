package com.example.movieticket.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DiscountCodeResponse {

    Long id;
    String code;
    String type;
    BigDecimal value;
    BigDecimal maxDiscount;
    BigDecimal minBookingAmount;
    Instant validFrom;
    Instant validTo;
    int usageLimit;
    int usedCount;
    boolean active;
}
