
package com.inventory_service.application.stockreservation.command;

import java.util.Objects;
import java.util.UUID;

public record ReserveOrderItem(
        UUID variantId,
        long quantity
) {
    public ReserveOrderItem {
        Objects.requireNonNull(variantId, "Mã biến thể không được để trống");
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Số lượng phải lớn hơn 0"
            );
        }
    }
}
