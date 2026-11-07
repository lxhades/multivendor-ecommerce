package com.order_service.adapter.out.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class OrderOutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderOutboxPublisher.class);

    private static final String EVENT_TYPE_RESERVATION = "InventoryReservationRequested";
    private static final String EVENT_TYPE_RELEASE = "InventoryReleaseRequested";
    private static final String EVENT_TYPE_ORDER_SHIPPED = "OrderShipped";


    private final SpringDataOrderOutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Clock clock;
    private final String reservationTopic;
    private final String releaseTopic;
    private final String orderShippedTopic;
    public OrderOutboxPublisher(
            SpringDataOrderOutboxRepository outboxRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            Clock clock,
            @Value("${order.kafka.inventory-topic:inventory.reserve.requested.v1}")
            String reservationTopic,
            @Value("${order.kafka.inventory-release-topic:inventory.release.requested.v1}")
            String releaseTopic,
            @Value("${order.kafka.shipped-topic:order.shipped.v1}")
                    String orderShippedTopic
    ) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.clock = clock;
        this.reservationTopic = reservationTopic;
        this.releaseTopic = releaseTopic;
        this.orderShippedTopic=orderShippedTopic;
    }

    @Scheduled(fixedDelayString = "${order.outbox.poll-delay-ms:3000}")
    public void publishPending() {
        List<OrderOutboxEntity> readyEvents = outboxRepository.findReadyEvents(
                OutboxStatus.PENDING,
                clock.instant(),
                PageRequest.of(0, 5)
        );

        if (readyEvents.isEmpty()) {
            log.debug("Không có event nào trong Outbox cần gửi.");
            return;
        }

        log.info("Tìm thấy {} event(s) trong Outbox cần xử lý.", readyEvents.size());

        for (OrderOutboxEntity event : readyEvents) {
            if (Thread.currentThread().isInterrupted()) {
                log.warn("Luồng Publisher bị ngắt. Dừng xử lý các event còn lại.");
                return;
            }
            publishOne(event);
        }
    }

    private void publishOne(OrderOutboxEntity event) {

        String targetTopic;
        try {
            targetTopic = resolveTopic(event.getEventType());
        } catch (IllegalArgumentException ex) {
            log.error("Lỗi cấu hình: Không tìm thấy topic cho loại sự kiện '{}'. eventId={}",
                    event.getEventType(), event.getId(), ex);
            return;
        }

        log.info("Đang gửi event lên Kafka: type={}, topic={}, eventId={}",
                event.getEventType(), targetTopic, event.getId());

        // 1. Gửi Kafka và chờ ACK
        try {
            kafkaTemplate.send(
                    targetTopic,
                    event.getAggregateId().toString(),
                    event.getPayload()
            ).get(15, TimeUnit.SECONDS);

            log.debug("Nhận Kafka ACK thành công. eventId={}", event.getId());

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Luồng bị gián đoạn khi đang chờ Kafka ACK. eventId={}", event.getId());
            return;

        } catch (ExecutionException | TimeoutException | RuntimeException ex) {
            log.error("Gửi Kafka thất bại. eventId={}", event.getId(), ex);
            recordFailure(event, ex);
            return;
        }

        try {
            int updatedRows = outboxRepository.markPublished(
                    event.getId(),
                    clock.instant(),
                    OutboxStatus.PENDING,
                    OutboxStatus.PUBLISHED
            );

            if (updatedRows == 1) {
                log.info("Đã cập nhật Outbox thành PUBLISHED thành công. eventId={}", event.getId());
            } else {
                log.warn("Không thể cập nhật trạng thái Outbox eventId={}", event.getId());
            }

        } catch (RuntimeException dbError) {
            log.error("LỖI NGHIÊM TRỌNG: Kafka đã nhận event nhưng cập nhật DB thất bại! eventId={}",
                    event.getId(), dbError);
        }
    }

    private String resolveTopic(String eventType) {

        return switch (eventType) {

            case EVENT_TYPE_RESERVATION -> reservationTopic;

            case EVENT_TYPE_RELEASE -> releaseTopic;

            case EVENT_TYPE_ORDER_SHIPPED -> orderShippedTopic;

            default -> throw new IllegalArgumentException(
                    "Unsupported event type: " + eventType
            );
        };
    }

    private void recordFailure(OrderOutboxEntity event, Exception error) {
        int failureCount = event.getAttemptCount() + 1;
        long delaySeconds = calculateBackoff(failureCount);
        Instant nextRetryAt = clock.instant().plusSeconds(delaySeconds);

        // Giới hạn độ dài chuỗi lỗi để tránh tràn cột trong DB
        String errorMessage = error.toString();
        if (errorMessage.length() > 2000) {
            errorMessage = errorMessage.substring(0, 2000);
        }

        try {
            int updatedRows = outboxRepository.recordFailure(
                    event.getId(),
                    nextRetryAt,
                    errorMessage,
                    OutboxStatus.PENDING
            );

            if (updatedRows == 1) {
                log.warn("Đã ghi nhận thử lại. eventId={}, số lần lỗi={}, thời gian chờ={}s",
                        event.getId(), failureCount, delaySeconds);
            } else {
                log.warn("Không thể lưu thông tin thử lại vào DB. eventId={}", event.getId());
            }

        } catch (RuntimeException dbError) {
            log.error("Lỗi khi lưu thông tin retry vào DB. eventId={}", event.getId(), dbError);
        }
    }


    private long calculateBackoff(int failureCount) {
        int exponent = Math.min(Math.max(failureCount - 1, 0), 6);
        return Math.min(5L * (1L << exponent), Duration.ofMinutes(5).toSeconds());
    }
}