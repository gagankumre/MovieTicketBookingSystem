package com.example.movieticket.service;

import com.example.movieticket.domain.PricingTier;
import com.example.movieticket.domain.enums.SeatCategory;
import com.example.movieticket.domain.enums.ShowType;
import com.example.movieticket.exception.DuplicateResourceException;
import com.example.movieticket.mapper.PricingTierMapper;
import com.example.movieticket.repository.PricingTierRepository;
import com.example.movieticket.web.dto.PricingTierRequest;
import com.example.movieticket.web.dto.PricingTierResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resolves the price of a seat from the show's base price and the (seat category × show type)
 * pricing tier: {@code basePrice * multiplier + surcharge}. When no tier is configured the base
 * price applies unchanged. Also manages the admin-configured tiers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PricingService {

    private final PricingTierRepository pricingTierRepository;
    private final PricingTierMapper pricingTierMapper;

    @Transactional
    public PricingTierResponse createTier(PricingTierRequest request) {
        if (pricingTierRepository.existsByCategoryAndShowType(request.getCategory(), request.getShowType())) {
            throw new DuplicateResourceException(
                    "Pricing tier for " + request.getCategory() + "/" + request.getShowType() + " already exists");
        }
        PricingTier saved = pricingTierRepository.save(new PricingTier(request.getCategory(),
                request.getShowType(), request.getMultiplier(), request.getSurcharge()));
        log.info("Created pricing tier id={} {}/{}", saved.getId(), saved.getCategory(), saved.getShowType());
        return pricingTierMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PricingTierResponse> listTiers() {
        return pricingTierMapper.toResponseList(pricingTierRepository.findAll());
    }

    @Transactional(readOnly = true)
    public BigDecimal resolvePrice(BigDecimal basePrice, SeatCategory category, ShowType showType) {
        return pricingTierRepository.findByCategoryAndShowType(category, showType)
                .map(tier -> basePrice.multiply(tier.getMultiplier()).add(tier.getSurcharge()))
                .orElse(basePrice)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
