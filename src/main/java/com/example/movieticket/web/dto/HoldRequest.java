package com.example.movieticket.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class HoldRequest {

    @NotNull
    private Long showId;

    @NotEmpty
    private List<Long> seatIds;
}
