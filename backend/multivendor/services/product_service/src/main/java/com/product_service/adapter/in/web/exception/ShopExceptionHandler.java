package com.product_service.adapter.in.web.exception;

import com.product_service.adapter.in.web.Helper;
import com.product_service.adapter.in.web.dto.response.ErrorResponse;
import com.product_service.application.shop.exception.ShopNotFoundException;
import com.product_service.application.shop.exception.ShopSlugAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ShopExceptionHandler {

    @ExceptionHandler(ShopNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ShopNotFoundException ex, HttpServletRequest request) {
        return Helper.buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ShopSlugAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ShopSlugAlreadyExistsException ex, HttpServletRequest request) {
        return Helper.buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }


}