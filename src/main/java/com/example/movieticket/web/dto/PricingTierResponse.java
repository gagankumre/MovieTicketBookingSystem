package com.example.movieticket.web.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PricingTierResponse {

    Long id;
    String category;
    String showType;
    BigDecimal multiplier;
    BigDecimal surcharge;
}
