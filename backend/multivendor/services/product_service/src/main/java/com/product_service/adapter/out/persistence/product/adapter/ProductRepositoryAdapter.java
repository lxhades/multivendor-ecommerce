package com.product_service.adapter.out.persistence.product.adapter;

import com.product_service.adapter.out.persistence.product.entity.ProductApprovalHistoryJpaEntity;
import com.product_service.adapter.out.persistence.product.entity.ProductJpaEntity;
import com.product_service.adapter.out.persistence.product.entity.ProductVariantJpaEntity;
import com.product_service.adapter.out.persistence.product.repository.ProductJpaRepository;
import com.product_service.domain.model.aggregate.Product;
import com.product_service.domain.model.entity.ProductApprovalHistory;
import com.product_service.domain.model.entity.ProductVariant;
import com.product_service.domain.repository.ProductRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository jpa;

    public ProductRepositoryAdapter(ProductJpaRepository jpa) {
        this.jpa = jpa;
    }

    // Chuyển đổi JPA Entity sang Domain Model
    private Product map(ProductJpaEntity e) {
        List<ProductVariant> domainVariants = e.variants.stream()
                .map(v -> new ProductVariant(
                        v.id, v.sku, v.price, v.attributesJson, v.active, v.soldCount, v.deletedAt
                ))
                .toList();

        List<ProductApprovalHistory> domainHistories = e.histories.stream()
                .map(h -> new ProductApprovalHistory(
                        h.id, h.actorId, h.decision, h.reason, h.createdAt
                ))
                .toList();

        return Product.rehydrate(
                e.id, e.shopId, e.categoryId, e.name, e.slug, e.description,
                e.mediaJson, e.status, e.approvalStatus, e.soldCount,
                e.createdAt, e.updatedAt, e.deletedAt,
                domainVariants, domainHistories
        );
    }

    @Override
    public Product save(Product p) {
        ProductJpaEntity e = jpa.findById(p.id()).orElse(null);

        if (e == null) {
            e = new ProductJpaEntity();
            e.id = p.id();
            e.shopId = p.shopId();
            e.createdAt = p.createdAt();
        }

        e.categoryId = p.categoryId();
        e.name = p.name();
        e.slug = p.slug();
        e.description = p.description();
        e.mediaJson = p.mediaJson();
        e.status = p.status();
        e.approvalStatus = p.approvalStatus();
        e.minPrice = p.minPrice();
        e.soldCount = p.soldCount();
        e.updatedAt = p.updatedAt();
        e.deletedAt = p.deletedAt();

        // 1. Đồng bộ Variants
        Map<UUID, ProductVariantJpaEntity> oldVariants = new HashMap<>();
        for (var v : e.variants) {
            oldVariants.put(v.id, v);
        }

        for (var v : p.variants()) {
            var row = oldVariants.get(v.id());
            if (row == null) {
                row = new ProductVariantJpaEntity();
                row.id = v.id();
                row.product = e;
                row.createdAt = p.createdAt();
                e.variants.add(row);
            }
            row.sku = v.sku();
            row.price = v.price();
            row.attributesJson = v.attributesJson();
            row.active = v.active();
            row.soldCount = v.soldCount();
            row.deletedAt = v.deletedAt();
            row.updatedAt = p.updatedAt();
        }

        // 2. Đồng bộ Histories
        Set<UUID> existingHistories = new HashSet<>();
        for (var h : e.histories) {
            existingHistories.add(h.id);
        }

        for (var h : p.histories()) {
            if (existingHistories.add(h.id())) {
                var row = new ProductApprovalHistoryJpaEntity();
                row.id = h.id();
                row.product = e;
                row.actorId = h.actorId();
                row.decision = h.decision();
                row.reason = h.reason();
                row.createdAt = h.createdAt();
                e.histories.add(row);
            }
        }

        return map(jpa.saveAndFlush(e));
    }

    // Tìm sản phẩm theo ID
    @Override
    public Optional<Product> findById(UUID id) {
        return jpa.findById(id).map(this::map);
    }

    // Tìm sản phẩm theo ID và Shop ID (dùng cho Seller)
    @Override
    public Optional<Product> findByIdAndShopId(UUID id, UUID shop) {
        return jpa.byShop(id, shop).map(this::map);
    }

    // Tìm sản phẩm hiển thị (đã duyệt và đang bán) theo ID (dùng cho Buyer)
    @Override
    public Optional<Product> findVisibleById(UUID id) {
        return jpa.visibleById(id).map(this::map);
    }

    // Tìm danh sách sản phẩm hiển thị theo Shop, Danh mục (có phân cấp)
    @Override
    public List<Product> findVisible(UUID shop, UUID ignored, Collection<UUID> ids) {
        List<ProductJpaEntity> entities;

        if (ids == null) {
            if (shop == null) {
                entities = jpa.visibleAll();
            } else {
                entities = jpa.visibleShop(shop);
            }
        } else {
            if (ids.isEmpty()) {
                entities = List.of();
            } else {
                if (shop == null) {
                    entities = jpa.visibleCategories(ids);
                } else {
                    entities = jpa.visibleShopCategories(shop, ids);
                }
            }
        }

        return entities.stream().map(this::map).toList();
    }

    // Lấy danh sách sản phẩm theo Shop (dùng cho Seller)
    @Override
    public List<Product> findByShop(UUID shop) {
        return jpa.byShopList(shop).stream().map(this::map).toList();
    }

    // Lấy danh sách sản phẩm đang chờ duyệt (dùng cho Admin)
    @Override
    public List<Product> findPending() {
        return jpa.pending().stream().map(this::map).toList();
    }

    // Kiểm tra Slug đã tồn tại trong Shop
    @Override
    public boolean existsSlug(UUID shop, String slug, UUID except) {
        return jpa.slugExists(shop, slug, except);
    }
}