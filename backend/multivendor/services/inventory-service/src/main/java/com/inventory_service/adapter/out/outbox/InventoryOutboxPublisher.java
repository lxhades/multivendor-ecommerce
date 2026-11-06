package com.inventory_service.adapter.out.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class InventoryOutboxPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(InventoryOutboxPublisher.class);

    private final SpringDataInventoryOutboxRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Clock clock;
    private final String topic;

    public InventoryOutboxPublisher(
            SpringDataInventoryOutboxRepository repository,
            KafkaTemplate<String, String> kafkaTemplate,
            Clock clock,
            @Value("${inventory.kafka.result-topic}")
            String topic
    ) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.clock = clock;
        this.topic = topic;
    }

    @Scheduled(fixedDelayString = "${inventory.outbox.poll-delay-ms:3000}")
    public void publishPending() {

        List<InventoryOutboxEntity> events =
                repository.findReadyEvents(
                        InventoryOutboxStatus.PENDING,
                        clock.instant(),
                        PageRequest.of(0, 5)
                );

        if (events.isEmpty()) {
            log.debug("Không có event nào trong Outbox cần gửi.");
            return;
        }

        log.info("Tìm thấy {} event trong Outbox cần xử lý.", events.size());

        for (InventoryOutboxEntity event : events) {

            log.info("Đang gửi event lên Kafka: outboxId={}, aggregateId={}, type={}",
                    event.getId(), event.getAggregateId(), event.getEventType());

            try {
                // Gửi Kafka và chờ ACK
                kafkaTemplate.send(
                        topic,
                        event.getAggregateId().toString(),
                        event.getPayload()
                ).get(15, TimeUnit.SECONDS);

                log.info("Đã gửi event lên Kafka thành công: outboxId={}, aggregateId={}, type={}",
                        event.getId(), event.getAggregateId(), event.getEventType());

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                log.error("Luồng bị ngắt khi đang gửi Kafka. outboxId={}", event.getId(), ex);
                recordFailure(event, ex);
                return;

            } catch (Exception ex) {
                log.error("Gửi Kafka thất bại. outboxId={}", event.getId(), ex);
                recordFailure(event, ex);
                continue;
            }

            // Cập nhật DB sau khi Kafka ACK thành công
            try {
                markPublished(event.getId());
                log.info("Đã cập nhật Outbox thành PUBLISHED thành công. outboxId={}, aggregateId={}",
                        event.getId(), event.getAggregateId());
            } catch (Exception ex) {
                log.error("LỖI NGHIÊM TRỌNG: Kafka đã nhận event nhưng cập nhật Outbox DB thất bại! outboxId={}",
                        event.getId(), ex);
            }
        }
    }

    @Transactional
    public void markPublished(UUID eventId) {
        repository.markPublished(
                eventId,
                clock.instant(),
                InventoryOutboxStatus.PENDING,
                InventoryOutboxStatus.PUBLISHED
        );
    }

    private void recordFailure(
            InventoryOutboxEntity event,
            Exception ex
    ) {
        int attempt = event.getAttemptCount() + 1;

        long delay = calculateBackoff(attempt);
        Instant nextRetryAt = clock.instant().plusSeconds(delay);

        String error = ex.toString();
        if (error.length() > 2000) {
            error = error.substring(0, 2000);
        }

        try {
            saveFailure(event.getId(), nextRetryAt, error);
            log.warn("Đã ghi nhận retry vào DB. outboxId={}, attempt={}, nextRetryAt={}",
                    event.getId(), attempt, nextRetryAt);
        } catch (Exception dbEx) {
            log.error("Không thể ghi nhận retry vào DB. outboxId={}", event.getId(), dbEx);
        }

        log.warn("Gửi event thất bại, sẽ thử lại sau {}s. outboxId={}, attempt={}, error={}",
                delay, event.getId(), attempt, ex.getMessage());
    }

    @Transactional
    public void saveFailure(
            UUID eventId,
            Instant nextRetryAt,
            String error
    ) {
        repository.recordFailure(
                eventId,
                nextRetryAt,
                error,
                InventoryOutboxStatus.PENDING
        );
    }

    private long calculateBackoff(int attempt) {
        int exponent = Math.min(Math.max(attempt - 1, 0), 6);
        return Math.min(5L * (1L << exponent), 300L);
    }
}