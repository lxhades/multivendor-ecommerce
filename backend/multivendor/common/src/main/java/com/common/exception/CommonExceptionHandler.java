package com.common.exception;

import com.common.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class CommonExceptionHandler {

    //  Business / Domain 

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(
            DomainException ex, HttpServletRequest request) {
        log.warn("Lỗi nghiệp vụ tại {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(
                ex.getErrorCode().getHttpStatus(),
                ex.getErrorCode().getMessage(),
                null,
                request
        );
    }

    //  Request validation 

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        log.warn("Kiểm tra dữ liệu thất bại tại {}: {}", request.getRequestURI(), details);
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Dữ liệu đầu vào không hợp lệ",
                details,
                request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("JSON không đúng định dạng tại {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Dữ liệu gửi lên không đúng định dạng JSON",
                null,
                request
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.warn("Thiếu tham số '{}' tại {}", ex.getParameterName(), request.getRequestURI());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Thiếu tham số yêu cầu: " + ex.getParameterName(),
                null,
                request
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Tham số không hợp lệ tại {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null, request);
    }

    //  Spring / Framework 

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
            ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String message = ex.getReason() != null ? ex.getReason() : "Request failed";

        log.warn("Lỗi ResponseStatusException tại {}: {} - {}", request.getRequestURI(), status, message);
        return buildErrorResponse(status, message, null, request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex, HttpServletRequest request) {
        log.warn("Không tìm thấy API endpoint: {}", request.getRequestURI());
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Đường dẫn hoặc tài nguyên không tồn tại",
                null,
                request
        );
    }

    //  Fallback 

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {
        log.error("RuntimeException chưa được xử lý tại {}: ", request.getRequestURI(), ex);
        String message = ex.getMessage() != null
                ? ex.getMessage()
                : "Đã xảy ra lỗi trong quá trình xử lý.";
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, null, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(
            Exception ex, HttpServletRequest request) {
        log.error("Ngoại lệ chưa được xử lý tại {}: ", request.getRequestURI(), ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Đã xảy ra lỗi từ phía hệ thống. Vui lòng thử lại sau!",
                null,
                request
        );
    }

    

    private static ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status, String message, List<String> details, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.name(),
                message,
                details,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }
}
