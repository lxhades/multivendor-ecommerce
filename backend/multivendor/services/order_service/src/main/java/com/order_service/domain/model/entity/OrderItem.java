
package com.order_service.domain.model.entity;

import java.util.Objects;
import java.util.UUID;

public class OrderItem {

    private final UUID id;
    private final UUID productId;
    private final UUID variantId;
    private final String productName;
    private final long unitPrice;
    private final int quantity;

    private OrderItem(
            UUID id,
            UUID productId,
            UUID variantId,
            String productName,
            long unitPrice,
            int quantity
    ) {
        this.id = Objects.requireNonNull(id);
        this.productId = Objects.requireNonNull(productId);
        this.variantId = Objects.requireNonNull(variantId);
        this.productName = Objects.requireNonNull(productName);

        if (productName.isBlank()) {
            throw new IllegalArgumentException(
                    "Product name cannot be blank"
            );
        }

        if (unitPrice < 0) {
            throw new IllegalArgumentException(
                    "Unit price cannot be negative"
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be positive"
            );
        }

        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public static OrderItem create(
            UUID id,
            UUID productId,
            UUID variantId,
            String productName,
            long unitPrice,
            int quantity
    ) {
        return new OrderItem(
                id, productId, variantId,
                productName, unitPrice, quantity
        );
    }

    public long subtotal() {
        return Math.multiplyExact(unitPrice, (long) quantity);
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getVariantId() {
        return variantId;
    }

    public String getProductName() {
        return productName;
    }

    public long getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }
}
