package com.multivendor.shipping.service;

import com.multivendor.shipping.model.OutboxStatus;
import com.multivendor.shipping.model.ShippingOutboxEvent;
import com.multivendor.shipping.repository.ShippingOutboxRepository;

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
import java.util.concurrent.TimeUnit;

@Component
public class ShippingOutboxPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(ShippingOutboxPublisher.class);

    private final ShippingOutboxRepository repository;
    private final ShippingOutboxStateService stateService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Clock clock;
    private final String topic;

    public ShippingOutboxPublisher(
            ShippingOutboxRepository repository,
            ShippingOutboxStateService stateService,
            KafkaTemplate<String, String> kafkaTemplate,
            Clock clock,
            @Value("${shipping.kafka.status-topic:shipping.status.v1}")
            String topic
    ) {
        this.repository = repository;
        this.stateService = stateService;
        this.kafkaTemplate = kafkaTemplate;
        this.clock = clock;
        this.topic = topic;
    }

    @Scheduled(fixedDelayString = "${shipping.outbox.poll-ms:1000}")
    public void publishPending() {

        var events = repository.findReadyEvents(
                OutboxStatus.PENDING,
                clock.instant(),
                PageRequest.of(0, 20)
        );
        log.info("publish ds sau: "+events.size());

        for (ShippingOutboxEvent event : events) {

            try {
                kafkaTemplate.send(
                        topic,
                        event.getAggregateId().toString(),
                        event.getPayload()
                ).get(15, TimeUnit.SECONDS);

                stateService.markPublished(event.getId());

                log.info(
                        "Published Shipping eventId={}",
                        event.getId()
                );

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;

            } catch (Exception ex) {
                log.error(
                        "Failed to publish Shipping eventId={}",
                        event.getId(),
                        ex
                );

                try {
                    int attempt = event.getAttemptCount() + 1;
                    long delay = Math.min(
                            5L * (1L << Math.min(attempt - 1, 6)),
                            Duration.ofMinutes(5).toSeconds()
                    );

                    stateService.recordFailure(
                            event.getId(),
                            ex.toString(),
                            clock.instant().plusSeconds(delay)
                    );
                } catch (Exception dbError) {
                    log.error(
                            "Cannot record Outbox failure. eventId={}",
                            event.getId(),
                            dbError
                    );
                }
            }
        }
    }
}