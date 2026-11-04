package com.order_service.application.checkout.usecase;

import com.order_service.application.checkout.dto.CheckoutResult;
import com.order_service.domain.model.aggregate.Checkout;
import com.order_service.domain.repository.CheckoutRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class ExpireCheckoutUseCase {
    private final CheckoutRepository checkouts;
    private final Clock clock;

    public ExpireCheckoutUseCase(CheckoutRepository checkouts, Clock clock) {
        this.checkouts = checkouts;
        this.clock = clock;
    }

    @Transactional
    public CheckoutResult execute(UUID checkoutId) {
        java.util.Objects.requireNonNull(checkoutId, "checkoutId is required");

        Checkout checkout = checkouts.findById(checkoutId)
                .orElseThrow(() -> new IllegalArgumentException("Checkout not found"));
        checkout.expire(clock.instant());

        return CheckoutResult.from(checkouts.save(checkout));
    }
}
