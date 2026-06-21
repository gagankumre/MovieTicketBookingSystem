package com.example.movieticket.web.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ScreenResponse {

    Long id;
    Long theaterId;
    String theaterName;
    String name;
}
