package com.example.movieticket.web.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SeatLayoutResponse {

    Long screenId;
    int totalSeats;
}
