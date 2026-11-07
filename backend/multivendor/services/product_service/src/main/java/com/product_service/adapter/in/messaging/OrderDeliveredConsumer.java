package com.product_service.adapter.in.messaging;

import com.product_service.application.product.usecase.RecordDeliveredOrderSalesUseCase;
import com.product_service.application.product.usecase.RecordDeliveredOrderSalesUseCase.Event;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class OrderDeliveredConsumer {
    private final ObjectMapper mapper;
    private final RecordDeliveredOrderSalesUseCase recordDeliveredOrderSalesUseCase;

    public OrderDeliveredConsumer(
            ObjectMapper mapper,
            RecordDeliveredOrderSalesUseCase recordDeliveredOrderSalesUseCase)
    {
        this.mapper = mapper;
        this.recordDeliveredOrderSalesUseCase = recordDeliveredOrderSalesUseCase;
    }

    @KafkaListener(topics = "${product.kafka.delivered-topic}",
            groupId = "product-order-delivered-consumer")
    public void consume(String payload) {

        recordDeliveredOrderSalesUseCase.execute(mapper.readValue(payload, Event.class));
    }
}
