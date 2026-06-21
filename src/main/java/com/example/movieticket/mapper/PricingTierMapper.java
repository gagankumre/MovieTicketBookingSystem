package com.example.movieticket.mapper;

import com.example.movieticket.domain.PricingTier;
import com.example.movieticket.web.dto.PricingTierResponse;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PricingTierMapper {

    PricingTierResponse toResponse(PricingTier pricingTier);

    List<PricingTierResponse> toResponseList(List<PricingTier> pricingTiers);
}
