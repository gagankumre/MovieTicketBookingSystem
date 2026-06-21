package com.example.movieticket.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class SeatLayoutRequest {

    @NotEmpty
    @Valid
    private List<SeatRowSpec> rows;
}
