package com.example.movieticket.service;

import com.example.movieticket.domain.RefundPolicy;
import com.example.movieticket.exception.DuplicateResourceException;
import com.example.movieticket.mapper.RefundPolicyMapper;
import com.example.movieticket.repository.RefundPolicyRepository;
import com.example.movieticket.web.dto.RefundPolicyRequest;
import com.example.movieticket.web.dto.RefundPolicyResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundPolicyRepository refundPolicyRepository;
    private final RefundPolicyMapper refundPolicyMapper;

    @Transactional
    public RefundPolicyResponse createPolicy(int hoursBeforeShow, int refundPercent) {
        if (refundPolicyRepository.existsByHoursBeforeShow(hoursBeforeShow)) {
            throw new DuplicateResourceException("A refund rule for " + hoursBeforeShow + "h already exists");
        }
        RefundPolicy saved = refundPolicyRepository.save(new RefundPolicy(hoursBeforeShow, refundPercent));
        log.info("Created refund rule id={} >= {}h -> {}%", saved.getId(), hoursBeforeShow, refundPercent);
        return refundPolicyMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<RefundPolicyResponse> listPolicies() {
        return refundPolicyMapper.toResponseList(refundPolicyRepository.findAllByOrderByHoursBeforeShowDesc());
    }

    /**
     * Refund due for cancelling {@code amount} now, given the show start: the percent of the
     * highest-threshold rule whose {@code hoursBeforeShow} is still satisfied; 0 if none apply
     * (including once the show has started).
     */
    @Transactional(readOnly = true)
    public BigDecimal computeRefund(BigDecimal amount, Instant showStart, Instant now) {
        long hoursUntilShow = Duration.between(now, showStart).toHours();
        int percent = refundPolicyRepository.findAllByOrderByHoursBeforeShowDesc().stream()
                .filter(policy -> hoursUntilShow >= policy.getHoursBeforeShow())
                .map(RefundPolicy::getRefundPercent)
                .findFirst()
                .orElse(0);
        return amount.multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
