package com.order_service.adapter.out.persistence.mapper;

import com.order_service.adapter.out.persistence.entity.OrderItemJpaEntity;
import com.order_service.adapter.out.persistence.entity.OrderJpaEntity;
import com.order_service.domain.model.aggregate.Order;
import com.order_service.domain.model.entity.OrderItem;
import com.order_service.domain.model.enums.OrderStatus;
import com.order_service.domain.model.enums.PaymentMethod;
import com.order_service.domain.model.enums.PaymentStatus;

import java.util.List;

public final class OrderJpaMapper {

    private OrderJpaMapper() {
    }

    public static OrderJpaEntity toJpa(Order o) {
        var e = new OrderJpaEntity();

        e.id = o.getId();
        e.checkoutId = o.getCheckoutId();
        e.buyerId = o.getBuyerId();
        e.shopId = o.getShopId();
        e.shippingAddress = o.getShippingAddress();
        e.subtotal = o.getSubtotal();
        e.shippingFee = o.getShippingFee();
        e.totalAmount = o.getTotalAmount();
        e.paymentMethod = o.getPaymentMethod().name();
        e.paymentStatus = o.getPaymentStatus().name();
        e.orderStatus = o.getOrderStatus().name();
        e.createdAt = o.getCreatedAt();
        e.updatedAt = o.getUpdatedAt();
        e.version = o.getVersion();

        for (OrderItem i : o.getItems()) {
            var child = new OrderItemJpaEntity();

            child.id = i.getId();
            child.order = e;
            child.productId = i.getProductId();
            child.variantId = i.getVariantId();
            child.productName = i.getProductName();
            child.unitPrice = i.getUnitPrice();
            child.quantity = i.getQuantity();
            child.subtotal = i.subtotal();

            e.items.add(child);
        }

        return e;
    }

    public static Order toDomain(OrderJpaEntity e) {
        List<OrderItem> items = e.items.stream()
                .map(i -> OrderItem.restore(
                        i.id,
                        i.productId,
                        i.variantId,
                        i.productName,
                        i.unitPrice,
                        i.quantity
                ))
                .toList();

        Order o = Order.restore(
                e.id,
                e.checkoutId,
                e.buyerId,
                e.shopId,
                items,
                e.shippingAddress,
                e.shippingFee,
                PaymentMethod.valueOf(e.paymentMethod),
                PaymentStatus.valueOf(e.paymentStatus),
                OrderStatus.valueOf(e.orderStatus),
                e.createdAt,
                e.updatedAt,
                e.version
        );

        if (o.getSubtotal() != e.subtotal || o.getTotalAmount() != e.totalAmount) {
            throw new IllegalStateException("Stored order totals do not match items");
        }

        return o;
    }
}