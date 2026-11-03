
package com.order_service.domain.repository;

import com.order_service.domain.model.aggregate.Checkout;

import java.util.Optional;
import java.util.UUID;

public interface CheckoutRepository {

    Checkout save(Checkout checkout);

    Optional<Checkout> findById(UUID checkoutId);

    Optional<Checkout> findByBuyerIdAndIdempotencyKey(
            UUID buyerId,
            String idempotencyKey
    );
}
