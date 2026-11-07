package com.inventory_service.adapter.in.messaging;

import com.inventory_service.application.stockreservation.usecase.ConfirmOrderReservationsUseCase;
import com.inventory_service.application.stockreservation.usecase.ConfirmOrderReservationsUseCase.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class OrderDeliveredConsumer {

    private final ObjectMapper mapper;
    private final ConfirmOrderReservationsUseCase confirmOrderReservations;

    @KafkaListener(
            topics = "${inventory.kafka.delivered-topic}",
            groupId = ConfirmOrderReservationsUseCase.CONSUMER_GROUP
    )
    public void consume(String payload) {
        confirmOrderReservations.execute(mapper.readValue(payload, Event.class));
    }
}
