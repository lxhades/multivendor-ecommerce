package com.order_service.application.saga;


import com.order_service.application.saga.command.HandleInventoryResultCommand;
import com.order_service.application.saga.port.InboxPort;
import com.order_service.domain.model.aggregate.Checkout;
import com.order_service.domain.model.aggregate.Order;
import com.order_service.domain.model.enums.CheckoutStatus;
import com.order_service.domain.model.enums.PaymentMethod;
import com.order_service.domain.repository.CheckoutRepository;
import com.order_service.domain.repository.OrderRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
public class HandleInventoryResultUseCase {

    private static final String CONSUMER_NAME =
            "order-inventory-result-consumer";

    private final InboxPort inboxPort;
    private final CheckoutRepository checkoutRepository;
    private final OrderRepository orderRepository;
    private final Clock clock;

    public HandleInventoryResultUseCase(
            InboxPort inboxPort,
            CheckoutRepository checkoutRepository,
            OrderRepository orderRepository,
            Clock clock
    ) {
        this.inboxPort = Objects.requireNonNull(inboxPort);
        this.checkoutRepository =
                Objects.requireNonNull(checkoutRepository);
        this.orderRepository =
                Objects.requireNonNull(orderRepository);
        this.clock = Objects.requireNonNull(clock);
    }

    @Transactional
    public void execute(HandleInventoryResultCommand command) {

        Objects.requireNonNull(command);

        // 1. Inbox Idempotency
        boolean claimed = inboxPort.tryClaim(
                CONSUMER_NAME,
                command.eventId(),
                command.eventType()
        );

        if (!claimed) {
            return;
        }

        // 2. Load Checkout
        Checkout checkout = checkoutRepository
                .findById(command.checkoutId())
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy phiên thanh toán: " + command.checkoutId()
                ));

        // 3. Load tất cả Orders thuộc Checkout
        List<Order> orders = orderRepository
                .findByCheckoutId(command.checkoutId());

        if (orders.isEmpty()) {
            throw new IllegalStateException(
                    "Không tìm thấy đơn hàng cho phiên thanh toán: "
                            + command.checkoutId()
            );
        }

        Instant now = clock.instant();

        // 4. Xử lý theo eventType
        switch (command.eventType()) {

            case "InventoryReserved" -> handleReserved(checkout, orders, now);

            case "InventoryFailed" -> handleFailed(checkout, orders, now);

            default -> throw new IllegalArgumentException(
                    "Loại sự kiện không được hỗ trợ: " + command.eventType()
            );
        }


    }

    private void handleReserved(
            Checkout checkout,
            List<Order> orders,
            Instant now
    ) {
        if (checkout.getStatus() != CheckoutStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Phiên thanh toán không ở trạng thái PROCESSING: " + checkout.getStatus()
            );
        }

        if (checkout.getPaymentMethod() == PaymentMethod.COD) {

            // COD: xác nhận đơn nhưng chưa thanh toán
            for (Order order : orders) {
                order.confirmCod(now);
            }

            orderRepository.saveAll(orders);

            // Checkout hoàn tất bước đặt hàng COD
            checkout.complete(now);
            checkoutRepository.save(checkout);

        }
        //TODO chưa thực hiện checkout = ATM
        else if (checkout.getPaymentMethod() == PaymentMethod.ATM) {

            // ATM: chưa xác nhận thanh toán.
            // Giữ nguyên Checkout PROCESSING.
            //
            // Bước tiếp theo sẽ:
            // 1. Ghi Saga Step INVENTORY_RESERVED
            // 2. Tạo PaymentRequested trong Order Outbox
            //
            // Không complete Checkout tại đây.

        }
        else {
            throw new IllegalStateException(
                    "Phương thức thanh toán không được hỗ trợ: "
                            + checkout.getPaymentMethod()
            );
        }
    }

    private void handleFailed(
            Checkout checkout,
            List<Order> orders,
            Instant now
    ) {
        if (checkout.getStatus() != CheckoutStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Phiên thanh toán không ở trạng thái PROCESSING: " + checkout.getStatus()
            );
        }

        // Hủy tất cả Order thuộc Checkout
        for (Order order : orders) {
            order.cancel(now);
        }

        orderRepository.saveAll(orders);

        // Đánh dấu Checkout thất bại
        checkout.fail(now);
        checkoutRepository.save(checkout);
    }
}