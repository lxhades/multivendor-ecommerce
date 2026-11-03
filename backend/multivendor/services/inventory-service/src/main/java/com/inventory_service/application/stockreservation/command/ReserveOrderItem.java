
package com.inventory_service.application.stockreservation.command;

import java.util.Objects;
import java.util.UUID;

public record ReserveOrderItem(
        UUID variantId,
        long quantity
) {
    public ReserveOrderItem {
        Objects.requireNonNull(variantId, "variantId is required");
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "quantity must be positive"
            );
        }
    }
}
