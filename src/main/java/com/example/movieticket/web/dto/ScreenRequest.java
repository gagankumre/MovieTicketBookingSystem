package com.example.movieticket.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ScreenRequest {

    @NotNull
    private Long theaterId;

    @NotBlank
    @Size(max = 100)
    private String name;
}
