
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
        Objects.requireNonNull(orderId, "orderId is required");
        Objects.requireNonNull(items, "items are required");

        items = List.copyOf(items);

        if (items.isEmpty()) {
            throw new IllegalArgumentException("items cannot be empty");
        }

        if (items.stream()
                .collect(Collectors.groupingBy(
                        ReserveOrderItem::variantId,
                        Collectors.counting()))
                .values().stream().anyMatch(count -> count > 1)) {
            throw new IllegalArgumentException(
                    "Duplicate variantId in order"
            );
        }
    }
}
