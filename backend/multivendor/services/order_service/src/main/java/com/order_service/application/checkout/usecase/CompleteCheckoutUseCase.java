package com.order_service.application.checkout.usecase;

import com.order_service.application.checkout.dto.CheckoutResult;
import com.order_service.domain.model.aggregate.Checkout;
import com.order_service.domain.repository.CheckoutRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class CompleteCheckoutUseCase {
    private final CheckoutRepository checkouts;
    private final Clock clock;

    public CompleteCheckoutUseCase(CheckoutRepository checkouts, Clock clock) {
        this.checkouts = checkouts;
        this.clock = clock;
    }

    @Transactional
    public CheckoutResult execute(UUID checkoutId) {

        Checkout checkout = checkouts.findById(checkoutId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên thanh toán"));
        checkout.complete(clock.instant());
        return CheckoutResult.from(checkouts.save(checkout));
    }
}
