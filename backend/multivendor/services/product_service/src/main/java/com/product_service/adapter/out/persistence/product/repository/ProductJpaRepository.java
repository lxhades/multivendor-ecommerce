package com.product_service.adapter.out.persistence.product.repository;

import com.product_service.adapter.out.persistence.product.entity.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, UUID> {

    // Lấy sản phẩm theo ID và Shop ID (dành cho Seller thao tác trên sản phẩm của mình)
    @Query("""
            SELECT p FROM ProductJpaEntity p 
            WHERE p.id = :id 
              AND p.shopId = :shop 
              AND p.deletedAt IS NULL
            """)
    Optional<ProductJpaEntity> byShop(@Param("id") UUID id, @Param("shop") UUID shop);

    // Lấy chi tiết sản phẩm đang hiển thị (Đã duyệt, Đang bán) theo ID (dành cho Buyer)
    @Query("""
            SELECT p FROM ProductJpaEntity p 
            WHERE p.id = :id 
              AND p.deletedAt IS NULL 
              AND p.status = ProductStatus.ACTIVE 
              AND p.approvalStatus = ApprovalStatus.APPROVED
            """)
    Optional<ProductJpaEntity> visibleById(@Param("id") UUID id);

    // Lấy tất cả sản phẩm đang hiển thị (Đã duyệt, Đang bán)
    @Query("""
            SELECT p FROM ProductJpaEntity p 
            WHERE p.deletedAt IS NULL 
              AND p.status = ProductStatus.ACTIVE 
              AND p.approvalStatus = ApprovalStatus.APPROVED 
            ORDER BY p.createdAt DESC
            """)
    List<ProductJpaEntity> visibleAll();

    // Lấy danh sách sản phẩm đang hiển thị của một Shop cụ thể
    @Query("""
            SELECT p FROM ProductJpaEntity p 
            WHERE p.deletedAt IS NULL 
              AND p.status = ProductStatus.ACTIVE 
              AND p.approvalStatus = ApprovalStatus.APPROVED 
              AND p.shopId = :shop 
            ORDER BY p.createdAt DESC
            """)
    List<ProductJpaEntity> visibleShop(@Param("shop") UUID shop);

    // Lấy danh sách sản phẩm đang hiển thị thuộc một tập hợp các Danh mục
    @Query("""
            SELECT p FROM ProductJpaEntity p 
            WHERE p.deletedAt IS NULL 
              AND p.status = ProductStatus.ACTIVE 
              AND p.approvalStatus = ApprovalStatus.APPROVED 
              AND p.categoryId IN :categoryIds 
            ORDER BY p.createdAt DESC
            """)
    List<ProductJpaEntity> visibleCategories(@Param("categoryIds") Collection<UUID> ids);

    // Lấy danh sách sản phẩm đang hiển thị của một Shop cụ thể và thuộc tập hợp các Danh mục
    @Query("""
            SELECT p FROM ProductJpaEntity p 
            WHERE p.deletedAt IS NULL 
              AND p.status = ProductStatus.ACTIVE 
              AND p.approvalStatus = ApprovalStatus.APPROVED 
              AND p.shopId = :shop 
              AND p.categoryId IN :categoryIds 
            ORDER BY p.createdAt DESC
            """)
    List<ProductJpaEntity> visibleShopCategories(@Param("shop") UUID shop, @Param("categoryIds") Collection<UUID> ids);

    // Lấy tất cả sản phẩm của một Shop (dành cho Seller xem danh sách quản lý, không phân biệt trạng thái)
    @Query("""
            SELECT p FROM ProductJpaEntity p 
            WHERE p.shopId = :shop 
              AND p.deletedAt IS NULL 
            ORDER BY p.createdAt DESC
            """)
    List<ProductJpaEntity> byShopList(@Param("shop") UUID shop);

    // Lấy danh sách sản phẩm đang chờ duyệt (dành cho Admin)
    @Query("""
            SELECT p FROM ProductJpaEntity p 
            WHERE p.deletedAt IS NULL 
              AND p.approvalStatus = ApprovalStatus.PENDING 
            ORDER BY p.updatedAt ASC
            """)
    List<ProductJpaEntity> pending();

    // Kiểm tra xem Slug đã tồn tại trong Shop chưa (Bỏ qua ID của sản phẩm đang cập nhật để tránh báo lỗi trùng chính nó)
    @Query("""
            SELECT count(p) > 0 FROM ProductJpaEntity p 
            WHERE p.shopId = :shop 
              AND p.slug = :slug 
              AND p.deletedAt IS NULL 
              AND (:except IS NULL OR p.id <> :except)
            """)
    boolean slugExists(@Param("shop") UUID shop, @Param("slug") String slug, @Param("except") UUID except);
}