package com.example.movieticket.web.dto;

import com.example.movieticket.domain.enums.SeatCategory;
import com.example.movieticket.domain.enums.ShowType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class PricingTierRequest {

    @NotNull
    private SeatCategory category;

    @NotNull
    private ShowType showType;

    @NotNull
    @PositiveOrZero
    private BigDecimal multiplier;

    @NotNull
    @PositiveOrZero
    private BigDecimal surcharge;
}
