package com.example.movieticket.web.dto;

import com.example.movieticket.domain.enums.SeatCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SeatRowSpec {

    @NotBlank
    @Size(max = 5)
    private String rowLabel;

    @NotNull
    @Min(1)
    private Integer seatCount;

    @NotNull
    private SeatCategory category;
}
