
package com.order_service.domain.model.aggregate;

import com.order_service.domain.model.entity.OrderItem;
import com.order_service.domain.model.enums.OrderStatus;
import com.order_service.domain.model.enums.PaymentMethod;
import com.order_service.domain.model.enums.PaymentStatus;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Order {

    private final UUID id;
    private final UUID checkoutId;
    private final UUID buyerId;
    private final UUID shopId;

    private final List<OrderItem> items;
    private final String shippingAddress;

    private final long subtotal;
    private final long shippingFee;
    private final long discountAmount;
    private final long totalAmount;

    private final PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private OrderStatus orderStatus;

    private final Instant createdAt;
    private Instant updatedAt;
    private final Long version;

    private Order(
            UUID id,
            UUID checkoutId,
            UUID buyerId,
            UUID shopId,
            List<OrderItem> items,
            String shippingAddress,
            long shippingFee,
            long discountAmount,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus,
            OrderStatus orderStatus,
            Instant createdAt,
            Instant updatedAt,
            Long version
    ) {
        this.id = Objects.requireNonNull(id);
        this.checkoutId = Objects.requireNonNull(checkoutId);
        this.buyerId = Objects.requireNonNull(buyerId);
        this.shopId = Objects.requireNonNull(shopId);

        Objects.requireNonNull(items, "items are required");

        if (items.isEmpty()) {
            throw new IllegalArgumentException(
                    "Order must contain at least one item"
            );
        }

        this.items = List.copyOf(items);

        this.shippingAddress =
                Objects.requireNonNull(shippingAddress);

        if (shippingAddress.isBlank()) {
            throw new IllegalArgumentException(
                    "Shipping address cannot be blank"
            );
        }

        if (shippingFee < 0 || discountAmount < 0) {
            throw new IllegalArgumentException(
                    "Shipping fee and discount cannot be negative"
            );
        }

        this.subtotal = calculateSubtotal(this.items);
        this.shippingFee = shippingFee;
        this.discountAmount = discountAmount;

        long beforeDiscount =
                Math.addExact(subtotal, shippingFee);

        if (discountAmount > beforeDiscount) {
            throw new IllegalArgumentException(
                    "Discount exceeds order amount"
            );
        }

        this.totalAmount =
                Math.subtractExact(beforeDiscount, discountAmount);

        this.paymentMethod =
                Objects.requireNonNull(paymentMethod);
        this.paymentStatus =
                Objects.requireNonNull(paymentStatus);
        this.orderStatus =
                Objects.requireNonNull(orderStatus);

        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.version = version;
    }



    public static Order create(
            UUID id,
            UUID checkoutId,
            UUID buyerId,
            UUID shopId,
            List<OrderItem> items,
            String shippingAddress,
            long shippingFee,
            long discountAmount,
            PaymentMethod paymentMethod,
            Instant now
    ) {
        Objects.requireNonNull(now, "now is required");

        return new Order(
                id,
                checkoutId,
                buyerId,
                shopId,
                items,
                shippingAddress,
                shippingFee,
                discountAmount,
                paymentMethod,
                PaymentStatus.UNPAID,
                OrderStatus.PENDING_PAYMENT,
                now,
                now,
                null
        );
    }



    public static Order restore(
            UUID id,
            UUID checkoutId,
            UUID buyerId,
            UUID shopId,
            List<OrderItem> items,
            String shippingAddress,
            long shippingFee,
            long discountAmount,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus,
            OrderStatus orderStatus,
            Instant createdAt,
            Instant updatedAt,
            Long version
    ) {
        return new Order(
                id,
                checkoutId,
                buyerId,
                shopId,
                items,
                shippingAddress,
                shippingFee,
                discountAmount,
                paymentMethod,
                paymentStatus,
                orderStatus,
                createdAt,
                updatedAt,
                version
        );
    }


    private static long calculateSubtotal(List<OrderItem> items) {
        long total = 0;

        for (OrderItem item : items) {
            total = Math.addExact(
                    total,
                    Objects.requireNonNull(item).subtotal()
            );
        }

        return total;
    }

    public void markPaid(Instant now) {
        Objects.requireNonNull(now);

        if (paymentStatus == PaymentStatus.PAID) {
            return;
        }

        if (orderStatus != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException(
                    "Order is not pending payment"
            );
        }

        this.paymentStatus = PaymentStatus.PAID;
        this.orderStatus = OrderStatus.CONFIRMED;
        this.updatedAt = now;
    }

    public void confirmCod(Instant now) {
        Objects.requireNonNull(now);

        if (paymentMethod != PaymentMethod.COD) {
            throw new IllegalStateException(
                    "Order payment method is not COD"
            );
        }

        if (orderStatus == OrderStatus.CONFIRMED) {
            return;
        }

        if (orderStatus != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException(
                    "Cannot confirm order in current status"
            );
        }

        this.orderStatus = OrderStatus.CONFIRMED;
        this.updatedAt = now;
    }

    public void startProcessing(Instant now) {
        transition(
                OrderStatus.CONFIRMED,
                OrderStatus.PROCESSING,
                now
        );
    }

    public void markShipped(Instant now) {
        transition(
                OrderStatus.PROCESSING,
                OrderStatus.SHIPPED,
                now
        );
    }

    public void markDelivered(Instant now) {
        transition(
                OrderStatus.SHIPPED,
                OrderStatus.DELIVERED,
                now
        );
    }

    public void cancel(Instant now) {
        Objects.requireNonNull(now);

        if (orderStatus == OrderStatus.CANCELLED) {
            return;
        }

        if (orderStatus != OrderStatus.PENDING_PAYMENT
                && orderStatus != OrderStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Order cannot be cancelled in current status"
            );
        }

        this.orderStatus = OrderStatus.CANCELLED;
        this.updatedAt = now;
    }

    private void transition(
            OrderStatus expected,
            OrderStatus target,
            Instant now
    ) {
        Objects.requireNonNull(now);

        if (orderStatus != expected) {
            throw new IllegalStateException(
                    "Invalid order transition: "
                            + orderStatus + " -> " + target
            );
        }

        this.orderStatus = target;
        this.updatedAt = now;
    }


    public UUID getId() { return id; }
    public UUID getCheckoutId() { return checkoutId; }
    public UUID getBuyerId() { return buyerId; }
    public UUID getShopId() { return shopId; }

    public List<OrderItem> getItems() { return items; }
    public String getShippingAddress() { return shippingAddress; }

    public long getSubtotal() { return subtotal; }
    public long getShippingFee() { return shippingFee; }
    public long getDiscountAmount() { return discountAmount; }
    public long getTotalAmount() { return totalAmount; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public OrderStatus getOrderStatus() { return orderStatus; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
}
