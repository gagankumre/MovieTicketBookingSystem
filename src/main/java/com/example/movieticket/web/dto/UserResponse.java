package com.example.movieticket.web.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserResponse {

    Long id;
    String email;
    String role;
}
