package com.example.movieticket.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequest {

    @NotNull
    private Long holdId;

    private String discountCode;

    @NotBlank
    private String paymentMethod;
}
