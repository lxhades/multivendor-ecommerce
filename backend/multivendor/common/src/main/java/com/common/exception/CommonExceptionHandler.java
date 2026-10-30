package com.common.exception;

import com.common.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AutoConfiguration
@RestControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(
            DomainException ex, HttpServletRequest request) {
        log.warn("Business error: {}", ex.getMessage());
            ErrorResponse response = new ErrorResponse(
                    Instant.now(),
                    ex.getErrorCode().getHttpStatus().value(),
                    ex.getErrorCode().name(),
                    ex.getErrorCode().getMessage(),
                    null,
                    request.getRequestURI()
            );
            return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        String errorMessage = details.isEmpty() ? "Dữ liệu nhập vào không hợp lệ" : "Dữ liệu đầu vào không hợp lệ";

        log.warn("Validation failed at {}: {}", request.getRequestURI(), details);

        return buildErrorResponse(HttpStatus.BAD_REQUEST, errorMessage, details, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed JSON at {}", request.getRequestURI());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Dữ liệu gửi lên không đúng định dạng JSON", null, request);
    }
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String message = "Thiếu tham số yêu cầu: " + ex.getParameterName();
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Đã xảy ra lỗi từ phía hệ thống. Vui lòng thử lại sau!",
                null,
                request
        );
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null, request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        log.warn("API endpoint not found: {}", request.getRequestURI());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Đường dẫn hoặc tài nguyên không tồn tại",null, request);
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        log.error("Lỗi RuntimeException tại đường dẫn {}: ", request.getRequestURI(), ex);
        String message = ex.getMessage() != null ? ex.getMessage() : "Đã xảy ra lỗi trong quá trình xử lý.";
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Đường dẫn hoặc tài nguyên không tồn tại",null, request);

    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception at {}: ", request.getRequestURI(), ex);

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Đã xảy ra lỗi từ phía hệ thống. Vui lòng thử lại sau!",
                null,
                request
        );
    }



    public static ResponseEntity<ErrorResponse> buildErrorResponse(
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