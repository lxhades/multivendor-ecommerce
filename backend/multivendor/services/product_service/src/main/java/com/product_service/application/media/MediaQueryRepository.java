package com.product_service.application.media;

import com.product_service.adapter.out.persistence.product.entity.ProductJpaEntity;
import com.product_service.adapter.out.persistence.product.entity.ProductVariantJpaEntity;
import com.product_service.adapter.out.persistence.shop.entity.ShopJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MediaQueryRepository extends JpaRepository<ProductJpaEntity, UUID> {

    @Query("SELECT p.mediaJson FROM ProductJpaEntity p WHERE p.mediaJson IS NOT NULL AND p.mediaJson <> ''")
    List<String> findAllProductMediaJson();

    @Query("SELECT v.imageUrl FROM ProductVariantJpaEntity v WHERE v.imageUrl IS NOT NULL AND v.imageUrl <> ''")
    List<String> findAllVariantImageUrls();

    @Query("SELECT s.logoUrl FROM ShopJpaEntity s WHERE s.logoUrl IS NOT NULL")
    List<String> findAllShopLogoUrls();

    @Query("SELECT s.bannerUrl FROM ShopJpaEntity s WHERE s.bannerUrl IS NOT NULL")
    List<String> findAllShopBannerUrls();
}