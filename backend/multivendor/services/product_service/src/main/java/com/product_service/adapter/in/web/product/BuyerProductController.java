package com.product_service.adapter.in.web.product;

import com.product_service.adapter.in.web.dto.response.ApiResponse;
import com.product_service.application.product.dto.ProductDtos.Buyer;
import com.product_service.application.product.usecase.BuyerProductUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class BuyerProductController {

    private final BuyerProductUseCase usecase;

    public BuyerProductController(BuyerProductUseCase usecase) {
        this.usecase = usecase;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Buyer>>> list(
            @RequestParam(required = false) UUID shopId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "false") boolean includeChildren) {

        List<Buyer> result = usecase.list(shopId, categoryId, includeChildren);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Buyer>> details(@PathVariable UUID id) {
        Buyer result = usecase.details(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}