package com.product_service.adapter.in.web.exception;

import com.product_service.adapter.in.web.dto.response.ErrorResponse;
import com.product_service.application.category.exception.CategoryNotFoundException;
import com.product_service.application.category.exception.CategorySlugAlreadyExistsException;
import com.product_service.domain.exception.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ErrorResponse> handleRouteNotFound(Exception ex, HttpServletRequest request) {
        String message = "Đường dẫn không tồn tại: " + request.getRequestURI();
        return buildErrorResponse(HttpStatus.NOT_FOUND, message, request);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(CategoryNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(CategorySlugAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleConflict(CategorySlugAlreadyExistsException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler({DomainException.class, IllegalArgumentException.class, NullPointerException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Invalid request";
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(MissingServletRequestParameterException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(" | "));
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ: " + message, request);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        log.error("Runtime Exception at path: {}", request.getRequestURI(), ex);
        String message = ex.getMessage() != null ? ex.getMessage() : "No message available";
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception ex, HttpServletRequest request) {
        log.error("Unknown error occurred at path: {}", request.getRequestURI(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(errorResponse);
    }
}