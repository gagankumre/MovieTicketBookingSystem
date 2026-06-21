package com.example.movieticket.web.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MovieResponse {

    Long id;
    String title;
    String language;
    int durationMinutes;
    String certification;
}
