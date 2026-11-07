package com.multivendor.shipping.consumer;

import com.multivendor.shipping.dto.OrderShippedEvent;
import com.multivendor.shipping.service.ShipmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Objects;

@Component
public class OrderShippedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderShippedConsumer.class);

    private final ShipmentService shipmentService;
    private final ObjectMapper objectMapper;

    public OrderShippedConsumer(ShipmentService shipmentService, ObjectMapper objectMapper) {
        this.shipmentService = shipmentService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${shipping.kafka.order-shipped-topic}",
            groupId = "shipping-order-shipped-consumer"
    )
    public void consume(String payload) throws Exception {

        OrderShippedEvent event = objectMapper.readValue(payload, OrderShippedEvent.class);

        Objects.requireNonNull(event.eventId(), "Không được để trống eventId");
        Objects.requireNonNull(event.orderId(), "Không được để trống orderId");

        if (!"OrderShipped".equals(event.eventType())) {
            throw new IllegalArgumentException("Loại sự kiện không hợp lệ: " + event.eventType());
        }

        log.info("Đã nhận sự kiện OrderShipped eventId={}, orderId={}", event.eventId(), event.orderId());

        shipmentService.createFromOrderShipped(event);


        log.info("Đã tạo vận đơn orderId={}", event.orderId());
    }
}