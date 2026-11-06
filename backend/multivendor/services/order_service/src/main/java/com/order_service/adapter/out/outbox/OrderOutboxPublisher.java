
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class OrderOutboxPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(OrderOutboxPublisher.class);

    private final SpringDataOrderOutboxRepository outbox;
    private final KafkaTemplate<String, String> kafka;
    private final Clock clock;
    private final String topic;

    public OrderOutboxPublisher(
            SpringDataOrderOutboxRepository outbox,
            KafkaTemplate<String, String> kafka,
            Clock clock,
            @Value("${order.kafka.inventory-topic:inventory.reserve.requested.v1}")
            String topic
    ) {
        this.outbox = outbox;
        this.kafka = kafka;
        this.clock = clock;
        this.topic = topic;
    }

    @Scheduled(
            fixedDelayString = "${order.outbox.poll-delay-ms:3000}"
    )
    public void publishPending() {

        // lấy 5 event đã đến hạn retry.
        var events = outbox.findReadyEvents(
                OutboxStatus.PENDING,
                clock.instant(),
                PageRequest.of(0, 5)
        );

        for (OrderOutboxEntity event : events) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            publishOne(event);
        }
    }

    private void publishOne(OrderOutboxEntity event) {

        // Gửi Kafka và chờ ACK.
        try {
            kafka.send(
                    topic,
                    event.getAggregateId().toString(),
                    event.getPayload()
            ).get(15, TimeUnit.SECONDS);

            log.info("Kafka ACK received for event: id={}", event.getId());

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Publisher interrupted: {}", event.getId());
            return;

        } catch (ExecutionException | TimeoutException | RuntimeException ex) {

            // Kafka lỗi -> lưu retry.
            recordFailure(event, ex);
            return;
        }

        // Kafka ACK thành công.
        // Chỉ cập nhật PUBLISHED sau ACK.
        // Tách khỏi catch Kafka để không coi lỗi DB là lỗi gửi.
        try {
            int updated = outbox.markPublished(
                    event.getId(),
                    clock.instant(),
                    OutboxStatus.PENDING,
                    OutboxStatus.PUBLISHED
            );

            if (updated == 1) {
                log.info(
                        "Outbox published: {}",
                        event.getId()
                );
            } else {
                log.warn(
                        "Outbox status not updated: {}",
                        event.getId()
                );
            }

        } catch (RuntimeException dbError) {

            //  Kafka đã ACK nhưng DB update lỗi.
            log.error(
                    "Kafka ACK succeeded but DB update failed: {}",
                    event.getId(),
                    dbError
            );
        }
    }

    private void recordFailure(
            OrderOutboxEntity event,
            Exception error
    ) {
        // Tính exponential backoff.
        int failureCount = event.getAttemptCount() + 1;

        long delaySeconds = calculateBackoff(failureCount);

        Instant nextRetryAt =
                clock.instant().plusSeconds(delaySeconds);

        String message = error.toString();

        // Giới hạn kích thước lỗi lưu DB.
        if (message.length() > 2000) {
            message = message.substring(0, 2000);
        }

        // Lưu retry
        try {
            int updated = outbox.recordFailure(
                    event.getId(),
                    nextRetryAt,
                    message,
                    OutboxStatus.PENDING
            );

            if (updated == 1) {
                log.warn(
                        "Outbox retry: eventId={}, failures={}, delay={}s",
                        event.getId(),
                        failureCount,
                        delaySeconds,
                        error
                );
            } else {
                log.warn(
                        "Could not record retry for event: {}",
                        event.getId()
                );
            }

        } catch (RuntimeException dbError) {
            log.error(
                    "Failed to persist retry metadata: {}",
                    event.getId(),
                    dbError
            );
        }
    }


    // 5s -> 10s -> 20s -> 40s -> ... -> 300s.
    private long calculateBackoff(int failureCount) {

        int exponent = Math.min(
                Math.max(failureCount - 1, 0),
                6
        );

        return Math.min(
                5L * (1L << exponent),
                Duration.ofMinutes(5).toSeconds()
        );
    }
}
