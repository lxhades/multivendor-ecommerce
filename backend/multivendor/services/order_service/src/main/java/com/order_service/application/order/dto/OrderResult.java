package com.order_service.application.order.dto;

import com.order_service.domain.model.aggregate.Order;
import com.order_service.domain.model.enums.OrderStatus;
import com.order_service.domain.model.enums.PaymentMethod;
import com.order_service.domain.model.enums.PaymentStatus;

import java.util.UUID;

public record OrderResult(
        UUID id,
        UUID checkoutId,
        UUID buyerId,
        UUID shopId,
        long subtotal,
        long shippingFee,
        long totalAmount,
        OrderStatus orderStatus,
        PaymentStatus paymentStatus,
        PaymentMethod paymentMethod
) {

    public static OrderResult from(Order o) {
        return new OrderResult(
                o.getId(),
                o.getCheckoutId(),
                o.getBuyerId(),
                o.getShopId(),
                o.getSubtotal(),
                o.getShippingFee(),
                o.getTotalAmount(),
                o.getOrderStatus(),
                o.getPaymentStatus(),
                o.getPaymentMethod()
        );
    }
}