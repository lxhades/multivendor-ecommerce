package com.product_service.application.product.usecase;

import com.product_service.application.integration.event.ProductVarientInventoryCreatedEvent;
import com.product_service.application.integration.port.out.ProductOutboxRepository;
import com.product_service.application.product.dto.ProductDtos.Seller;
import com.product_service.domain.model.enumtype.ApprovalStatus;
import com.product_service.domain.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AdminProductUseCase {

    private final ProductRepository repo;
    private final Clock clock;
    private final ObjectMapper objectMapper;
    private final ProductOutboxRepository productOutboxRepository;

    @Transactional(readOnly = true)
    public List<Seller> pending() {
        return repo.findPending()
                .stream()
                .map(Seller::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public Seller details(UUID id) {
        return repo.findById(id)
                .map(Seller::of)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
    }


    @Transactional
    public Seller decide(
            UUID adminId,
            UUID productId,
            ApprovalStatus status,
            String reason
    ) {
        var p = repo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        p.decide(status, adminId, reason, clock.instant());

        repo.save(p);

        if (status == ApprovalStatus.APPROVED) {

            for (var variant : p.variants()) {

                ProductVarientInventoryCreatedEvent event =
                        new ProductVarientInventoryCreatedEvent(
                                UUID.randomUUID(),
                                ProductVarientInventoryCreatedEvent.EVENT_TYPE,
                                p.id(),
                                variant.id(),
                                p.shopId()
                        );

                String payload =
                        objectMapper.writeValueAsString(event);

                productOutboxRepository.save(
                        event.eventId(),
                        "PRODUCT_VARIANT",
                        event.variantId(),
                        event.eventType(),
                        payload
                );
            }}


        return Seller.of(p);
    }
}
