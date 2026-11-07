package com.inventory_service.application.stockreservation.command;

import java.util.UUID;

public record ReserveStockCommand(
        UUID orderId,
        UUID variantId,
        long quantity
) {
    public ReserveStockCommand {
        if (orderId == null) {
            throw new IllegalArgumentException("Mã đơn hàng không được để trống");
        }
        if (variantId == null) {
            throw new IllegalArgumentException("Mã biến thể không được để trống");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
    }
}