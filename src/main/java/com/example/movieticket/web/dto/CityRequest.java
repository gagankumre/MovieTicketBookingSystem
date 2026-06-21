package com.example.movieticket.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CityRequest {

    @NotBlank
    @Size(max = 100)
    private String name;
}
