
package com.product_service.application.product.usecase;

import com.product_service.adapter.out.persistence.product.repository.ProductVariantJpaRepository;
import com.product_service.application.product.dto.ProductDtos;
import com.product_service.domain.model.aggregate.Product;
import com.product_service.domain.model.enumtype.ApprovalStatus;
import com.product_service.domain.model.enumtype.ProductStatus;
import com.product_service.domain.model.enumtype.ShopStatus;
import com.product_service.domain.model.vo.ShopId;
import com.product_service.domain.repository.ShopRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.*;

@Service
public class VerifyVariantsUseCase {

    private final ProductVariantJpaRepository variants;
    private final ShopRepository shops;

    public VerifyVariantsUseCase(
            ProductVariantJpaRepository variants,
            ShopRepository shops
    ) {
        this.variants = variants;
        this.shops = shops;
    }

    @Transactional(readOnly = true)
    public List<VerifiedVariant> execute(List<UUID> ids) {

        if (ids == null || ids.isEmpty() || ids.size() > 100
                || ids.stream().anyMatch(Objects::isNull)
                || new HashSet<>(ids).size() != ids.size()) {
            throw new IllegalArgumentException("Invalid variant IDs");
        }

        var rows = variants.findAllWithProduct(ids);

        Map<UUID, Boolean> shopAvailability = new HashMap<>();
        Map<UUID, VerifiedVariant> results = new HashMap<>();

        for (var v : rows) {
            var p = v.product;

            boolean shopActive = shopAvailability.computeIfAbsent(
                    p.shopId,
                    id -> shops.findById(new ShopId(id))
                            .map(s -> s.getStatus() == ShopStatus.ACTIVE)
                            .orElse(false)
            );


            boolean available =
                    v.deletedAt == null
                            && v.active
                            && p.deletedAt == null
                            && p.status == ProductStatus.ACTIVE
                            && p.approvalStatus == ApprovalStatus.APPROVED
                            && shopActive;
            if (v.deletedAt != null) {
                System.out.println("    -> FAIL: Variant đã bị xóa");

            }

            System.out.println("[2] v.active:           " + v.active);
            if (!v.active) {
                System.out.println("    -> FAIL: Variant đang tắt");

            }

            System.out.println("[3] p.deletedAt:        " + p.deletedAt);
            if (p.deletedAt != null) {
                System.out.println("    -> FAIL: Product đã bị xóa");

            }

            System.out.println("[4] p.status:           " + p.status);
            if (p.status != ProductStatus.ACTIVE) {
                System.out.println("    -> FAIL: Product không ở trạng thái ACTIVE");

            }

            System.out.println("[5] p.approvalStatus:   " + p.approvalStatus);
            if (p.approvalStatus != ApprovalStatus.APPROVED) {
                System.out.println("    -> FAIL: Product chưa được duyệt");

            }

            System.out.println("[6] shopActive:         " + shopActive);
            if (!shopActive) {
                System.out.println("    -> FAIL: Shop đang đóng");

            }


            long price = v.price
                    .setScale(0, RoundingMode.UNNECESSARY)
                    .longValueExact();

            results.put(v.id, new VerifiedVariant(
                    p.id,
                    v.id,
                    p.shopId,
                    p.name,
                    price,
                    available
            ));
        }

        return ids.stream()
                .map(results::get)
                .filter(Objects::nonNull)
                .toList();
    }

    public record VerifiedVariant(
            UUID productId,
            UUID variantId,
            UUID shopId,
            String productName,
            long unitPrice,
            boolean available
    ) {}

}
