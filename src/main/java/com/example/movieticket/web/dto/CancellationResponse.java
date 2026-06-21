package com.example.movieticket.web.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CancellationResponse {

    Long bookingId;
    String status;
    BigDecimal refundAmount;
    String refundStatus;
}
