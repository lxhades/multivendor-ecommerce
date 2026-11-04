package com.order_service.application.checkout.usecase;

import com.order_service.application.checkout.command.CreateCheckoutCommand;
import com.order_service.application.checkout.command.ShopOrderCommand;
import com.order_service.application.checkout.dto.CheckoutResult;
import com.order_service.application.checkout.dto.CreateCheckoutResult;
import com.order_service.application.order.dto.OrderResult;
import com.order_service.domain.model.aggregate.Checkout;
import com.order_service.domain.model.aggregate.Order;
import com.order_service.domain.model.entity.OrderItem;
import com.order_service.domain.repository.CheckoutRepository;
import com.order_service.domain.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class CreateCheckoutUseCase {

    private final CheckoutRepository checkouts;
    private final OrderRepository orders;
    private final Clock clock;

    public CreateCheckoutUseCase(
            CheckoutRepository checkouts,
            OrderRepository orders,
            Clock clock
    ) {
        this.checkouts = checkouts;
        this.orders = orders;
        this.clock = clock;
    }

    @Transactional
    public CreateCheckoutResult execute(CreateCheckoutCommand command) {



        // Bước 1: Idempotency check
        var previous = checkouts.findByBuyerIdAndIdempotencyKey(
                command.buyerId(),
                command.idempotencyKey()
        );

        // Bước 1a: Nếu đã có checkout trước đó, load lại và trả về toàn bộ order con
        if (previous.isPresent()) {
            Checkout existing = previous.get();
            return new CreateCheckoutResult(
                    CheckoutResult.from(existing),
                    orders.findByCheckoutId(existing.getId())
                            .stream()
                            .map(OrderResult::from)
                            .toList()
            );
        }


        var now = clock.instant();

        // Bước 2: Tạo aggregate Checkout
        Checkout checkout = Checkout.create(
                UUID.randomUUID(),
                command.buyerId(),
                command.idempotencyKey(),
                command.paymentMethod(),
                now
        );

        // Bước 3: Duyệt từng shop trong command, mỗi shop sinh ra 1 Order riêng
        List<Order> newOrders = new ArrayList<>();

        for (ShopOrderCommand shop : command.shops()) {

            // Bước 4: Map từng ItemCommand sang OrderItem domain
            List<OrderItem> items = shop.items()
                    .stream()
                    .map(i -> OrderItem.create(
                            UUID.randomUUID(),
                            i.productId(),
                            i.variantId(),
                            i.productName(),
                            i.unitPrice(),
                            i.quantity()
                    ))
                    .toList();

            // Bước 4b: Tạo Order cho shop này — gắn với checkoutId vừa tạo,
            newOrders.add(Order.create(
                    UUID.randomUUID(),
                    checkout.getId(),
                    command.buyerId(),
                    shop.shopId(),
                    items,
                    command.shippingAddress(),
                    shop.shippingFee(),
                    command.paymentMethod(),
                    now
            ));
        }

        // Bước 5: Persist checkout trước — cần id của checkout để Order tham chiếu
        Checkout saved = checkouts.save(checkout);

        // Bước 6: Persist toàn bộ order con trong 1 lần gọi saveAll —
        List<Order> persisted = orders.saveAll(newOrders);

        // Bước 7: Map domain -> DTO và trả về cho controller.
        return new CreateCheckoutResult(
                CheckoutResult.from(saved),
                persisted.stream()
                        .map(OrderResult::from)
                        .toList()
        );
    }
}