package com.inventory_service.application.inventory.command;

import java.util.UUID;

public record IncreaseStockCommand(
        UUID variantId,
        long quantity
) {}