package com.order_service.adapter.out.messaging;

import com.order_service.adapter.out.outbox.SpringDataOrderOutboxRepository;
import com.order_service.application.checkout.event.InventoryReservationRequested;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@ConditionalOnProperty(name = "order.outbox.enabled", havingValue = "true", matchIfMissing = true)
public class OrderOutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderOutboxPublisher.class);

    private final SpringDataOrderOutboxRepository outbox;
    private final KafkaTemplate<String, String> kafka;
    private final TransactionTemplate transactions;
    private final Clock clock;
    private final String topic;
    private final int batchSize;
    private final long sendTimeoutMs;

    public OrderOutboxPublisher(
            SpringDataOrderOutboxRepository outbox,
            KafkaTemplate<String, String> kafka,
            PlatformTransactionManager transactionManager,
            Clock clock,
            @Value("${order.kafka.reserve-topic:inventory.reserve.requested.v1}") String topic,
            @Value("${order.outbox.batch-size:50}") int batchSize,
            @Value("${order.outbox.send-timeout-ms:15000}") long sendTimeoutMs
    ) {
        if (batchSize <= 0 || sendTimeoutMs <= 0) {
            throw new IllegalArgumentException("Outbox batch size and send timeout must be positive");
        }
        this.outbox = outbox;
        this.kafka = kafka;
        this.transactions = new TransactionTemplate(transactionManager);
        this.clock = clock;
        this.topic = topic;
        this.batchSize = batchSize;
        this.sendTimeoutMs = sendTimeoutMs;
    }

    @Scheduled(fixedDelayString = "${order.outbox.poll-delay-ms:3000}")
    public void publishPending() {
        for (int i = 0; i < batchSize; i++) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            try {
                UUID publishedId = transactions.execute(status -> publishNext());
                if (publishedId == null) {
                    return;
                }
                log.info("Published order outbox event {} to {}", publishedId, topic);
            } catch (RuntimeException ex) {
                // Rollback leaves the event PENDING; retry on the next scheduled poll.
                log.warn("Order outbox publish failed; pending event will be retried", ex);
                return;
            }
        }
    }

    private UUID publishNext() {
        var pending = outbox.findNextPendingForUpdate(InventoryReservationRequested.EVENT_TYPE);
        if (pending.isEmpty()) {
            return null;
        }

        var event = pending.get();
        try {
            kafka.send(topic, event.getAggregateId().toString(), event.getPayload())
                    .get(sendTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while publishing outbox event " + event.getId(), ex);
        } catch (ExecutionException | TimeoutException ex) {
            throw new IllegalStateException("Kafka did not acknowledge outbox event " + event.getId(), ex);
        }

        // The row remains locked until commit. A crash after ACK can cause redelivery;
        // Inventory's inbox deduplicates using the original eventId in the payload.
        event.markPublished(clock.instant());
        return event.getId();
    }
}
