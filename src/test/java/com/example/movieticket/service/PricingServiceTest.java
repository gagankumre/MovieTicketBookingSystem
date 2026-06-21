package com.example.movieticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.movieticket.domain.PricingTier;
import com.example.movieticket.domain.enums.SeatCategory;
import com.example.movieticket.domain.enums.ShowType;
import com.example.movieticket.repository.PricingTierRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    private PricingTierRepository pricingTierRepository;

    @InjectMocks
    private PricingService pricingService;

    @Test
    void returnsBasePriceWhenNoTierConfigured() {
        when(pricingTierRepository.findByCategoryAndShowType(SeatCategory.REGULAR, ShowType.REGULAR))
                .thenReturn(Optional.empty());

        BigDecimal price = pricingService.resolvePrice(new BigDecimal("200.00"),
                SeatCategory.REGULAR, ShowType.REGULAR);

        assertThat(price).isEqualByComparingTo("200.00");
    }

    @Test
    void appliesMultiplierAndSurchargeFromTier() {
        PricingTier tier = new PricingTier(SeatCategory.PREMIUM, ShowType.WEEKEND,
                new BigDecimal("1.500"), new BigDecimal("50.00"));
        when(pricingTierRepository.findByCategoryAndShowType(SeatCategory.PREMIUM, ShowType.WEEKEND))
                .thenReturn(Optional.of(tier));

        BigDecimal price = pricingService.resolvePrice(new BigDecimal("200.00"),
                SeatCategory.PREMIUM, ShowType.WEEKEND);

        assertThat(price).isEqualByComparingTo("350.00");
    }
}
