package com.order_service.adapter.in.message;

import com.order_service.application.saga.HandleInventoryResultUseCase;
import com.order_service.application.saga.command.HandleInventoryResultCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import java.util.Objects;

@Component
public class InventoryResultKafkaConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(InventoryResultKafkaConsumer.class);

    private final HandleInventoryResultUseCase handleInventoryResultUseCase;
    private final ObjectMapper objectMapper;

    public InventoryResultKafkaConsumer(
            HandleInventoryResultUseCase handleInventoryResultUseCase,
            ObjectMapper objectMapper
    ) {
        this.handleInventoryResultUseCase = Objects.requireNonNull(handleInventoryResultUseCase);
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    @KafkaListener(
            topics = "${order.kafka.inventory-result-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String jsonPayload) {

        log.info("Đã nhận sự kiện kết quả từ Inventory Service: payload={}", jsonPayload);

        InventoryResultEvent inventoryResultEvent;
        try {
            inventoryResultEvent = objectMapper.readValue(jsonPayload, InventoryResultEvent.class);
        } catch (Exception ex) {
            log.error("Lỗi parse JSON từ Inventory Service. payload={}", jsonPayload, ex);
            throw new IllegalArgumentException("Dữ liệu JSON không hợp lệ", ex);
        }

        validateEvent(inventoryResultEvent);

        HandleInventoryResultCommand handleInventoryResultCommand =
                new HandleInventoryResultCommand(
                        inventoryResultEvent.eventId(),
                        inventoryResultEvent.eventType(),
                        inventoryResultEvent.checkoutId(),
                        inventoryResultEvent.causationEventId(),
                        inventoryResultEvent.reason()
                );



        handleInventoryResultUseCase.execute(handleInventoryResultCommand);

        log.info("Xử lý kết quả từ inventory: eventId={}, checkoutId={}, eventType={}",
                inventoryResultEvent.eventId(),
                inventoryResultEvent.checkoutId(),
                inventoryResultEvent.eventType());
    }


    private void validateEvent(InventoryResultEvent inventoryResultEvent) {
        if (inventoryResultEvent.eventId() == null
                || inventoryResultEvent.checkoutId() == null
                || inventoryResultEvent.causationEventId() == null) {

            throw new IllegalArgumentException(
                    "Thiếu thông tin bắt buộc trong event (eventId, checkoutId, causationEventId)"
            );
        }

        if (!"InventoryReserved".equals(inventoryResultEvent.eventType())
                && !"InventoryFailed".equals(inventoryResultEvent.eventType())) {

            throw new IllegalArgumentException(
                    "Loại sự kiện không được hỗ trợ: " + inventoryResultEvent.eventType()
            );
        }
    }
}