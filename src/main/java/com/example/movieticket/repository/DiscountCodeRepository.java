package com.example.movieticket.repository;

import com.example.movieticket.domain.DiscountCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Long> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<DiscountCode> findByCodeIgnoreCase(String code);

    /**
     * Atomically consumes one use of a code, guarded against exceeding its limit (0 = unlimited).
     * Returns 1 when consumed, 0 when the limit was already reached — so concurrent redemptions
     * can never over-issue.
     */
    @Modifying
    @Query("update DiscountCode d set d.usedCount = d.usedCount + 1 "
            + "where d.id = :id and (d.usageLimit = 0 or d.usedCount < d.usageLimit)")
    int incrementUsage(@Param("id") Long id);
}
