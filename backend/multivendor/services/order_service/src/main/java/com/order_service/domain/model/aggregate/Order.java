package com.order_service.domain.model.aggregate;

import com.order_service.domain.model.entity.OrderItem;
import com.order_service.domain.model.enums.OrderStatus;
import com.order_service.domain.model.enums.PaymentMethod;
import com.order_service.domain.model.enums.PaymentStatus;

import java.time.Instant;
import java.util.*;

public final class Order {

    private final UUID id;
    private final UUID checkoutId;
    private final UUID buyerId;
    private final UUID shopId;

    private final List<OrderItem> items;
    private final String shippingAddress;

    private final long subtotal;
    private final long shippingFee;
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

        this.items = List.copyOf(Objects.requireNonNull(items));

        if (this.items.isEmpty()) {
            throw new IllegalArgumentException("Empty order");
        }

        Set<UUID> seenVariantIds = new HashSet<>();

        for (OrderItem item : this.items) {
            if (!seenVariantIds.add(item.getVariantId())) {
                throw new IllegalArgumentException("Duplicate variantId");
            }
        }

        if (shippingAddress == null || shippingAddress.isBlank()) {
            throw new IllegalArgumentException("Shipping address required");
        }
        this.shippingAddress = shippingAddress;

        if (shippingFee < 0) {
            throw new IllegalArgumentException("Negative shipping fee");
        }
        this.shippingFee = shippingFee;

        long calculatedSubtotal = 0;

        for (OrderItem item : this.items) {
            calculatedSubtotal = Math.addExact(calculatedSubtotal, item.subtotal());
        }

        this.subtotal = calculatedSubtotal;
        this.totalAmount = Math.addExact(calculatedSubtotal, shippingFee);

        this.paymentMethod = Objects.requireNonNull(paymentMethod);
        this.paymentStatus = Objects.requireNonNull(paymentStatus);
        this.orderStatus = Objects.requireNonNull(orderStatus);

        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);

        if (updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("Invalid timestamps");
        }

        if (version != null && version < 0) {
            throw new IllegalArgumentException("Negative version");
        }

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
            PaymentMethod paymentMethod,
            Instant now
    ) {
        return new Order(
                id,
                checkoutId,
                buyerId,
                shopId,
                items,
                shippingAddress,
                shippingFee,
                paymentMethod,
                PaymentStatus.UNPAID,
                paymentMethod == PaymentMethod.COD
                        ? OrderStatus.PENDING
                        : OrderStatus.PENDING_PAYMENT,
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
                paymentMethod,
                paymentStatus,
                orderStatus,
                createdAt,
                updatedAt,
                version
        );
    }

    private void validateTime(Instant now) {
        Objects.requireNonNull(now);

        if (now.isBefore(updatedAt)) {
            throw new IllegalArgumentException("Time went backwards");
        }
    }

    public void markPaid(Instant now) {
        validateTime(now);

        if (paymentStatus == PaymentStatus.PAID) {
            return;
        }

        if (paymentMethod == PaymentMethod.COD) {
            throw new IllegalStateException("COD cannot use online paid flow");
        }

        if (orderStatus != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Not pending payment");
        }

        paymentStatus = PaymentStatus.PAID;
        orderStatus = OrderStatus.CONFIRMED;
        updatedAt = now;
    }

    public void confirmCod(Instant now) {
        validateTime(now);

        if (paymentMethod != PaymentMethod.COD) {
            throw new IllegalStateException("Not COD");
        }

        if (orderStatus == OrderStatus.CONFIRMED) {
            return;
        }

        if (orderStatus != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Cannot confirm");
        }

        orderStatus = OrderStatus.CONFIRMED;
        updatedAt = now;
    }

    private void transition(
            OrderStatus expectedStatus,
            OrderStatus targetStatus,
            Instant now
    ) {
        validateTime(now);

        if (orderStatus == targetStatus) {
            return;
        }

        if (orderStatus != expectedStatus) {
            throw new IllegalStateException(
                    "Invalid transition: " + orderStatus + " -> " + targetStatus
            );
        }

        orderStatus = targetStatus;
        updatedAt = now;
    }

    public void startProcessing(Instant now) {
        transition(OrderStatus.CONFIRMED, OrderStatus.PROCESSING, now);
    }

    public void markShipped(Instant now) {
        transition(OrderStatus.PROCESSING, OrderStatus.SHIPPED, now);
    }

    public void markDelivered(Instant now) {

        transition(OrderStatus.SHIPPED, OrderStatus.DELIVERED, now);
        paymentStatus=PaymentStatus.PAID;
    }

    public void cancel(Instant now) {
        validateTime(now);

        if (orderStatus == OrderStatus.CANCELLED) {
            return;
        }

        if (orderStatus != OrderStatus.PENDING_PAYMENT
                && orderStatus != OrderStatus.PENDING
                && orderStatus != OrderStatus.CONFIRMED
        ) {
            throw new IllegalStateException("Cannot cancel");
        }

        if (paymentStatus == PaymentStatus.PAID) {
            throw new IllegalStateException("Refund flow not implemented");
        }

        orderStatus = OrderStatus.CANCELLED;
        updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCheckoutId() {
        return checkoutId;
    }

    public UUID getBuyerId() {
        return buyerId;
    }

    public UUID getShopId() {
        return shopId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public long getSubtotal() {
        return subtotal;
    }

    public long getShippingFee() {
        return shippingFee;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }
}