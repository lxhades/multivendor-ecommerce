package com.product_service.adapter.in.web.shop;

import com.product_service.application.shop.exception.ShopNotFoundException;
import com.product_service.application.shop.exception.ShopSlugAlreadyExistsException;
import com.product_service.domain.exception.DomainException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(assignableTypes = ShopController.class)
public class ShopExceptionHandler {
    @ExceptionHandler(ShopNotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(ShopNotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler({ShopSlugAlreadyExistsException.class, DataIntegrityViolationException.class})
    public ResponseEntity<Map<String, String>> conflict(RuntimeException ex) {
        return ResponseEntity.status(409)
                .body(Map.of("error", "Shop slug or database constraint conflict"));
    }

    @ExceptionHandler({DomainException.class, IllegalArgumentException.class, NullPointerException.class})
    public ResponseEntity<Map<String, String>> invalid(RuntimeException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage() == null ? "Invalid request" : ex.getMessage()));
    }
}
