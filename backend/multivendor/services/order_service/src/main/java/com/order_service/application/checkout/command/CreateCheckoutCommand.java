package com.order_service.application.checkout.command;

import com.order_service.domain.model.enums.PaymentMethod;

import java.util.*;

public record CreateCheckoutCommand(
        UUID buyerId,
        String idempotencyKey,
        PaymentMethod paymentMethod,
        String shippingAddress,
        List<ShopOrderCommand> shops
) {

    public CreateCheckoutCommand {
        Objects.requireNonNull(buyerId);
        Objects.requireNonNull(paymentMethod);

        if (idempotencyKey == null || idempotencyKey.isBlank()
                || shippingAddress == null || shippingAddress.isBlank()) {
            throw new IllegalArgumentException("Invalid checkout");
        }

        shops = List.copyOf(Objects.requireNonNull(shops));

        if (shops.isEmpty()) {
            throw new IllegalArgumentException("Empty checkout");
        }

        Set<UUID> ids = new HashSet<>();

        for (ShopOrderCommand shop : shops) {
            if (!ids.add(shop.shopId())) {
                throw new IllegalArgumentException("Duplicate shop");
            }
        }
    }
}