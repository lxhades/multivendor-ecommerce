package com.product_service.adapter.out.persistence.shop;

import com.product_service.adapter.out.persistence.shop.entity.ShopJpaEntity;
import com.product_service.adapter.out.persistence.shop.mapper.ShopPersistenceMapper;
import com.product_service.adapter.out.persistence.shop.repository.ShopJpaRepository;
import com.product_service.domain.model.aggregate.Shop;
import com.product_service.domain.model.vo.SellerId;
import com.product_service.domain.model.vo.ShopId;
import com.product_service.domain.model.vo.Slug;
import com.product_service.domain.repository.ShopRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class ShopRepositoryAdapter implements ShopRepository {
    private final ShopJpaRepository jpa;

    public ShopRepositoryAdapter(ShopJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Shop> findById(ShopId id) {

        return jpa.findById(id.value()).map(ShopPersistenceMapper::toDomain);
    }

    @Override
    public Page<Shop> findAll(Pageable pageable) {
        Page<ShopJpaEntity> jpaPage = jpa.findAllByOrderByCreatedAtDesc(pageable);
        return jpaPage.map(ShopPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Shop> findBySellerId(SellerId sellerId) {
        return jpa.findBySellerId(sellerId.value())
                .map(ShopPersistenceMapper::toDomain);


    }

    @Override
    @Transactional
    public Shop save(Shop shop) {
        return ShopPersistenceMapper.toDomain(jpa.save(ShopPersistenceMapper.toEntity(shop)));
    }

    @Override
    public boolean existsBySlug(Slug slug, ShopId excludingId) {
        return excludingId == null ? jpa.existsBySlug(slug.value()) : jpa.existsBySlugAndIdNot(slug.value(), excludingId.value());
    }

    @Override
    public boolean existsShopBySellerId(SellerId sellerId) {
        return jpa.existsBySellerId(sellerId.value());
    }
}
