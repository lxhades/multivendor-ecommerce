package com.product_service.adapter.in.web.product;

import com.common.dto.response.ApiResponse;
import com.common.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import com.product_service.application.product.dto.BuyerSearch;
import com.product_service.application.product.usecase.BuyerSearchUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/products/search")
public class BuyerSearchController {
    private final BuyerSearchUseCase search;

    public BuyerSearchController(BuyerSearchUseCase search) {
        this.search = search;
    }

    @GetMapping
    public ApiResponse<BuyerSearch.Page> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID shopId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "false") boolean includeChildren,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        BuyerSearch.Sort order = sort == null ? null : BuyerSearch.Sort.valueOf(sort.toUpperCase(Locale.ROOT));
        return ApiResponse.success(search.execute(new BuyerSearch.Criteria(q, shopId, categoryId,
                includeChildren, minPrice, maxPrice, order, page, size)));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse invalidParameter(MethodArgumentTypeMismatchException error, HttpServletRequest request) {
        return new ErrorResponse(Instant.now(), 400, "BAD_REQUEST",
                "Invalid parameter: " + error.getName(), null, request.getRequestURI());
    }
}
