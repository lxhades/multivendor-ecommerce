package com.order_service.adapter.in.web.exception;

import com.common.dto.response.ErrorResponse;
import com.order_service.adapter.in.web.controller.AdminOrderQueryController;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;

@RestControllerAdvice(assignableTypes = AdminOrderQueryController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AdminOrderQueryExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> status(ResponseStatusException e, HttpServletRequest request) {
        return error(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason(), request);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> invalid(IllegalArgumentException e, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }
    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> parameter(Exception e, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST,
                "Kiểm tra UUID, from/to (YYYY-MM-DD), trạng thái, dateBasis, direction và phân trang", request);
    }
    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ErrorResponse(Instant.now(), status.value(),
                status.getReasonPhrase(), message, null, request.getRequestURI()));
    }
}
