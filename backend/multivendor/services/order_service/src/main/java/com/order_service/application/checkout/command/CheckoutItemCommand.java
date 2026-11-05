
package com.order_service.application.checkout.command;

import java.util.Objects;
import java.util.UUID;

public record CheckoutItemCommand(
        UUID variantId,
        int quantity
) {
    public CheckoutItemCommand {
        Objects.requireNonNull(variantId, "variantId required");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Invalid quantity");
        }
    }
}
