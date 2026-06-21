package com.example.movieticket.web.dto;

import com.example.movieticket.domain.enums.ShowType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Data;

@Data
public class ShowRequest {

    @NotNull
    private Long screenId;

    @NotNull
    private Long movieId;

    @NotNull
    @Future
    private Instant startTime;

    @NotNull
    private ShowType showType;

    @NotNull
    @Positive
    private BigDecimal basePrice;
}
