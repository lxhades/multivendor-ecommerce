
package com.order_service.application.checkout.command;

import com.order_service.domain.model.enums.PaymentMethod;
import java.util.*;

public record CreateCheckoutCommand(
        UUID buyerId,
        String idempotencyKey,
        PaymentMethod paymentMethod,
        String shippingAddress,
        List<CheckoutItemCommand> items
) {
    public CreateCheckoutCommand {
        Objects.requireNonNull(buyerId);
        Objects.requireNonNull(paymentMethod);

        if (idempotencyKey == null || idempotencyKey.isBlank()
                || shippingAddress == null || shippingAddress.isBlank()) {
            throw new IllegalArgumentException("Invalid checkout");
        }

        items = List.copyOf(Objects.requireNonNull(items));

        if (items.isEmpty()) {
            throw new IllegalArgumentException("Empty checkout");
        }

        Set<UUID> variants = new HashSet<>();
        for (CheckoutItemCommand item : items) {
            if (!variants.add(item.variantId())) {
                throw new IllegalArgumentException("Duplicate variantId");
            }
        }
    }
}
