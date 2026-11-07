package com.order_service.adapter.in.message;


import com.order_service.application.shipping.HandleShippingStatusChangedUseCase;
import com.order_service.application.shipping.command.HandleShippingStatusChangedCommand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import java.util.Objects;

@Component
public class ShippingStatusChangedConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(ShippingStatusChangedConsumer.class);

    private final HandleShippingStatusChangedUseCase useCase;
    private final ObjectMapper objectMapper;

    public ShippingStatusChangedConsumer(
            HandleShippingStatusChangedUseCase useCase,
            ObjectMapper objectMapper
    ) {
        this.useCase = Objects.requireNonNull(useCase);
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    @KafkaListener(
            topics = "${order.kafka.shipping-status-topic}",
            groupId = "order-shipping-status-consumer"
    )
    public void consume(String payload) {

        ShippingStatusChangedEvent event;

        try {
            event = objectMapper.readValue(
                    payload,
                    ShippingStatusChangedEvent.class
            );
        } catch (Exception ex) {
            log.error("Dữ liệu JSON của sự kiện vận chuyển không hợp lệ", ex);
            throw new IllegalArgumentException(
                    "Dữ liệu JSON của sự kiện vận chuyển không hợp lệ", ex
            );
        }

        validateEvent(event);

        HandleShippingStatusChangedCommand command =
                new HandleShippingStatusChangedCommand(
                        event.eventId(),
                        event.orderId(),
                        event.status(),
                        event.occurredAt()
                );

        useCase.execute(command);

        log.info(
                "Shipping event processed: eventId={}, orderId={}, status={}",
                event.eventId(),
                event.orderId(),
                event.status()
        );
    }

    private void validateEvent(ShippingStatusChangedEvent event) {

        if (event == null
                || event.eventId() == null
                || event.orderId() == null
                || event.occurredAt() == null) {

            throw new IllegalArgumentException(
                    "Sự kiện vận chuyển thiếu trường bắt buộc"
            );
        }

        if (!"ShippingStatusChanged".equals(event.eventType())) {
            throw new IllegalArgumentException(
                    "Loại sự kiện không được hỗ trợ: " + event.eventType()
            );
        }

        if (!"DELIVERED".equals(event.status())) {
            throw new IllegalArgumentException(
                    "Trạng thái vận chuyển không được hỗ trợ: " + event.status()
            );
        }
    }
}