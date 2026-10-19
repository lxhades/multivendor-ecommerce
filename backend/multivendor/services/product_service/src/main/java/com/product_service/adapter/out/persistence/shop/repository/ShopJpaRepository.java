package com.product_service.adapter.out.persistence.shop.repository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.product_service.adapter.out.persistence.shop.entity.ShopJpaEntity;
public interface ShopJpaRepository extends JpaRepository<ShopJpaEntity, UUID> {
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, UUID id);
    List<ShopJpaEntity> findAllByOrderByCreatedAtDesc();
    List<ShopJpaEntity> findBySellerIdOrderByCreatedAtDesc(UUID sellerId);
}
