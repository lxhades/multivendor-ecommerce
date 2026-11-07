
package com.order_service.application.checkout.command;

import java.util.Objects;
import java.util.UUID;

public record CheckoutItemCommand(
        UUID variantId,
        int quantity
) {
    public CheckoutItemCommand {
        Objects.requireNonNull(variantId, "Mã biến thể không được để trống");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng không hợp lệ");
        }
    }
}
