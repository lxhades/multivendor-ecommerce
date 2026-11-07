package com.inventory_service.application.stockreservation.command;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record ReserveCheckoutCommand(
        UUID eventId,
        UUID checkoutId,
        UUID buyerId,
        List<Item> items
) {
    public ReserveCheckoutCommand {
        Objects.requireNonNull(eventId, "Mã sự kiện không được để trống");
        Objects.requireNonNull(checkoutId, "Mã phiên thanh toán không được để trống");
        Objects.requireNonNull(buyerId, "Mã người mua không được để trống");
        Objects.requireNonNull(items, "Danh sách sản phẩm không được để trống");

        items = List.copyOf(items);

        if (items.isEmpty()) {
            throw new IllegalArgumentException("Danh sách sản phẩm không được để trống");
        }

        Set<String> uniqueItems = new HashSet<>();

        for (Item item : items) {
            Objects.requireNonNull(item, "Sản phẩm không được để trống");

            String key = item.orderId() + ":" + item.variantId();

            if (!uniqueItems.add(key)) {
                throw new IllegalArgumentException(
                        "Trùng cặp mã đơn hàng và mã biến thể: " + key
                );
            }
        }
    }

    public record Item(
            UUID orderId,
            UUID variantId,
            long quantity
    ) {
        public Item {
            Objects.requireNonNull(orderId);
            Objects.requireNonNull(variantId);

            if (quantity <= 0) {
                throw new IllegalArgumentException(
                        "Số lượng phải lớn hơn 0"
                );
            }
        }
    }
}