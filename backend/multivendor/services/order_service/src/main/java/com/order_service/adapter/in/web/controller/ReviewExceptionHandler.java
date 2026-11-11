package com.order_service.adapter.in.web.controller;

import com.common.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

/** Scoped to Review so status/message semantics do not depend on other services' exception advice. */
@RestControllerAdvice(assignableTypes = ReviewController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ReviewExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> status(ResponseStatusException e, HttpServletRequest request) {
        return error(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason(), request);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> invalid(Exception e, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Dữ liệu đánh giá không hợp lệ: kiểm tra ID, rating (1–5), comment (1–2000 ký tự) và mediaUrls", request);
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ErrorResponse(Instant.now(), status.value(),
                status.getReasonPhrase(), message, null, request.getRequestURI()));
    }
}
