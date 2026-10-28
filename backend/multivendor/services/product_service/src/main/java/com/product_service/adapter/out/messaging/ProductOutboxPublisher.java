package com.product_service.adapter.out.messaging;

import com.product_service.adapter.out.persistence.product.entity.ProductOutboxEventEntity;
import com.product_service.adapter.out.persistence.product.repository.ProductOutboxJpaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.concurrent.TimeUnit;

@Component
public class ProductOutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(ProductOutboxPublisher.class);

    private final ProductOutboxJpaRepository outbox;
    private final KafkaTemplate<String, String> kafka;
    private final Clock clock;
    private final String topic;

    public ProductOutboxPublisher(
            ProductOutboxJpaRepository outbox,
            KafkaTemplate<String, String> kafka,
            Clock clock,
            @Value("${product.kafka.inventory-topic:product.inventory-init.v1}")
            String topic
    ) {
        this.outbox = outbox;
        this.kafka = kafka;
        this.clock = clock;
        this.topic = topic;
    }

    @Scheduled(
            fixedDelayString = "${product.outbox.poll-delay-ms:3000}"
    )
    public void publishPending() {

        for (ProductOutboxEventEntity event :
                outbox.findTop50ByStatusOrderByCreatedAtAsc("PENDING")) {

            try {
                kafka.send(
                        topic,
                        event.getAggregateId().toString(),
                        event.getPayload()
                ).get(15, TimeUnit.SECONDS);

                event.markPublished(clock.instant());
                outbox.save(event);

                log.info(
                        "Published inventory event {}",
                        event.getId()
                );

            } catch (Exception e) {
                event.recordFailure();

                try {
                    outbox.save(event);
                } catch (Exception dbError) {
                    log.error(
                            "Failed to update outbox {}",
                            event.getId(),
                            dbError
                    );
                }

                log.warn(
                        "Outbox publish failed for event {}",
                        event.getId(),
                        e
                );

                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}