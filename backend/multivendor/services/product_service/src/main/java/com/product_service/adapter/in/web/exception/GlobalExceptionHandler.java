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

    // =========================================================================
    // NHÓM LỖI 404: KHÔNG TÌM THẤY
    // =========================================================================

    /**
     * Xử lý lỗi 404 khi Client gọi vào một API (URL) không tồn tại.
     * Bắt các exception mặc định của Spring MVC.
     */
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ErrorResponse> handleRouteNotFound(Exception ex, HttpServletRequest request) {
        String message = "Đường dẫn không tồn tại: " + request.getRequestURI();
        return buildErrorResponse(HttpStatus.NOT_FOUND, message, request);
    }

    /**
     * Xử lý lỗi 404 khi Client gọi đúng API nhưng dữ liệu (vd: Danh mục) không tồn tại trong DB.
     */
    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(CategoryNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // =========================================================================
    // NHÓM LỖI 409: XUNG ĐỘT DỮ LIỆU (CONFLICT)
    // =========================================================================

    /**
     * Xử lý lỗi 409 khi Client cố tạo dữ liệu đã tồn tại.
     */
    @ExceptionHandler(CategorySlugAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleConflict(CategorySlugAlreadyExistsException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // =========================================================================
    // NHÓM LỖI 400: DỮ LIỆU GỬI LÊN KHÔNG HỢP LỆ (BAD REQUEST)
    // =========================================================================

    /**
     * Xử lý lỗi 400 khi dữ liệu vi phạm các quy tắc nghiệp vụ (Domain Rule).
     * Hoặc khi có lỗi logic cơ bản (Ném IllegalArgumentException, NullPointerException).
     */
    @ExceptionHandler({DomainException.class, IllegalArgumentException.class, NullPointerException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        String message = ex.getMessage()
                != null ? ex.getMessage() : "Yêu cầu không hợp lệ. Vui lòng kiểm tra lại dữ liệu.";
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    /**
     * Xử lý lỗi 400 khi Client thiếu các tham số bắt buộc trên URL (Query Param).
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String message = "Thiếu tham số yêu cầu: " + ex.getParameterName();
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    /**
     * Xử lý lỗi 400 khi dữ liệu gửi lên (Request Body) không vượt qua được bước Validate (@Valid).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(" | "));
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ: " + message, request);
    }

    // =========================================================================
    // NHÓM LỖI 500: LỖI SERVER (INTERNAL SERVER ERROR)
    // =========================================================================

    /**
     * Xử lý lỗi 500 cho các lỗi Runtime chưa được bắt ở trên.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        log.error("Lỗi RuntimeException tại đường dẫn {}: ", request.getRequestURI(), ex);

        String message = ex.getMessage() != null ? ex.getMessage() : "Đã xảy ra lỗi trong quá trình xử lý.";
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, request);
    }

    // Bắt tất cả lỗi còn lại
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception ex, HttpServletRequest request) {
        log.error("Lỗi không xác định tại đường dẫn {}: ", request.getRequestURI(), ex);

        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau hoặc liên hệ quản trị viên.", request);
    }

    // =========================================================================
    // HÀM HỖ TRỢ (HELPER METHOD)
    // =========================================================================


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