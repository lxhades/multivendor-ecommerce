package com.inventory_service.application.inventory.command;

import java.util.UUID;

public record InitializeInventoryCommand(
        UUID productId,
        UUID variantId,
        UUID shopId
) {
}