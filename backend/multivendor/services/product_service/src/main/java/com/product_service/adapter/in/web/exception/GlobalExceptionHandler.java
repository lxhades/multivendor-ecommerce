package com.product_service.adapter.in.web.exception;

import com.product_service.adapter.in.web.Helper;
import com.product_service.adapter.in.web.dto.response.ErrorResponse;
import com.product_service.domain.exception.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
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
@Order(1) // Ưu tiên chạy trước
@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- NHÓM LỖI 404 URL ---
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ErrorResponse> handleRouteNotFound(Exception ex, HttpServletRequest request) {
        String message = "Đường dẫn không tồn tại: " + request.getRequestURI();
        return Helper.buildErrorResponse(HttpStatus.NOT_FOUND, message, request);
    }

    // --- NHÓM LỖI 400 (VALIDATION & PARAMS) ---
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String message = "Thiếu tham số yêu cầu: " + ex.getParameterName();
        return Helper.buildErrorResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(" | "));
        return Helper.buildErrorResponse(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ: " + message, request);
    }

    // --- NHÓM LỖI DOMAIN CHUNG ---
    @ExceptionHandler({DomainException.class, IllegalArgumentException.class, NullPointerException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Yêu cầu không hợp lệ. Vui lòng kiểm tra lại dữ liệu.";
        return Helper.buildErrorResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    // --- NHÓM LỖI 500 (FALLBACK) ---
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        log.error("Lỗi RuntimeException tại đường dẫn {}: ", request.getRequestURI(), ex);
        String message = ex.getMessage() != null ? ex.getMessage() : "Đã xảy ra lỗi trong quá trình xử lý.";
        return Helper.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception ex, HttpServletRequest request) {
        log.error("Lỗi không xác định tại đường dẫn {}: ", request.getRequestURI(), ex);
        return Helper.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.", request);
    }

}