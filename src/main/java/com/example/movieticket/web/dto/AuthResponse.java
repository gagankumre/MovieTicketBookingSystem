package com.example.movieticket.web.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthResponse {

    String token;
    String tokenType;
    long expiresInMinutes;
}
