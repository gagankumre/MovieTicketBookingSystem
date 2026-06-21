package com.example.movieticket.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MovieRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = 50)
    private String language;

    @NotNull
    @Min(1)
    private Integer durationMinutes;

    @Size(max = 20)
    private String certification;
}
