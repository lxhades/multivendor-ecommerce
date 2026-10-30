package com.product_service.adapter.in.web.exception;

import com.common.dto.response.ErrorResponse;
import com.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@Order(1)
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class,
            NullPointerException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        log.warn("Invalid request at {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorCode errorCode = ErrorCode.INVALID_DOMAIN_RULE;
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(new ErrorResponse(
                        Instant.now(),
                        errorCode.getHttpStatus().value(),
                        errorCode.name(),
                        errorCode.getMessage(),
                        null,
                        request.getRequestURI()
                ));
    }
}