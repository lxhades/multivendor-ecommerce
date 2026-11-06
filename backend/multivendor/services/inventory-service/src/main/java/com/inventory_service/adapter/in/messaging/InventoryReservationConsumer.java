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


        log.info("Đã nhận sự kiện yêu cầu giữ kho: eventId={}, checkoutId={}",
                reservationEvent.eventId(), reservationEvent.checkoutId());

        // 2. Validate dữ liệu đầu vào
        if (!"InventoryReservationRequested".equals(reservationEvent.eventType())) {
            throw new IllegalArgumentException("Loại sự kiện không hợp lệ: " + reservationEvent.eventType());
        }

        if (reservationEvent.items() == null) {
            throw new IllegalArgumentException("Danh sách items không được để trống");
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

        // 4. Gọi UseCase xử lý
        int processStatus = reserveCheckoutUseCase.execute(reserveCommand);

        // 5. Phân nhánh in log
        if (processStatus == 1) {
            log.info("Xử lý giữ kho thành công. eventId={}, checkoutId={}",
                    reservationEvent.eventId(),
                    reservationEvent.checkoutId());

        } else if (processStatus == -1) {
            log.info("Bỏ qua sự kiện trùng lặp (Idempotency). eventId={}",
                    reservationEvent.eventId());

        } else {
            // Sửa lại lỗi thiếu {} và sửa lỗi chính tả
            log.warn("Không thể giữ kho (Lỗi không tìm thấy tồn kho hoặc số lượng không đủ). eventId={}",
                    reservationEvent.eventId());
        }
    }
}