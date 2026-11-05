
package com.order_service.adapter.out.outbox;

import com.order_service.application.checkout.event.InventoryReservationRequested;
import com.order_service.application.checkout.port.OutboxPort;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;

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
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    public void saveInventoryReservationRequested(
            InventoryReservationRequested event
    ) {
        String payload = objectMapper.writeValueAsString(event);

        OrderOutboxEntity entity = new OrderOutboxEntity(
                event.eventId(),
                event.checkoutId(),
                "InventoryReservationRequested",
                payload,
                clock.instant()
        );

        repository.save(entity);
    }
}
