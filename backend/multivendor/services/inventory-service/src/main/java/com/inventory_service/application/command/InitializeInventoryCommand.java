package com.inventory_service.application.command;

import java.util.UUID;

public record InitializeInventoryCommand(
        UUID productId,
        UUID variantId,
        UUID shopId
) {
}