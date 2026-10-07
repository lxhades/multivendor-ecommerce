package com.catalog_service.adapters.out.persistence.repository;

import com.catalog_service.adapters.out.persistence.entity.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataProductRepository extends JpaRepository<ProductJpaEntity, UUID> {
}
