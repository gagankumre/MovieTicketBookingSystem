package com.example.movieticket.web.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TheaterResponse {

    Long id;
    Long cityId;
    String cityName;
    String name;
    String address;
}
