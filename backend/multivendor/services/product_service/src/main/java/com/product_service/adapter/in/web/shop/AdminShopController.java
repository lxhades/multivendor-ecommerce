package com.product_service.adapter.in.web.shop;

import com.product_service.adapter.in.web.response.response.ApiResponse;
import com.product_service.application.shop.result.ShopResult;
import com.product_service.application.shop.usecase.ChangeShopStatusUseCase;
import com.product_service.application.shop.usecase.CloseShopUseCase;
import com.product_service.application.shop.usecase.GetShopUseCase;
import com.product_service.application.shop.usecase.ListShopsUseCase;
import com.product_service.domain.model.enumtype.ShopStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/shops")

public class AdminShopController {

    private final GetShopUseCase getShopUseCase;
    private final ListShopsUseCase listShopsUseCase;
    private final CloseShopUseCase closeShopUseCase;
    private final ChangeShopStatusUseCase changeShopStatusUseCase;

    public AdminShopController(
            GetShopUseCase getShopUseCase,
            ListShopsUseCase listShopsUseCase,
            CloseShopUseCase closeShopUseCase,
            ChangeShopStatusUseCase changeShopStatusUseCase) {
        this.getShopUseCase = getShopUseCase;
        this.listShopsUseCase = listShopsUseCase;
        this.closeShopUseCase = closeShopUseCase;
        this.changeShopStatusUseCase = changeShopStatusUseCase;
    }

    private UUID currentUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    // Admin xem danh sách toàn bộ shop
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ShopResult>>> list(
            @RequestParam(required = false) UUID sellerId,
            Pageable pageable) {

        Page<ShopResult> shopResultPage = listShopsUseCase.list(pageable);
        return ResponseEntity.ok(ApiResponse.success(shopResultPage));
    }

    // Admin xem chi tiết bất kỳ shop nào
    @GetMapping("/{shopId}")
    public ResponseEntity<ApiResponse<ShopResult>> findById(@PathVariable UUID shopId) {
        ShopResult result = getShopUseCase.get(shopId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // Admin đổi trạng thái shop (Khóa/Mở khóa)
    @PatchMapping("/{shopId}/status")
    public ResponseEntity<ApiResponse<ShopResult>> changeStatus(
            @PathVariable UUID shopId,
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam ShopStatus shopStatus) {

        UUID actorAdminId = currentUserId(jwt);
        ShopResult updatedShopResult = changeShopStatusUseCase.change(shopId, actorAdminId, shopStatus);

        return ResponseEntity.ok(ApiResponse.success("Admin đã cập nhật trạng thái cửa hàng", updatedShopResult));
    }


    @DeleteMapping("/{shopId}")
    public ResponseEntity<ApiResponse<Void>> close(
            @PathVariable UUID shopId,
            @AuthenticationPrincipal Jwt jwt) {

        UUID actorAdminId = currentUserId(jwt);
        closeShopUseCase.close(shopId, actorAdminId);

        return ResponseEntity.ok(ApiResponse.success("Admin đã đóng cửa hàng thành công", null));
    }

}