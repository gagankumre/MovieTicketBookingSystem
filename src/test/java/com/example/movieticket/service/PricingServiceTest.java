package com.example.movieticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import com.example.movieticket.domain.PricingTier;
import com.example.movieticket.domain.enums.SeatCategory;
import com.example.movieticket.domain.enums.ShowType;
import com.example.movieticket.exception.DuplicateResourceException;
import com.example.movieticket.mapper.PricingTierMapperImpl;
import com.example.movieticket.repository.PricingTierRepository;
import com.example.movieticket.web.dto.PricingTierRequest;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    private PricingTierRepository pricingTierRepository;

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        pricingService = new PricingService(pricingTierRepository, new PricingTierMapperImpl());
    }

    private PricingTierRequest tierRequest() {
        PricingTierRequest request = new PricingTierRequest();
        request.setCategory(SeatCategory.PREMIUM);
        request.setShowType(ShowType.WEEKEND);
        request.setMultiplier(new BigDecimal("1.500"));
        request.setSurcharge(new BigDecimal("50.00"));
        return request;
    }

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

    @Test
    void createTierRejectsDuplicate() {
        when(pricingTierRepository.existsByCategoryAndShowType(SeatCategory.PREMIUM, ShowType.WEEKEND))
                .thenReturn(true);

        assertThatThrownBy(() -> pricingService.createTier(tierRequest()))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void createTierSavesWhenNew() {
        when(pricingTierRepository.existsByCategoryAndShowType(SeatCategory.PREMIUM, ShowType.WEEKEND))
                .thenReturn(false);
        when(pricingTierRepository.save(any(PricingTier.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(pricingService.createTier(tierRequest()).getShowType()).isEqualTo("WEEKEND");
    }
}
