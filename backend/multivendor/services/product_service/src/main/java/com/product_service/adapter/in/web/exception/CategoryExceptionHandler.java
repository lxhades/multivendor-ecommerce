package com.product_service.adapter.in.web.exception;

import com.product_service.adapter.in.web.Helper;
import com.product_service.adapter.in.web.dto.response.ErrorResponse;
import com.product_service.application.category.exception.CategoryNotFoundException;
import com.product_service.application.category.exception.CategorySlugAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class CategoryExceptionHandler {

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(CategoryNotFoundException ex, HttpServletRequest request) {
        return Helper.buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(CategorySlugAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleConflict(CategorySlugAlreadyExistsException ex, HttpServletRequest request) {
        return Helper.buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }


}