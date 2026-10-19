package com.product_service.adapter.out.persistence.shop;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.product_service.adapter.out.persistence.shop.mapper.ShopPersistenceMapper;
import com.product_service.adapter.out.persistence.shop.repository.ShopJpaRepository;
import com.product_service.domain.model.aggregate.Shop;
import com.product_service.domain.model.vo.*;
import com.product_service.domain.repository.ShopRepository;

@Repository
@Transactional(readOnly = true)
public class ShopRepositoryAdapter implements ShopRepository {

    private final ShopJpaRepository jpa;

    public ShopRepositoryAdapter(ShopJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Shop> findById(ShopId id) {
        return jpa.findById(id.value())
                .map(ShopPersistenceMapper::toDomain);
    }

    @Override
    public List<Shop> findAll() {
        return jpa.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ShopPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Shop> findBySellerId(SellerId sellerId) {
        return jpa.findBySellerIdOrderByCreatedAtDesc(sellerId.value())
                .stream()
                .map(ShopPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public Shop save(Shop shop) {
        return ShopPersistenceMapper.toDomain(
                jpa.save(ShopPersistenceMapper.toEntity(shop))
        );
    }

    @Override
    public boolean existsBySlug(Slug slug, ShopId excludingId) {
        return excludingId == null
                ? jpa.existsBySlug(slug.value())
                : jpa.existsBySlugAndIdNot(slug.value(), excludingId.value());
    }
}