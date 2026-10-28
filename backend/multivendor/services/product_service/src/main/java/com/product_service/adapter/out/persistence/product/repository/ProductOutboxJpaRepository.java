package com.product_service.adapter.out.persistence.product.repository;

import com.product_service.adapter.out.persistence.product.entity.ProductOutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ProductOutboxJpaRepository extends JpaRepository<ProductOutboxEventEntity, UUID> {
    List<ProductOutboxEventEntity> findTop50ByStatusOrderByCreatedAtAsc(String status);
}
