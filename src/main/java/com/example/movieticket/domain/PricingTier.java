package com.example.movieticket.domain;

import com.example.movieticket.domain.enums.SeatCategory;
import com.example.movieticket.domain.enums.ShowType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Admin-configurable price rule keyed by (seat category, show type). Resolved price is
 * {@code basePrice * multiplier + surcharge}.
 */
@Entity
@Table(name = "pricing_tier", uniqueConstraints = @UniqueConstraint(
        name = "uk_pricing_tier_category_showtype", columnNames = {"category", "show_type"}))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PricingTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "show_type", nullable = false)
    private ShowType showType;

    @Column(nullable = false, precision = 6, scale = 3)
    private BigDecimal multiplier;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal surcharge;

    public PricingTier(SeatCategory category, ShowType showType, BigDecimal multiplier, BigDecimal surcharge) {
        this.category = category;
        this.showType = showType;
        this.multiplier = multiplier;
        this.surcharge = surcharge;
    }
}
