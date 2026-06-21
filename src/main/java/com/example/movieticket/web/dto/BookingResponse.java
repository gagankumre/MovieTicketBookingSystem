package com.example.movieticket.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BookingResponse {

    Long bookingId;
    Long showId;
    String status;
    BigDecimal subtotal;
    BigDecimal discountAmount;
    BigDecimal totalAmount;
    String discountCode;
    Instant createdAt;
    List<BookedSeat> seats;
    String paymentStatus;
    String paymentReference;
}
