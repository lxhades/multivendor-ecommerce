
package com.inventory_service.adapter.in.messaging;

import com.inventory_service.adapter.in.messaging.event.ProductVarientInventoryCreatedEvent;
import com.inventory_service.application.inventory.command.InitializeInventoryCommand;
import com.inventory_service.application.inventory.usecase.InitializeInventoryUseCase;

import tools.jackson.databind.ObjectMapper;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ProductVariantKafkaConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(ProductVariantKafkaConsumer.class);

    private final InitializeInventoryUseCase initializeUseCase;
    private final ObjectMapper objectMapper;

    public ProductVariantKafkaConsumer(
            InitializeInventoryUseCase initializeUseCase,
            ObjectMapper objectMapper
    ) {
        this.initializeUseCase =
                Objects.requireNonNull(initializeUseCase);

        this.objectMapper =
                Objects.requireNonNull(objectMapper);
    }

    @KafkaListener(
            topics = "${inventory.kafka.product-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onMessage(ConsumerRecord<String, String> record) {

        String payload = record.value();

        log.info(
                "Received Product inventory event: topic={}, partition={}, offset={}",
                record.topic(),
                record.partition(),
                record.offset()
        );

        ProductVarientInventoryCreatedEvent event =
                objectMapper.readValue(
                        payload,
                        ProductVarientInventoryCreatedEvent.class
                );

        if (!ProductVarientInventoryCreatedEvent.EVENT_TYPE
                .equals(event.eventType())) {

            throw new IllegalArgumentException(
                    "Unsupported eventType: " + event.eventType()
            );
        }

        Objects.requireNonNull(event.eventId(), "eventId is required");
        Objects.requireNonNull(event.productId(), "productId is required");
        Objects.requireNonNull(event.variantId(), "variantId is required");
        Objects.requireNonNull(event.shopId(), "shopId is required");

        InitializeInventoryCommand command =
                new InitializeInventoryCommand(
                        event.productId(),
                        event.variantId(),
                        event.shopId()
                );

        initializeUseCase.execute(command);

        log.info(
                "Inventory initialized successfully: eventId={}, variantId={}",
                event.eventId(),
                event.variantId()
        );
    }
}
