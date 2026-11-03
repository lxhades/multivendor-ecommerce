
package com.inventory_service.adapter.in.messaging;

import com.inventory_service.adapter.in.messaging.event.InventoryReserveRequestedEvent;
import com.inventory_service.application.stockreservation.command.ReserveOrderItem;
import com.inventory_service.application.stockreservation.command.ReserveOrderStockCommand;
import com.inventory_service.application.stockreservation.usecase.ReserveOrderStockUseCase;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import java.util.Objects;

@Component
public class InventoryReserveKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final ReserveOrderStockUseCase reserveOrderStockUseCase;

    public InventoryReserveKafkaConsumer(
            ObjectMapper objectMapper,
            ReserveOrderStockUseCase reserveOrderStockUseCase
    ) {
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.reserveOrderStockUseCase =
                Objects.requireNonNull(reserveOrderStockUseCase);
    }

    @KafkaListener(
            topics = "${inventory.kafka.reserve-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(ConsumerRecord<String, String> record) {

        InventoryReserveRequestedEvent event =
                objectMapper.readValue(
                        record.value(),
                        InventoryReserveRequestedEvent.class
                );

        Objects.requireNonNull(event.eventId());
        Objects.requireNonNull(event.orderId());

        if (!InventoryReserveRequestedEvent.EVENT_TYPE
                .equals(event.eventType())) {
            throw new IllegalArgumentException(
                    "Unsupported eventType: " + event.eventType()
            );
        }

        Objects.requireNonNull(event.items());

        var items = event.items().stream()
                .map(item -> new ReserveOrderItem(
                        item.variantId(),
                        item.quantity()
                ))
                .toList();

        reserveOrderStockUseCase.execute(
                new ReserveOrderStockCommand(
                        event.orderId(),
                        items
                )
        );
    }
}
