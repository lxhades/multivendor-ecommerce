
package com.order_service.adapter.out.outbox;

import com.order_service.application.event.InventoryReleaseRequested;
import com.order_service.application.event.InventoryReservationRequested;
import com.order_service.application.checkout.port.OutboxPort;

import com.order_service.application.event.OrderShipped;
import org.springframework.stereotype.Repository;
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

        Objects.requireNonNull(event, "Sự kiện không được để trống");
        Objects.requireNonNull(event.eventId(), "Mã sự kiện không được để trống");
        Objects.requireNonNull(event.checkoutId(), "Mã phiên thanh toán không được để trống");
        Objects.requireNonNull(event.buyerId(), "Mã người mua không được để trống");

        if (event.items() == null || event.items().isEmpty()) {
            throw new IllegalArgumentException(
                    "Danh sách sản phẩm giữ chỗ tồn kho không được để trống"
            );
        }


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
    @Override
    public void saveInventoryReleaseRequested(InventoryReleaseRequested event)
    {
        String payload = objectMapper.writeValueAsString(event);

        repository.save(new OrderOutboxEntity(
                event.eventId(),
                event.orderId(),
                InventoryReleaseRequested.EVENT_TYPE,
                payload,
                clock.instant()
        ));
    }
    @Override
    public void saveOrderShipped(OrderShipped event) {

        Objects.requireNonNull(event, "Sự kiện không được để trống");

        String payload = objectMapper.writeValueAsString(event);

        OrderOutboxEntity entity = new OrderOutboxEntity(
                event.eventId(),
                event.orderId(),
                OrderShipped.EVENT_TYPE,
                payload,
                clock.instant()
        );

        repository.save(entity);
    }
}
