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

        // 1. Log nhận message
        log.info(
                "Đã nhận sự kiện khởi tạo tồn kho từ Product Service: topic={}, partition={}, offset={}",
                record.topic(),
                record.partition(),
                record.offset()
        );

        ProductVarientInventoryCreatedEvent event =
                objectMapper.readValue(
                        payload,
                        ProductVarientInventoryCreatedEvent.class
                );

        // 2. Validate eventType
        if (!ProductVarientInventoryCreatedEvent.EVENT_TYPE
                .equals(event.eventType())) {

            throw new IllegalArgumentException(
                    "Loại sự kiện không được hỗ trợ: " + event.eventType()
            );
        }

        // 3. Validate dữ liệu đầu vào
        Objects.requireNonNull(event.eventId(), "Thiếu thông tin eventId");
        Objects.requireNonNull(event.productId(), "Thiếu thông tin productId");
        Objects.requireNonNull(event.variantId(), "Thiếu thông tin variantId");
        Objects.requireNonNull(event.shopId(), "Thiếu thông tin shopId");

        // 4. Map sang Command
        InitializeInventoryCommand command =
                new InitializeInventoryCommand(
                        event.productId(),
                        event.variantId(),
                        event.shopId()
                );

        // 5. Thực thi UseCase
        initializeUseCase.execute(command);

        // 6. Log thành công
        log.info(
                "Khởi tạo tồn kho thành công: eventId={}, variantId={}",
                event.eventId(),
                event.variantId()
        );
    }
}