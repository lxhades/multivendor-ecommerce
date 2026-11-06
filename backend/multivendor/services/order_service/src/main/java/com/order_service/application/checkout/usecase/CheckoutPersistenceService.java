package com.order_service.application.checkout.usecase;

import com.order_service.application.checkout.dto.CheckoutResult;
import com.order_service.application.checkout.dto.CreateCheckoutResult;
import com.order_service.application.checkout.event.InventoryReservationRequested;

import com.order_service.application.checkout.port.OutboxPort;
import com.order_service.application.order.dto.OrderResult;

import com.order_service.domain.model.aggregate.Checkout;
import com.order_service.domain.model.aggregate.Order;
import com.order_service.domain.model.entity.OrderItem;
import com.order_service.domain.repository.CheckoutRepository;
import com.order_service.domain.repository.OrderRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class CheckoutPersistenceService {

    private final CheckoutRepository checkouts;
    private final OrderRepository orders;
    private final OutboxPort outbox;

    public CheckoutPersistenceService(
            CheckoutRepository checkouts,
            OrderRepository orders,
            OutboxPort outbox
    ) {
        this.checkouts = checkouts;
        this.orders = orders;
        this.outbox = outbox;
    }


    @Transactional
    public CreateCheckoutResult persist(
            Checkout checkout,
            List<Order> newOrders
    ) {
        // SAVE CHECKOUT
        Checkout savedCheckout = checkouts.save(checkout);

        // SAVE ORDERS
        List<Order> savedOrders = orders.saveAll(newOrders);

        // BUILD INVENTORY EVENT
        List<InventoryReservationRequested.Item> items =
                savedOrders.stream()
                        .flatMap(this::toReservationItems)
                        .toList();
        // tạo event
        InventoryReservationRequested event =
                new InventoryReservationRequested(
                        UUID.randomUUID(),
                        InventoryReservationRequested.EVENT_TYPE,
                        savedCheckout.getId(),
                        savedCheckout.getBuyerId(),
                        items
                );

        // SAVE OUTBOX EVENT
        outbox.saveInventoryReservationRequested(event);


        return new CreateCheckoutResult(
                CheckoutResult.from(savedCheckout),
                savedOrders.stream()
                        .map(OrderResult::from)
                        .toList()
        );
    }


    private InventoryReservationRequested.Item toReservationItem(
            Order order,
            OrderItem item
    ) {
        return new InventoryReservationRequested.Item(
                order.getId(),
                item.getVariantId(),
                item.getQuantity()
        );
    }


     //List<Order> thành List<Item>.
    private Stream<InventoryReservationRequested.Item> toReservationItems(Order order) {
        return order.getItems()
                .stream()
                .map(item -> toReservationItem(order, item));
    }
}