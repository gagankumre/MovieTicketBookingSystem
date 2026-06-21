package com.example.movieticket.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class RefundPolicyRequest {

    @NotNull
    @PositiveOrZero
    private Integer hoursBeforeShow;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer refundPercent;
}
