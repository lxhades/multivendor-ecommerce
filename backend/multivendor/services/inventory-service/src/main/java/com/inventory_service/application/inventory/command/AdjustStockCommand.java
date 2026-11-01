package com.inventory_service.application.inventory.command;

import java.util.UUID;

public record AdjustStockCommand(
        UUID variantId,
        long newQuantity
) {}