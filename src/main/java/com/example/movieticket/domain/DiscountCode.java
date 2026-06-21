package com.example.movieticket.domain;

import com.example.movieticket.domain.enums.DiscountType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "discount_code", uniqueConstraints = @UniqueConstraint(
        name = "uk_discount_code", columnNames = "code"))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiscountCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType type;

    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal value;

    @Column(name = "max_discount", precision = 12, scale = 2)
    private BigDecimal maxDiscount;

    @Column(name = "min_booking_amount", precision = 12, scale = 2)
    private BigDecimal minBookingAmount;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_to", nullable = false)
    private Instant validTo;

    @Column(name = "usage_limit", nullable = false)
    private int usageLimit;

    @Column(name = "used_count", nullable = false)
    private int usedCount;

    @Column(nullable = false)
    private boolean active = true;

    @Version
    private long version;

    public DiscountCode(String code, DiscountType type, BigDecimal value, BigDecimal maxDiscount,
                        BigDecimal minBookingAmount, Instant validFrom, Instant validTo, int usageLimit) {
        this.code = code;
        this.type = type;
        this.value = value;
        this.maxDiscount = maxDiscount;
        this.minBookingAmount = minBookingAmount;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.usageLimit = usageLimit;
    }

    public boolean isValid(Instant now, BigDecimal subtotal) {
        if (!active) {
            return false;
        }
        if (now.isBefore(validFrom) || now.isAfter(validTo)) {
            return false;
        }
        if (usageLimit > 0 && usedCount >= usageLimit) {
            return false;
        }
        return minBookingAmount == null || subtotal.compareTo(minBookingAmount) >= 0;
    }

    /**
     * Discount this code applies to the given subtotal, capped at {@code maxDiscount} (if set)
     * and never exceeding the subtotal itself. Does not check validity — call {@link #isValid}.
     */
    public BigDecimal computeDiscount(BigDecimal subtotal) {
        BigDecimal discount = type == DiscountType.PERCENT
                ? subtotal.multiply(value).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : value;
        if (maxDiscount != null && discount.compareTo(maxDiscount) > 0) {
            discount = maxDiscount;
        }
        if (discount.compareTo(subtotal) > 0) {
            discount = subtotal;
        }
        return discount.setScale(2, RoundingMode.HALF_UP);
    }
}
