package com.product_service.adapter.in.web.shop;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.product_service.application.shop.exception.*;
import com.product_service.domain.exception.DomainException;
@RestControllerAdvice(assignableTypes = ShopController.class)
public class ShopExceptionHandler {
    @ExceptionHandler(ShopNotFoundException.class)
    public ResponseEntity<Map<String,String>> notFound(ShopNotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler({ShopSlugAlreadyExistsException.class, DataIntegrityViolationException.class})
    public ResponseEntity<Map<String,String>> conflict(RuntimeException ex) {
        return ResponseEntity.status(409)
                .body(Map.of("error", "Shop slug or database constraint conflict"));
    }

    @ExceptionHandler({DomainException.class, IllegalArgumentException.class, NullPointerException.class})
    public ResponseEntity<Map<String,String>> invalid(RuntimeException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage() == null ? "Invalid request" : ex.getMessage()));
    }
}
