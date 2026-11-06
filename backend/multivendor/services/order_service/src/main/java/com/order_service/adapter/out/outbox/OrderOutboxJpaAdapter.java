
package com.order_service.adapter.out.outbox;

import com.order_service.application.checkout.event.InventoryReservationRequested;
import com.order_service.application.checkout.port.OutboxPort;

import org.springframework.stereotype.Repository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.util.Objects;

@Repository
public class OrderOutboxJpaAdapter implements OutboxPort {

    private final SpringDataOrderOutboxRepository repository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public OrderOutboxJpaAdapter(
            SpringDataOrderOutboxRepository repository,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.repository = Objects.requireNonNull(repository);
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public void saveInventoryReservationRequested(
            InventoryReservationRequested event
    ) {

        Objects.requireNonNull(event, "Event is required");
        Objects.requireNonNull(event.eventId(), "eventId is required");
        Objects.requireNonNull(event.checkoutId(), "checkoutId is required");
        Objects.requireNonNull(event.buyerId(), "buyerId is required");

        // Kiểm tra danh sách Item.
        if (event.items() == null || event.items().isEmpty()) {
            throw new IllegalArgumentException(
                    "Inventory reservation items cannot be empty"
            );
        }

        // Serialize Event thành JSON String.
        String payload = objectMapper.writeValueAsString(event);


        // Outbox Entity.
        // eventId cũng là ID của Outbox.
        OrderOutboxEntity entity = new OrderOutboxEntity(
                event.eventId(),
                event.checkoutId(),
                InventoryReservationRequested.EVENT_TYPE,
                payload,
                clock.instant()
        );

        repository.save(entity);
    }
}
