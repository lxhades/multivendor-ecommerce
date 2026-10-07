package com.catalog_service.infrastructure.web.handler;

import com.catalog_service.domain.exception.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleProductNotFound(ProductNotFoundException ex) {
        return Map.of(
                "success", false,
                "code", "PRODUCT_NOT_FOUND",
                "message", ex.getMessage(),
                "timestamp", Instant.now().toString()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleIllegalArgument(IllegalArgumentException ex) {
        return Map.of(
                "success", false,
                "code", "BAD_REQUEST",
                "message", ex.getMessage(),
                "timestamp", Instant.now().toString()
        );
    }
}
