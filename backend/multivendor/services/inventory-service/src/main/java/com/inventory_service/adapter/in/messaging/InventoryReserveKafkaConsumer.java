
package com.inventory_service.adapter.in.messaging;

import com.inventory_service.adapter.in.messaging.event.InventoryReserveRequestedEvent;
import com.inventory_service.application.stockreservation.command.ReserveCheckoutStockCommand;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class InventoryReserveKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final ProcessInventoryReservationUseCase processUseCase;

    @KafkaListener(
            topics = "${inventory.kafka.reserve-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(ConsumerRecord<String, String> record) {

        // Deserialize event
        InventoryReserveRequestedEvent event =
                objectMapper.readValue(
                        record.value(),
                        InventoryReserveRequestedEvent.class
                );

        // Validate Kafka key.
        if (!event.checkoutId().toString().equals(record.key())) {
            throw new IllegalArgumentException(
                    "Kafka key must match checkoutId"
            );
        }
        //map list
        var items = event.items().stream()
                .map(item -> new ReserveCheckoutStockCommand.Item(
                        item.orderId(),
                        item.variantId(),
                        item.quantity()
                ))
                .toList();

        ReserveCheckoutStockCommand command =
                new ReserveCheckoutStockCommand(
                        event.checkoutId(),
                        items
                );

        // STEP 4: Inbox + Reserve trong cùng transaction.
        processUseCase.execute(event.eventId(), command);
    }
}
