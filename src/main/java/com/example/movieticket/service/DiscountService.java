package com.example.movieticket.service;

import com.example.movieticket.domain.DiscountCode;
import com.example.movieticket.exception.DiscountInvalidException;
import com.example.movieticket.exception.DuplicateResourceException;
import com.example.movieticket.mapper.DiscountCodeMapper;
import com.example.movieticket.repository.DiscountCodeRepository;
import com.example.movieticket.web.dto.DiscountCodeRequest;
import com.example.movieticket.web.dto.DiscountCodeResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountService {

    private final DiscountCodeRepository discountCodeRepository;
    private final DiscountCodeMapper discountCodeMapper;

    @Transactional
    public DiscountCodeResponse create(DiscountCodeRequest request) {
        String code = request.getCode().trim();
        if (discountCodeRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateResourceException("Discount code '" + code + "' already exists");
        }
        if (!request.getValidTo().isAfter(request.getValidFrom())) {
            throw new DiscountInvalidException("validTo must be after validFrom");
        }
        DiscountCode saved = discountCodeRepository.save(new DiscountCode(code, request.getType(),
                request.getValue(), request.getMaxDiscount(), request.getMinBookingAmount(),
                request.getValidFrom(), request.getValidTo(), request.getUsageLimit()));
        log.info("Created discount code id={} code={}", saved.getId(), saved.getCode());
        return discountCodeMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DiscountCodeResponse> list() {
        return discountCodeMapper.toResponseList(discountCodeRepository.findAll(Sort.by("code")));
    }

    /**
     * Validates a code against the subtotal, computes the discount, and atomically consumes one use.
     * Intended to run inside the booking transaction so an unsuccessful booking rolls the usage back.
     */
    @Transactional
    public AppliedDiscount apply(String code, BigDecimal subtotal, Instant now) {
        DiscountCode discount = discountCodeRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new DiscountInvalidException("Unknown discount code '" + code + "'"));
        if (!discount.isValid(now, subtotal)) {
            throw new DiscountInvalidException("Discount code '" + code + "' is not applicable");
        }
        BigDecimal amount = discount.computeDiscount(subtotal);
        if (discountCodeRepository.incrementUsage(discount.getId()) == 0) {
            throw new DiscountInvalidException("Discount code '" + code + "' usage limit reached");
        }
        log.info("Applied discount code {} for {} off", discount.getCode(), amount);
        return new AppliedDiscount(discount.getCode(), amount);
    }
}
