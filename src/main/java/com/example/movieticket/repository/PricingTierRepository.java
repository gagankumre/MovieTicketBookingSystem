package com.example.movieticket.repository;

import com.example.movieticket.domain.PricingTier;
import com.example.movieticket.domain.enums.SeatCategory;
import com.example.movieticket.domain.enums.ShowType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PricingTierRepository extends JpaRepository<PricingTier, Long> {

    Optional<PricingTier> findByCategoryAndShowType(SeatCategory category, ShowType showType);

    boolean existsByCategoryAndShowType(SeatCategory category, ShowType showType);
}
