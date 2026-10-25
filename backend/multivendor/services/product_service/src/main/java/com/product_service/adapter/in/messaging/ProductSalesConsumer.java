package com.product_service.adapter.in.messaging;

import com.product_service.application.product.usecase.RecordSaleEventUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
public class ProductSalesConsumer {

    private final RecordSaleEventUseCase usecase;

    public ProductSalesConsumer(RecordSaleEventUseCase usecase) {
        this.usecase = usecase;
    }

    public record SalesEvent(
            UUID eventId,
            String eventType,
            UUID orderItemId,
            UUID productId,
            UUID variantId,
            long quantity,
            BigDecimal amount,
            Instant occurredAt
    ) {
    }

    @KafkaListener(topics = "${app.kafka.sales-topic:product.sales.events}", groupId = "product-service-group")
    public void consume(SalesEvent event) {
        usecase.execute(
                event.eventId(),
                event.eventType(),
                event.orderItemId(),
                event.productId(),
                event.variantId(),
                event.quantity(),
                event.amount(),
                event.occurredAt()
        );
    }
}