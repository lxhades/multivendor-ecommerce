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
            throw new IllegalArgumentException("Đơn hàng phải có ít nhất một sản phẩm");
        }

        Set<UUID> seenVariantIds = new HashSet<>();

        for (OrderItem item : this.items) {
            if (!seenVariantIds.add(item.getVariantId())) {
                throw new IllegalArgumentException("Mã biến thể bị trùng");
            }
        }

        if (shippingAddress == null || shippingAddress.isBlank()) {
            throw new IllegalArgumentException("Địa chỉ giao hàng không được để trống");
        }
        this.shippingAddress = shippingAddress;

        if (shippingFee < 0) {
            throw new IllegalArgumentException("Phí vận chuyển không được là số âm");
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
            throw new IllegalArgumentException("Các mốc thời gian không hợp lệ");
        }

        if (version != null && version < 0) {
            throw new IllegalArgumentException("Phiên bản không được là số âm");
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
            throw new IllegalArgumentException("Thời điểm xử lý không được trước thời điểm cập nhật gần nhất");
        }
    }

    public void markPaid(Instant now) {
        validateTime(now);

        if (paymentStatus == PaymentStatus.PAID) {
            return;
        }

        if (paymentMethod == PaymentMethod.COD) {
            throw new IllegalStateException("Đơn thanh toán khi nhận hàng không thể sử dụng quy trình thanh toán trực tuyến");
        }

        if (orderStatus != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Đơn hàng không ở trạng thái chờ thanh toán");
        }

        paymentStatus = PaymentStatus.PAID;
        orderStatus = OrderStatus.CONFIRMED;
        updatedAt = now;
    }

    public void confirmCod(Instant now) {
        validateTime(now);

        if (paymentMethod != PaymentMethod.COD) {
            throw new IllegalStateException("Đơn hàng không sử dụng phương thức thanh toán khi nhận hàng");
        }

        if (orderStatus == OrderStatus.CONFIRMED) {
            return;
        }

        if (orderStatus != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Không thể xác nhận đơn thanh toán khi nhận hàng từ trạng thái " + orderStatus
            );
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
                    "Chuyển trạng thái không hợp lệ: " + orderStatus + " -> " + targetStatus
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
        if(paymentMethod == PaymentMethod.COD) {
        paymentStatus=PaymentStatus.PAID;
        }
        transition(OrderStatus.SHIPPED, OrderStatus.DELIVERED, now);
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
            throw new IllegalStateException("Không thể hủy đơn hàng");
        }

        if (paymentStatus == PaymentStatus.PAID) {
            throw new IllegalStateException("Chưa hỗ trợ quy trình hoàn tiền");
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