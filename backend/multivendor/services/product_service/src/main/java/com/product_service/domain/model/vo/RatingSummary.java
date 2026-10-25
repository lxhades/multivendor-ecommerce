package com.product_service.domain.model.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.product_service.domain.shared.Checks.required;
import static com.product_service.domain.shared.Checks.rule;

/**
 * Recomputed from visible reviews, never by adding rounded averages.
 */
public record RatingSummary(BigDecimal average, long count) {
    public RatingSummary {
        required(average, "average");
        rule(count >= 0, "Negative count");
        rule(average.signum() >= 0 && average.compareTo(BigDecimal.valueOf(5)) <= 0, "Rating outside 0..5");
        rule(count != 0 || average.signum() == 0, "Empty rating must be zero");
        average = average.setScale(2, RoundingMode.HALF_UP);
    }

    public static RatingSummary empty() {
        return new RatingSummary(BigDecimal.ZERO, 0);
    }

    public static RatingSummary fromTotal(long totalStars, long count) {
        rule(count >= 0 && totalStars >= 0, "Invalid rating totals");
        if (count == 0) {
            rule(totalStars == 0, "Invalid empty total");
            return empty();
        }
        BigDecimal sum = BigDecimal.valueOf(totalStars);
        rule(sum.compareTo(BigDecimal.valueOf(count)) >= 0 && sum.compareTo(BigDecimal.valueOf(count).multiply(BigDecimal.valueOf(5))) <= 0, "Invalid rating totals");
        return new RatingSummary(sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP), count);
    }
}
