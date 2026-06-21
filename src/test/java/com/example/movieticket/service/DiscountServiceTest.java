package com.example.movieticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.movieticket.domain.DiscountCode;
import com.example.movieticket.exception.DiscountInvalidException;
import com.example.movieticket.mapper.DiscountCodeMapperImpl;
import com.example.movieticket.repository.DiscountCodeRepository;
import com.example.movieticket.support.factory.DiscountCodeFactory;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

    @Mock
    private DiscountCodeRepository discountCodeRepository;

    private DiscountService discountService;

    @BeforeEach
    void setUp() {
        discountService = new DiscountService(discountCodeRepository, new DiscountCodeMapperImpl());
    }

    private DiscountCode validPercentCode() {
        DiscountCode code = DiscountCodeFactory.percent(new BigDecimal("10"));
        code.setId(1L);
        return code;
    }

    @Test
    void applyComputesDiscountAndConsumesOneUse() {
        when(discountCodeRepository.findByCodeIgnoreCase("SAVE")).thenReturn(Optional.of(validPercentCode()));
        when(discountCodeRepository.incrementUsage(1L)).thenReturn(1);

        AppliedDiscount applied = discountService.apply("SAVE", new BigDecimal("250.00"), DiscountCodeFactory.NOW);

        assertThat(applied.code()).isEqualTo("SAVE");
        assertThat(applied.amount()).isEqualByComparingTo("25.00");
    }

    @Test
    void applyRejectsUnknownCode() {
        when(discountCodeRepository.findByCodeIgnoreCase("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> discountService.apply("NOPE", new BigDecimal("250.00"), DiscountCodeFactory.NOW))
                .isInstanceOf(DiscountInvalidException.class);
    }

    @Test
    void applyRejectsCodeBelowMinimum() {
        DiscountCode code = DiscountCodeFactory.percent(new BigDecimal("10"), null, new BigDecimal("100.00"));
        code.setId(1L);
        when(discountCodeRepository.findByCodeIgnoreCase("SAVE")).thenReturn(Optional.of(code));

        assertThatThrownBy(() -> discountService.apply("SAVE", new BigDecimal("50.00"), DiscountCodeFactory.NOW))
                .isInstanceOf(DiscountInvalidException.class);
    }

    @Test
    void applyRejectsWhenUsageLimitReached() {
        when(discountCodeRepository.findByCodeIgnoreCase("SAVE")).thenReturn(Optional.of(validPercentCode()));
        when(discountCodeRepository.incrementUsage(1L)).thenReturn(0);

        assertThatThrownBy(() -> discountService.apply("SAVE", new BigDecimal("250.00"), DiscountCodeFactory.NOW))
                .isInstanceOf(DiscountInvalidException.class);
    }

    @Test
    void createRejectsDuplicateCode() {
        var request = new com.example.movieticket.web.dto.DiscountCodeRequest();
        request.setCode("SAVE10");
        when(discountCodeRepository.existsByCodeIgnoreCase("SAVE10")).thenReturn(true);

        assertThatThrownBy(() -> discountService.create(request))
                .isInstanceOf(com.example.movieticket.exception.DuplicateResourceException.class);
    }

    @Test
    void createSavesValidCode() {
        var request = new com.example.movieticket.web.dto.DiscountCodeRequest();
        request.setCode("SAVE10");
        request.setType(com.example.movieticket.domain.enums.DiscountType.PERCENT);
        request.setValue(new BigDecimal("10"));
        request.setValidFrom(DiscountCodeFactory.FROM);
        request.setValidTo(DiscountCodeFactory.TO);
        request.setUsageLimit(100);
        when(discountCodeRepository.existsByCodeIgnoreCase("SAVE10")).thenReturn(false);
        when(discountCodeRepository.save(any(DiscountCode.class))).thenAnswer(inv -> {
            DiscountCode code = inv.getArgument(0);
            code.setId(9L);
            return code;
        });

        assertThat(discountService.create(request).getCode()).isEqualTo("SAVE10");
    }
}
