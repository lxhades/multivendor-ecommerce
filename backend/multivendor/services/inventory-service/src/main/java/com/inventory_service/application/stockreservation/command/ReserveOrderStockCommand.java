
package com.inventory_service.application.stockreservation.command;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public record ReserveOrderStockCommand(
        UUID orderId,
        List<ReserveOrderItem> items
) {
    public ReserveOrderStockCommand {
        Objects.requireNonNull(orderId, "Mã đơn hàng không được để trống");
        Objects.requireNonNull(items, "Danh sách sản phẩm không được để trống");

        items = List.copyOf(items);

        if (items.isEmpty()) {
            throw new IllegalArgumentException("Danh sách sản phẩm không được để trống");
        }

        if (items.stream()
                .collect(Collectors.groupingBy(
                        ReserveOrderItem::variantId,
                        Collectors.counting()))
                .values().stream().anyMatch(count -> count > 1)) {
            throw new IllegalArgumentException(
                    "Mã biến thể bị trùng trong đơn hàng"
            );
        }
    }
}
