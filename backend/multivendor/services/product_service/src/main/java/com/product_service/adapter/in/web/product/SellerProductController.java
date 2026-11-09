package com.product_service.adapter.in.web.product;

import com.common.dto.response.ApiResponse;
import com.product_service.application.product.dto.ProductDtos;
import com.product_service.application.product.dto.ProductDtos.Seller;
import com.product_service.application.product.usecase.SellerProductUseCase;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products/seller/shops/{shopId}")
@AllArgsConstructor
public class SellerProductController {

    private final SellerProductUseCase usecase;

    private UUID currentUserId(Authentication actor) {
        String sub = actor.getName();
        if (sub == null || sub.isBlank()) {
            throw new IllegalStateException("JWT không có subject");
        }
        return UUID.fromString(sub);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Seller>> create(
            Authentication actor,
            @PathVariable UUID shopId,
            @RequestBody ProductDtos.ProductInput in) {

        Seller result = usecase.create(currentUserId(actor), shopId, in);
        return ResponseEntity.ok(ApiResponse.success("Tạo sản phẩm thành công", result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Seller>> update(
            Authentication actor,
            @PathVariable UUID shopId,
            @PathVariable UUID id,
            @RequestBody ProductDtos.ProductInput in) {

        Seller result = usecase.update(currentUserId(actor), shopId, id, in);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật sản phẩm thành công", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            Authentication actor,
            @PathVariable UUID shopId,
            @PathVariable UUID id) {

        usecase.delete(currentUserId(actor), shopId, id);
        return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm thành công", null));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Seller>> status(
            Authentication actor,
            @PathVariable UUID shopId,
            @PathVariable UUID id,
            @RequestBody ProductDtos.StatusInput in) {

        Seller result = usecase.changeStatus(currentUserId(actor), shopId, id, in.status());
        return ResponseEntity.ok(ApiResponse.success("Thay đổi trạng thái thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Seller>> details(
            Authentication actor,
            @PathVariable UUID shopId,
            @PathVariable UUID id) {

        Seller result = usecase.details(currentUserId(actor), shopId, id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Seller>>> list(
            Authentication actor,
            @PathVariable UUID shopId) {

        List<Seller> result = usecase.list(currentUserId(actor), shopId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{id}/variants")
    public ResponseEntity<ApiResponse<Seller>> addVariant(
            Authentication actor,
            @PathVariable UUID shopId,
            @PathVariable UUID id,
            @RequestBody ProductDtos.VariantInput in) {

        Seller result = usecase.addVariant(currentUserId(actor), shopId, id, in);
        return ResponseEntity.ok(ApiResponse.success("Thêm biến thể thành công", result));
    }

    @PutMapping("/{id}/variants/{variantId}")
    public ResponseEntity<ApiResponse<Seller>> updateVariant(
            Authentication actor,
            @PathVariable UUID shopId,
            @PathVariable UUID id,
            @PathVariable UUID variantId,
            @RequestBody ProductDtos.VariantInput in) {

        Seller result = usecase.updateVariant(currentUserId(actor), shopId, id, variantId, in);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật biến thể thành công", result));
    }

    @DeleteMapping("/{id}/variants/{variantId}")
    public ResponseEntity<ApiResponse<Seller>> deleteVariant(
            Authentication actor,
            @PathVariable UUID shopId,
            @PathVariable UUID id,
            @PathVariable UUID variantId) {

        Seller result = usecase.deleteVariant(currentUserId(actor), shopId, id, variantId);
        return ResponseEntity.ok(ApiResponse.success("Xóa biến thể thành công", result));
    }


}