package com.product_service.adapter.in.web.product;

import com.product_service.adapter.in.web.dto.response.ApiResponse;
import com.product_service.application.product.dto.ProductDtos;
import com.product_service.application.product.dto.ProductDtos.Seller;
import com.product_service.application.product.usecase.GetSalesStatisticsUseCase;
import com.product_service.application.product.usecase.SellerProductUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/seller/shops/{shopId}/products")
@PreAuthorize("hasRole('SELLER')")
public class SellerProductController {

    private final SellerProductUseCase usecase;
    private final GetSalesStatisticsUseCase stats;

    public SellerProductController(SellerProductUseCase usecase, GetSalesStatisticsUseCase stats) {
        this.usecase = usecase;
        this.stats = stats;
    }

    private UUID currentUserId(Jwt jwt) {
        String sub = jwt.getSubject();
        if (sub == null || sub.isBlank()) {
            throw new IllegalStateException("JWT không có subject");
        }
        return UUID.fromString(sub);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Seller>> create(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID shopId,
            @RequestBody ProductDtos.ProductInput in) {

        Seller result = usecase.create(currentUserId(jwt), shopId, in);
        return ResponseEntity.ok(ApiResponse.success("Tạo sản phẩm thành công", result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Seller>> update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID shopId,
            @PathVariable UUID id,
            @RequestBody ProductDtos.ProductInput in) {

        Seller result = usecase.update(currentUserId(jwt), shopId, id, in);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật sản phẩm thành công", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID shopId,
            @PathVariable UUID id) {

        usecase.delete(currentUserId(jwt), shopId, id);
        return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm thành công", null));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Seller>> status(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID shopId,
            @PathVariable UUID id,
            @RequestBody ProductDtos.StatusInput in) {

        Seller result = usecase.changeStatus(currentUserId(jwt), shopId, id, in.status());
        return ResponseEntity.ok(ApiResponse.success("Thay đổi trạng thái thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Seller>> details(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID shopId,
            @PathVariable UUID id) {

        Seller result = usecase.details(currentUserId(jwt), shopId, id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Seller>>> list(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID shopId) {

        List<Seller> result = usecase.list(currentUserId(jwt), shopId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{id}/variants")
    public ResponseEntity<ApiResponse<Seller>> addVariant(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID shopId,
            @PathVariable UUID id,
            @RequestBody ProductDtos.VariantInput in) {

        Seller result = usecase.addVariant(currentUserId(jwt), shopId, id, in);
        return ResponseEntity.ok(ApiResponse.success("Thêm biến thể thành công", result));
    }

    @PutMapping("/{id}/variants/{variantId}")
    public ResponseEntity<ApiResponse<Seller>> updateVariant(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID shopId,
            @PathVariable UUID id,
            @PathVariable UUID variantId,
            @RequestBody ProductDtos.VariantInput in) {

        Seller result = usecase.updateVariant(currentUserId(jwt), shopId, id, variantId, in);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật biến thể thành công", result));
    }

    @DeleteMapping("/{id}/variants/{variantId}")
    public ResponseEntity<ApiResponse<Seller>> deleteVariant(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID shopId,
            @PathVariable UUID id,
            @PathVariable UUID variantId) {

        Seller result = usecase.deleteVariant(currentUserId(jwt), shopId, id, variantId);
        return ResponseEntity.ok(ApiResponse.success("Xóa biến thể thành công", result));
    }

    @GetMapping("/sales")
    public ResponseEntity<ApiResponse<Object>> sales(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID shopId,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {

        Object result = stats.execute(currentUserId(jwt), shopId, productId, from, to);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}