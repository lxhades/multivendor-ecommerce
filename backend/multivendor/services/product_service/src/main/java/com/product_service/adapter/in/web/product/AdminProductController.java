package com.product_service.adapter.in.web.product;

import com.product_service.adapter.in.web.dto.response.ApiResponse;
import com.product_service.application.product.dto.ProductDtos.DecisionInput;
import com.product_service.application.product.dto.ProductDtos.Seller;
import com.product_service.application.product.usecase.AdminProductUseCase;
import com.product_service.domain.model.enumtype.ApprovalStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/products")
// @PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final AdminProductUseCase usecase;

    public AdminProductController(AdminProductUseCase usecase) {
        this.usecase = usecase;
    }

    private UUID currentUserId(Jwt jwt) {
        String sub = jwt.getSubject();
        if (sub == null || sub.isBlank()) {
            throw new IllegalStateException("JWT không có subject");
        }
        return UUID.fromString(sub);
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<Seller>>> pending() {
        List<Seller> result = usecase.pending();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Seller>> details(@PathVariable UUID id) {
        Seller result = usecase.details(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Seller>> approve(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @RequestBody(required = false) DecisionInput in) {

        Seller result = usecase.decide(
                currentUserId(jwt),
                id,
                ApprovalStatus.APPROVED,
                in == null ? null : in.reason()
        );

        return ResponseEntity.ok(ApiResponse.success("Duyệt sản phẩm thành công", result));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Seller>> reject(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @RequestBody DecisionInput in) {

        Seller result = usecase.decide(
                currentUserId(jwt),
                id,
                ApprovalStatus.REJECTED,
                in.reason()
        );

        return ResponseEntity.ok(ApiResponse.success("Từ chối sản phẩm thành công", result));
    }
}