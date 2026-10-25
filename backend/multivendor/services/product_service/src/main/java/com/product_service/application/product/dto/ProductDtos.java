package com.product_service.application.product.dto;

import com.product_service.domain.model.aggregate.Product;
import com.product_service.domain.model.entity.ProductApprovalHistory;
import com.product_service.domain.model.entity.ProductVariant;
import com.product_service.domain.model.enumtype.ApprovalStatus;
import com.product_service.domain.model.enumtype.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ProductDtos {

    private ProductDtos() {
    }

    // DTO hiển thị thông tin biến thể sản phẩm
    public record Variant(
            UUID id,
            String sku,
            BigDecimal price,
            String attributesJson,
            boolean active,
            long soldCount,
            Instant deletedAt
    ) {
        // Chuyển đổi từ Domain Entity sang DTO
        public static Variant of(ProductVariant v) {
            return new Variant(
                    v.id(),
                    v.sku(),
                    v.price(),
                    v.attributesJson(),
                    v.active(),
                    v.soldCount(),
                    v.deletedAt()
            );
        }
    }

    // DTO hiển thị sản phẩm dành cho Buyer (chỉ chứa các biến thể đang hoạt động)
    public record Buyer(
            UUID id,
            UUID shopId,
            UUID categoryId,
            String name,
            String slug,
            String description,
            String mediaJson,
            BigDecimal minPrice,
            long soldCount,
            List<Variant> variants
    ) {
        // Chuyển đổi từ Domain sang DTO (có xử lý lọc biến thể nếu cần chi tiết)
        public static Buyer of(Product p, boolean details) {
            List<Variant> activeVariants = List.of();
            if (details) {
                activeVariants = p.variants().stream()
                        .filter(v -> v.deletedAt() == null && v.active())
                        .map(Variant::of)
                        .toList();
            }

            return new Buyer(
                    p.id(),
                    p.shopId(),
                    p.categoryId(),
                    p.name(),
                    p.slug(),
                    p.description(),
                    p.mediaJson(),
                    p.minPrice(),
                    p.soldCount(),
                    activeVariants
            );
        }
    }

    // DTO hiển thị đầy đủ thông tin sản phẩm dành cho Seller (bao gồm cả lịch sử duyệt)
    public record Seller(
            UUID id,
            UUID shopId,
            UUID categoryId,
            String name,
            String slug,
            String description,
            String mediaJson,
            ProductStatus status,
            ApprovalStatus approvalStatus,
            BigDecimal minPrice,
            long soldCount,
            Instant deletedAt,
            Instant createdAt,
            Instant updatedAt,
            List<Variant> variants,
            List<ProductApprovalHistory> histories
    ) {
        // Chuyển đổi từ Domain sang DTO dành cho Seller
        public static Seller of(Product p) {
            return new Seller(
                    p.id(),
                    p.shopId(),
                    p.categoryId(),
                    p.name(),
                    p.slug(),
                    p.description(),
                    p.mediaJson(),
                    p.status(),
                    p.approvalStatus(),
                    p.minPrice(),
                    p.soldCount(),
                    p.deletedAt(),
                    p.createdAt(),
                    p.updatedAt(),
                    p.variants().stream().map(Variant::of).toList(),
                    p.histories()
            );
        }
    }

    // DTO đầu vào để tạo hoặc cập nhật sản phẩm
    public record ProductInput(
            UUID categoryId,
            String name,
            String slug,
            String description,
            String mediaJson,
            List<VariantInput> variants
    ) {
    }

    // DTO đầu vào cho thông tin biến thể sản phẩm
    public record VariantInput(
            String sku,
            BigDecimal price,
            String attributesJson,
            boolean active
    ) {
    }

    // DTO đầu vào để thay đổi trạng thái bán của sản phẩm
    public record StatusInput(
            ProductStatus status
    ) {
    }

    // DTO đầu vào cho quyết định duyệt/từ chối sản phẩm của Admin
    public record DecisionInput(
            String reason
    ) {
    }
}