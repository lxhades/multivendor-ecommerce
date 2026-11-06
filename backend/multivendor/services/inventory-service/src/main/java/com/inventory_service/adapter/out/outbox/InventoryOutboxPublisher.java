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

    @Scheduled(
            fixedDelayString = "${inventory.outbox.poll-delay-ms:3000}"
    )
    public void publishPending() {

        List<InventoryOutboxEntity> events =
                repository.findReadyEvents(
                        InventoryOutboxStatus.PENDING,
                        clock.instant(),
                        PageRequest.of(0, 5)
                );

        for (InventoryOutboxEntity event : events) {

            try {
                kafkaTemplate.send(
                        topic,
                        event.getAggregateId().toString(),
                        event.getPayload()
                ).get(15, TimeUnit.SECONDS);

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                recordFailure(event, ex);
                return;

            } catch (Exception ex) {
                recordFailure(event, ex);
                continue;
            }


            try {
                markPublished(event.getId());
            } catch (Exception ex) {
                log.error(
                        "Đã nhận Kafka ACK nhưng cập nhật outbox thất bại. eventId={}",
                        event.getId(),
                        ex
                );
            }
        }
    }

    @Transactional
    public void markPublished(java.util.UUID eventId) {
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
        } catch (Exception dbEx) {
            log.error(
                    "Ghi nhận retry cho outbox thất bại. eventId={}",
                    event.getId(),
                    dbEx
            );
        }

        log.warn(
                "Gửi event từ Inventory outbox thất bại. eventId={}, attempt={}, retryIn={}s",
                event.getId(),
                attempt,
                delay,
                ex
        );
    }

    @Transactional
    public void saveFailure(
            java.util.UUID eventId,
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
