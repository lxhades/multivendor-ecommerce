
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
                "Mã sự kiện không được để trống"
        );
        Objects.requireNonNull(
                checkoutId,
                "Mã phiên thanh toán không được để trống"
        );
        Objects.requireNonNull(
                buyerId,
                "Mã người mua không được để trống"
        );
        Objects.requireNonNull(
                items,
                "Danh sách sản phẩm không được để trống"
        );
        if (items.isEmpty()) {
            throw new IllegalArgumentException(
                    "Danh sách sản phẩm giữ chỗ tồn kho không được để trống"
            );
        }

        //  Kiểm tra loại Event.
        if (!EVENT_TYPE.equals(eventType)) {
            throw new IllegalArgumentException(
                    "Loại sự kiện không hợp lệ: " + eventType
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
                        "Trùng mục giữ chỗ tồn kho: "
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
                    "Mã đơn hàng không được để trống"
            );

            Objects.requireNonNull(
                    variantId,
                    "Mã biến thể không được để trống"
            );

            if (quantity <= 0) {
                throw new IllegalArgumentException(
                        "Số lượng phải lớn hơn 0"
                );
            }
        }
    }

    private record ItemKey(
            UUID orderId,
            UUID variantId
    ) {}
}
