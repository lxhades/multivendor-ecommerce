package com.order_service.adapter.out.persistence.jpa;

import com.order_service.adapter.out.persistence.entity.ProductReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ProductReviewRepository extends JpaRepository<ProductReviewEntity, UUID> {
    boolean existsByOrderIdAndProductId(UUID orderId, UUID productId);
    Page<ProductReviewEntity> findByProductId(UUID productId, Pageable pageable);

    @Query("select coalesce(avg(r.rating), 0.0) from ProductReviewEntity r where r.productId = :productId")
    double averageRating(@Param("productId") UUID productId);
}
