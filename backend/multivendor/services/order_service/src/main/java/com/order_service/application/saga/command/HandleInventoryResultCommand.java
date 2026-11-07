package com.order_service.application.saga.command;

import java.util.Objects;
import java.util.UUID;

public record HandleInventoryResultCommand(
        UUID eventId,
        String eventType,
        UUID checkoutId,
        UUID causationEventId,
        String reason
) {
    public HandleInventoryResultCommand {
        Objects.requireNonNull(eventId, "Mã sự kiện không được để trống");
        Objects.requireNonNull(eventType, "Loại sự kiện không được để trống");
        Objects.requireNonNull(checkoutId, "Mã phiên thanh toán không được để trống");
        Objects.requireNonNull(
                causationEventId,
                "Mã sự kiện nguyên nhân không được để trống"
        );

        if (!eventType.equals("InventoryReserved")
                && !eventType.equals("InventoryFailed")) {
            throw new IllegalArgumentException(
                    "Loại sự kiện không được hỗ trợ: " + eventType
            );
        }

        if (eventType.equals("InventoryFailed")
                && (reason == null || reason.isBlank())) {
            throw new IllegalArgumentException(
                    "Sự kiện InventoryFailed phải có lý do thất bại"
            );
        }
    }
}