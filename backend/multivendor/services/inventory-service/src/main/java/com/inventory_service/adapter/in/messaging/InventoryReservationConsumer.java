package com.inventory_service.adapter.in.messaging;


import com.inventory_service.adapter.in.messaging.event.InventoryReservationRequestedEvent;
import com.inventory_service.application.stockreservation.command.ReserveCheckoutCommand;
import com.inventory_service.application.stockreservation.usecase.ReserveCheckoutUseCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Objects;

@Component
public class InventoryReservationConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(InventoryReservationConsumer.class);

    private final ReserveCheckoutUseCase useCase;
    private final ObjectMapper objectMapper;

    public InventoryReservationConsumer(
            ReserveCheckoutUseCase useCase,
            ObjectMapper objectMapper
    ) {
        this.useCase = Objects.requireNonNull(useCase);
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    @KafkaListener(
            topics = "${inventory.kafka.reserve-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String payload) {

        InventoryReservationRequestedEvent event =
                objectMapper.readValue(
                        payload,
                        InventoryReservationRequestedEvent.class
                );

        if (!"InventoryReservationRequested".equals(event.eventType())) {
            throw new IllegalArgumentException(
                    "Unexpected eventType: " + event.eventType()
            );
        }

        if (event.items() == null) {
            throw new IllegalArgumentException("items cannot be null");
        }

        List<ReserveCheckoutCommand.Item> items =
                event.items().stream()
                        .map(item -> new ReserveCheckoutCommand.Item(
                                item.orderId(),
                                item.variantId(),
                                item.quantity()
                        ))
                        .toList();

        ReserveCheckoutCommand command =
                new ReserveCheckoutCommand(
                        event.eventId(),
                        event.checkoutId(),
                        event.buyerId(),
                        items
                );

        useCase.execute(command);

        log.info(
                "Inventory reservation event processed. eventId={}, checkoutId={}",
                event.eventId(),
                event.checkoutId()
        );
    }
}