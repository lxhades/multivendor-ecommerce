package com.catalog_service.adapters.in.web.exception;

import com.catalog_service.domain.exception.CategoryNotFoundException;
import com.catalog_service.domain.exception.CategorySlugAlreadyExistsException;
import com.catalog_service.domain.exception.ParentCategoryNotFoundException;
import com.catalog_service.domain.exception.ProductNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ====== Product ======
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(
            ProductNotFoundException ex, HttpServletRequest request) {
        String message = isBlank(ex.getMessage()) ? "Không tìm thấy sản phẩm" : ex.getMessage();
        return build(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", message, request);
    }

    // ====== Category ======
    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNotFound(
            CategoryNotFoundException ex, HttpServletRequest request) {
        String message = isBlank(ex.getMessage()) ? "Không tìm thấy danh mục" : ex.getMessage();
        return build(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", message, request);
    }

    @ExceptionHandler(ParentCategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleParentCategoryNotFound(
            ParentCategoryNotFoundException ex, HttpServletRequest request) {
        String message = isBlank(ex.getMessage()) ? "Không tìm thấy danh mục cha" : ex.getMessage();
        return build(HttpStatus.NOT_FOUND, "PARENT_CATEGORY_NOT_FOUND", message, request);
    }

    @ExceptionHandler(CategorySlugAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleCategorySlugAlreadyExists(
            CategorySlugAlreadyExistsException ex, HttpServletRequest request) {
        String message = isBlank(ex.getMessage()) ? "Slug của danh mục đã tồn tại" : ex.getMessage();
        return build(HttpStatus.CONFLICT, "CATEGORY_SLUG_ALREADY_EXISTS", message, request);
    }

    // ====== Validation / Argument ======
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        String message = isBlank(ex.getMessage()) ? "Dữ liệu nhập vào không hợp lệ" : ex.getMessage();
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message, request);
    }

    // ====== Helper ======
    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String code, String message, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .code(code)
                .message(message)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(error);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}