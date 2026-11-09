package com.product_service.adapter.in.web.shop;

import com.product_service.adapter.in.web.Helper;
import com.common.dto.response.ApiResponse;
import com.product_service.application.shop.result.ShopResult;
import com.product_service.application.shop.usecase.ChangeShopStatusUseCase;
import com.product_service.application.shop.usecase.CloseShopUseCase;
import com.product_service.application.shop.usecase.GetShopUseCase;
import com.product_service.application.shop.usecase.ListShopsUseCase;
import com.product_service.domain.model.enumtype.ShopStatus;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController

@RequestMapping("/api/shops/admin")
@AllArgsConstructor
public class AdminShopController {

    private final GetShopUseCase getShopUseCase;
    private final ListShopsUseCase listShopsUseCase;
    private final CloseShopUseCase closeShopUseCase;
    private final ChangeShopStatusUseCase changeShopStatusUseCase;


    // Admin xem danh sách toàn bộ shop
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ShopResult>>> list(
            @RequestParam(required = false) UUID sellerId,
            Pageable pageable) {

        Page<ShopResult> shopResultPage = listShopsUseCase.list(pageable);
        return ResponseEntity.ok(ApiResponse.success(shopResultPage));
    }

    // Admin xem chi tiết shop
    @GetMapping("/{shopId}")
    public ResponseEntity<ApiResponse<ShopResult>> findById(@PathVariable UUID shopId) {
        ShopResult result = getShopUseCase.get(shopId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // Admin đổi trạng thái shop (Khóa/Mở khóa)
    @PatchMapping("/{shopId}/status")
    public ResponseEntity<ApiResponse<ShopResult>> changeStatus(
            @PathVariable UUID shopId,
            Authentication actor,
            @RequestParam ShopStatus shopStatus) {

        UUID actorAdminId = Helper.currentUserId(actor);
        ShopResult updatedShopResult = changeShopStatusUseCase.change(shopId, actorAdminId, shopStatus,true);

        return ResponseEntity.ok(
                ApiResponse.success("Admin đã cập nhật trạng thái cửa hàng", updatedShopResult)
        );
    }


    @DeleteMapping("/{shopId}")
    public ResponseEntity<ApiResponse<Void>> close(
            @PathVariable UUID shopId,
            Authentication actor) {

        UUID actorAdminId = Helper.currentUserId(actor);
        closeShopUseCase.close(shopId, actorAdminId);

        return ResponseEntity.ok(ApiResponse.success("Admin đã đóng cửa hàng thành công", null));
    }

}