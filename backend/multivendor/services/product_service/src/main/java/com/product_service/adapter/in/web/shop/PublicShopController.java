package com.product_service.adapter.in.web.shop;

import com.common.dto.response.ApiResponse;
import com.product_service.application.shop.result.ShopResult;
import com.product_service.application.shop.usecase.GetShopUseCase;
import com.product_service.application.shop.usecase.ListShopsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/shops")
@RequiredArgsConstructor
public class PublicShopController {

    private final GetShopUseCase getShopUseCase;
    private final ListShopsUseCase listShopsUseCase;

    // Xem danh sách cửa hàng (Phân trang)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ShopResult>>> list(Pageable pageable) {
        Page<ShopResult> shopResultPage = listShopsUseCase.list(pageable);
        return ResponseEntity.ok(ApiResponse.success(shopResultPage));
    }

    // Xem chi tiết cửa hàng theo ID
    @GetMapping("/{shopId}")
    public ResponseEntity<ApiResponse<ShopResult>> findById(@PathVariable UUID shopId) {
        ShopResult result = getShopUseCase.get(shopId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}