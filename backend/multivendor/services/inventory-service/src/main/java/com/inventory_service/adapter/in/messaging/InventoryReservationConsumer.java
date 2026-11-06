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

    private static final Logger log = LoggerFactory.getLogger(InventoryReservationConsumer.class);

    private final ReserveCheckoutUseCase reserveCheckoutUseCase;
    private final ObjectMapper objectMapper;

    public InventoryReservationConsumer(
            ReserveCheckoutUseCase reserveCheckoutUseCase,
            ObjectMapper objectMapper
    ) {
        this.reserveCheckoutUseCase = Objects.requireNonNull(reserveCheckoutUseCase);
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    @KafkaListener(
            topics = "${inventory.kafka.reserve-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String jsonPayload) {

        // 1. Parse JSON thành Object
        InventoryReservationRequestedEvent reservationEvent =
                objectMapper.readValue(jsonPayload, InventoryReservationRequestedEvent.class);

        // 2. Validate dữ liệu đầu vào
        if (!"InventoryReservationRequested".equals(reservationEvent.eventType())) {
            throw new IllegalArgumentException("Unexpected eventType: " + reservationEvent.eventType());
        }

        if (reservationEvent.items() == null) {
            throw new IllegalArgumentException("items cannot be null");
        }

        // 3. Map dữ liệu từ Event sang Command
        List<ReserveCheckoutCommand.Item> requestedItems =
                reservationEvent.items().stream()
                        .map(item -> new ReserveCheckoutCommand.Item(
                                item.orderId(),
                                item.variantId(),
                                item.quantity()
                        ))
                        .toList();

        ReserveCheckoutCommand reserveCommand =
                new ReserveCheckoutCommand(
                        reservationEvent.eventId(),
                        reservationEvent.checkoutId(),
                        reservationEvent.buyerId(),
                        requestedItems
                );

        // 4. Gọi UseCase xử lý (Trả về true nếu xử lý thành công, false nếu là event trùng)
        int isProcessed = reserveCheckoutUseCase.execute(reserveCommand);

        // 5. Chỉ in log khi thực sự xử lý thành công
        if (isProcessed==1) {
            log.info("Xử lý giữ kho thành công. eventId={}, checkoutId={}",
                    reservationEvent.eventId(),
                    reservationEvent.checkoutId());
        } else if (isProcessed==0) {
            log.info("Bỏ qua event trùng lặp (Idempotency). eventId={}",
                    reservationEvent.eventId());
        }else {
            log.info("Lôi inventory hoặc số lương",
                    reservationEvent.eventId());
        }
    }
}