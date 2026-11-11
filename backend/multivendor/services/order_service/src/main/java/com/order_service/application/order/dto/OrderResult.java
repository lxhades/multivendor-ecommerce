package com.order_service.application.order.dto;

import com.order_service.domain.model.aggregate.Order;
import com.order_service.domain.model.entity.OrderItem;
import com.order_service.domain.model.enums.OrderStatus;
import com.order_service.domain.model.enums.PaymentMethod;
import com.order_service.domain.model.enums.PaymentStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResult(
        UUID id,
        UUID checkoutId,
        UUID buyerId,
        UUID shopId,
        String shippingAddress,
        List<Item> items,
        long subtotal,
        long shippingFee,
        long totalAmount,
        OrderStatus orderStatus,
        PaymentStatus paymentStatus,
        PaymentMethod paymentMethod,
        Instant createdAt,
        Instant updatedAt
) {

    public static OrderResult from(Order order) {
        return new OrderResult(
                order.getId(),
                order.getCheckoutId(),
                order.getBuyerId(),
                order.getShopId(),
                order.getShippingAddress(),
                order.getItems().stream()
                        .map(Item::from)
                        .toList(),
                order.getSubtotal(),
                order.getShippingFee(),
                order.getTotalAmount(),
                order.getOrderStatus(),
                order.getPaymentStatus(),
                order.getPaymentMethod(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    public record Item(
            UUID id,
            UUID productId,
            UUID variantId,
            String productName,
            String variantName,
            String variantImageUrl,
            long unitPrice,
            int quantity
    ) {
        private static Item from(OrderItem item) {
            return new Item(
                    item.getId(),
                    item.getProductId(),
                    item.getVariantId(),
                    item.getProductName(),
                    item.getVariantName(),
                    item.getVariantImageUrl(),
                    item.getUnitPrice(),
                    item.getQuantity()
            );
        }
    }
}