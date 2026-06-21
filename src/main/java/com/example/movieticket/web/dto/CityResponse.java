package com.example.movieticket.web.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CityResponse {

    Long id;
    String name;
}
