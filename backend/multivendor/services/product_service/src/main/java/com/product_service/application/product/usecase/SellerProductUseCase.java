package com.product_service.application.product.usecase;

import com.product_service.application.product.dto.ProductDtos.ProductInput;
import com.product_service.application.product.dto.ProductDtos.Seller;
import com.product_service.application.product.dto.ProductDtos.VariantInput;
import com.product_service.application.product.port.ShopOwnershipPort;
import com.product_service.domain.model.aggregate.Product;
import com.product_service.domain.model.entity.ProductVariant;
import com.product_service.domain.model.enumtype.ProductStatus;
import com.product_service.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class SellerProductUseCase {

    private final ProductRepository repo;
    private final ShopOwnershipPort ownership;
    private final Clock clock;

    public SellerProductUseCase(ProductRepository repo, ShopOwnershipPort ownership, Clock clock) {
        this.repo = repo;
        this.ownership = ownership;
        this.clock = clock;
    }

    // Tạo mới sản phẩm cho Seller
    @Transactional
    public Seller create(UUID sellerId, UUID shopId, ProductInput in) {
        if (!ownership.ownedBy(shopId, sellerId)) {
            throw new RuntimeException("Bạn không sở hữu cửa hàng này");
        }
        if (repo.existsSlug(shopId, in.slug(), null)) {
            throw new IllegalArgumentException("Slug đã tồn tại");
        }

        Product p = new Product(
                UUID.randomUUID(), shopId, in.categoryId(), in.name(),
                in.slug(), in.description(), in.mediaJson(), clock.instant()
        );

        if (in.variants() != null) {
            for (VariantInput v : in.variants()) {
                p.addVariant(new ProductVariant(
                        UUID.randomUUID(), v.sku(), v.price(),
                        v.attributesJson(), v.active(), 0, null
                ), clock.instant());
            }
        }

        return Seller.of(repo.save(p));
    }

    // Cập nhật thông tin sản phẩm
    @Transactional
    public Seller update(UUID sellerId, UUID shopId, UUID id, ProductInput in) {
        if (!ownership.ownedBy(shopId, sellerId)) {
            throw new SecurityException("Bạn không sở hữu cửa hàng này");
        }
        Product p = repo.findByIdAndShopId(id, shopId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sản phẩm"));

        if (repo.existsSlug(shopId, in.slug(), id)) {
            throw new IllegalArgumentException("Slug đã tồn tại");
        }

        p.edit(in.categoryId(), in.name(), in.slug(), in.description(), in.mediaJson(), clock.instant());
        return Seller.of(repo.save(p));
    }

    // Xóa mềm sản phẩm (Soft Delete)
    @Transactional
    public void delete(UUID sellerId, UUID shopId, UUID id) {
        if (!ownership.ownedBy(shopId, sellerId)) {
            throw new SecurityException("Bạn không sở hữu cửa hàng này");
        }
        Product p = repo.findByIdAndShopId(id, shopId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sản phẩm"));

        p.softDelete(clock.instant());
        repo.save(p);
    }

    // Thay đổi trạng thái kinh doanh của sản phẩm (ACTIVE / INACTIVE)
    @Transactional
    public Seller changeStatus(UUID sellerId, UUID shopId, UUID id, ProductStatus status) {
        if (!ownership.ownedBy(shopId, sellerId)) {
            throw new SecurityException("Bạn không sở hữu cửa hàng này");
        }
        Product p = repo.findByIdAndShopId(id, shopId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sản phẩm"));

        p.changeStatus(status, clock.instant());
        return Seller.of(repo.save(p));
    }

    // Xem chi tiết sản phẩm (Dành cho Seller)
    @Transactional(readOnly = true)
    public Seller details(UUID sellerId, UUID shopId, UUID id) {
        if (!ownership.ownedBy(shopId, sellerId)) {
            throw new SecurityException("Bạn không sở hữu cửa hàng này");
        }
        Product p = repo.findByIdAndShopId(id, shopId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sản phẩm"));

        return Seller.of(p);
    }

    // Lấy danh sách toàn bộ sản phẩm của cửa hàng
    @Transactional(readOnly = true)
    public List<Seller> list(UUID sellerId, UUID shopId) {
        if (!ownership.ownedBy(shopId, sellerId)) {
            throw new SecurityException("Bạn không sở hữu cửa hàng này");
        }
        return repo.findByShop(shopId).stream().map(Seller::of).toList();
    }

    // Thêm biến thể (Variant) cho sản phẩm
    @Transactional
    public Seller addVariant(UUID sellerId, UUID shopId, UUID productId, VariantInput in) {
        if (!ownership.ownedBy(shopId, sellerId)) {
            throw new SecurityException("Bạn không sở hữu cửa hàng này");
        }
        Product p = repo.findByIdAndShopId(productId, shopId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sản phẩm"));

        p.addVariant(new ProductVariant(
                UUID.randomUUID(), in.sku(), in.price(),
                in.attributesJson(), in.active(), 0, null
        ), clock.instant());

        return Seller.of(repo.save(p));
    }

    // Cập nhật thông tin biến thể
    @Transactional
    public Seller updateVariant(UUID sellerId, UUID shopId, UUID productId, UUID variantId, VariantInput in) {
        if (!ownership.ownedBy(shopId, sellerId)) {
            throw new SecurityException("Bạn không sở hữu cửa hàng này");
        }
        Product p = repo.findByIdAndShopId(productId, shopId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sản phẩm"));

        p.editVariant(variantId, in.sku(), in.price(), in.attributesJson(), in.active(), clock.instant());
        return Seller.of(repo.save(p));
    }

    // Xóa biến thể khỏi sản phẩm
    @Transactional
    public Seller deleteVariant(UUID sellerId, UUID shopId, UUID productId, UUID variantId) {
        if (!ownership.ownedBy(shopId, sellerId)) {
            throw new SecurityException("Bạn không sở hữu cửa hàng này");
        }
        Product p = repo.findByIdAndShopId(productId, shopId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sản phẩm"));

        p.deleteVariant(variantId, clock.instant());
        return Seller.of(repo.save(p));
    }
}