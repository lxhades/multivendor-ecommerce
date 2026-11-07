package com.product_service.application.product.usecase;

import com.product_service.application.product.port.SalesPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
public class RecordDeliveredOrderSalesUseCase {
    private final SalesPort sales;

    public RecordDeliveredOrderSalesUseCase(SalesPort sales) {
        this.sales = sales;
    }

    @Transactional
    public void execute(Event event) {
        if (event == null || !"OrderDelivered".equals(event.eventType())
                || event.eventId() == null || event.orderId() == null
                || event.shopId() == null || event.deliveredAt() == null
                || event.items() == null || event.items().isEmpty()) {
            throw new IllegalArgumentException("OrderDelivered event không hợp lệ");
        }

        var variants = new HashSet<UUID>();
        for (Item item : event.items()) {
            if (item == null || item.productId() == null || item.variantId() == null
                    || item.quantity() <= 0 || !variants.add(item.variantId())) {
                throw new IllegalArgumentException("varient trong OrderDelivered event không hợp lệ");
            }
        }

        if (!sales.claimDeliveredOrder(event.orderId())) {
            return;
        }

        var items = event.items().stream()
                .sorted(Comparator.comparing(Item::productId).thenComparing(Item::variantId))
                .toList();

        for (Item item : items) {
            sales.incrementSoldCount(event.shopId(), item.productId(), item.variantId(), item.quantity());
        }
    }

    public record Event(UUID eventId, String eventType, UUID orderId, UUID shopId,
                        Instant deliveredAt, List<Item> items) {}

    public record Item(UUID productId, UUID variantId, int quantity) {}
}
