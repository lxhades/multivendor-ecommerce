
package com.order_service.application.event;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record InventoryReservationRequested(
        UUID eventId,
        String eventType,
        UUID checkoutId,
        UUID buyerId,
        List<Item> items
) {

    public static final String EVENT_TYPE =
            "InventoryReservationRequested";

    public InventoryReservationRequested {


        Objects.requireNonNull(
                eventId,
                "eventId is required"
        );
        Objects.requireNonNull(
                checkoutId,
                "checkoutId is required"
        );
        Objects.requireNonNull(
                buyerId,
                "buyerId is required"
        );
        Objects.requireNonNull(
                items,
                "items is required"
        );
        if (items.isEmpty()) {
            throw new IllegalArgumentException(
                    "Inventory reservation items cannot be empty"
            );
        }

        //  Kiểm tra loại Event.
        if (!EVENT_TYPE.equals(eventType)) {
            throw new IllegalArgumentException(
                    "Invalid eventType: " + eventType
            );
        }

        items = List.copyOf(items);

        // Kiểm tra Item trùng trong Event.
        Set<ItemKey> uniqueItems = new HashSet<>();

        for (Item item : items) {

            // Item tự validate khi được khởi tạo.
            ItemKey key = new ItemKey(
                    item.orderId(),
                    item.variantId()
            );

            if (!uniqueItems.add(key)) {
                throw new IllegalArgumentException(
                        "Duplicate reservation item: "
                                + item.orderId()
                                + " / "
                                + item.variantId()
                );
            }
        }
    }

    public record Item(
            UUID orderId,
            UUID variantId,
            int quantity
    ) {

        public Item {

            Objects.requireNonNull(
                    orderId,
                    "orderId is required"
            );

            Objects.requireNonNull(
                    variantId,
                    "variantId is required"
            );

            if (quantity <= 0) {
                throw new IllegalArgumentException(
                        "quantity must be greater than 0"
                );
            }
        }
    }

    private record ItemKey(
            UUID orderId,
            UUID variantId
    ) {}
}
