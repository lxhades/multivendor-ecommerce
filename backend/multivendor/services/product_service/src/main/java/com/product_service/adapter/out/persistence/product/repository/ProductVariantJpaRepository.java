
package com.product_service.adapter.out.persistence.product.repository;

import com.product_service.adapter.out.persistence.product.entity.ProductVariantJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ProductVariantJpaRepository
        extends JpaRepository<ProductVariantJpaEntity, UUID> {

    @Query("""
        SELECT v
        FROM ProductVariantJpaEntity v
        JOIN FETCH v.product p
        WHERE v.id IN :ids
        """)
    List<ProductVariantJpaEntity> findAllWithProduct(
            @Param("ids") Collection<UUID> ids
    );
}
