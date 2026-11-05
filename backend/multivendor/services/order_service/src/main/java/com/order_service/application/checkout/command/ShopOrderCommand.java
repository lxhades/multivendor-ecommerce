package com.order_service.application.checkout.command;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record ShopOrderCommand(UUID shopId, List<OrderItemCommand> items, long shippingFee) {
    public ShopOrderCommand {
        Objects.requireNonNull(shopId);
        items = List.copyOf(Objects.requireNonNull(items));
        if (items.isEmpty() || shippingFee < 0)
            throw new IllegalArgumentException("Invalid shop");
    }
}
