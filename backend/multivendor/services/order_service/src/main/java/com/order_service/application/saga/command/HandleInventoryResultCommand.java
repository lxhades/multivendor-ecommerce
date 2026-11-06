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
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(eventType, "eventType is required");
        Objects.requireNonNull(checkoutId, "checkoutId is required");
        Objects.requireNonNull(
                causationEventId,
                "causationEventId is required"
        );

        if (!eventType.equals("InventoryReserved")
                && !eventType.equals("InventoryFailed")) {
            throw new IllegalArgumentException(
                    "Unsupported eventType: " + eventType
            );
        }

        if (eventType.equals("InventoryFailed")
                && (reason == null || reason.isBlank())) {
            throw new IllegalArgumentException(
                    "InventoryFailed requires reason"
            );
        }
    }
}