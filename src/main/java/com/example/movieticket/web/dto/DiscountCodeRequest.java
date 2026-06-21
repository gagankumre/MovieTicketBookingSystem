package com.example.movieticket.web.dto;

import com.example.movieticket.domain.enums.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Data;

@Data
public class DiscountCodeRequest {

    @NotBlank
    private String code;

    @NotNull
    private DiscountType type;

    @NotNull
    @PositiveOrZero
    private BigDecimal value;

    @PositiveOrZero
    private BigDecimal maxDiscount;

    @PositiveOrZero
    private BigDecimal minBookingAmount;

    @NotNull
    private Instant validFrom;

    @NotNull
    private Instant validTo;

    @NotNull
    @PositiveOrZero
    private Integer usageLimit;
}
