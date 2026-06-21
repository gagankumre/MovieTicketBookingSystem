package com.example.movieticket.service;

import com.example.movieticket.domain.enums.SeatCategory;
import com.example.movieticket.domain.enums.ShowType;
import com.example.movieticket.repository.PricingTierRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resolves the price of a seat from the show's base price and the (seat category × show type)
 * pricing tier: {@code basePrice * multiplier + surcharge}. When no tier is configured the base
 * price applies unchanged.
 */
@Service
@RequiredArgsConstructor
public class PricingService {

    private final PricingTierRepository pricingTierRepository;

    @Transactional(readOnly = true)
    public BigDecimal resolvePrice(BigDecimal basePrice, SeatCategory category, ShowType showType) {
        return pricingTierRepository.findByCategoryAndShowType(category, showType)
                .map(tier -> basePrice.multiply(tier.getMultiplier()).add(tier.getSurcharge()))
                .orElse(basePrice)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
