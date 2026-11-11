package com.order_service.application.review.command;

import jakarta.validation.constraints.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

public record CreateReviewCommand(
        @NotNull UUID orderId,
        @NotNull UUID productId,
        @NotNull @Min(1) @Max(5) Integer rating,
        @NotBlank @Size(max = 2000) String comment,
        List<@NotBlank @Size(max = 2048) String> mediaUrls
) {
    public CreateReviewCommand {
        if (orderId == null || productId == null) throw new IllegalArgumentException("orderId và productId là bắt buộc");
        if (rating == null || rating < 1 || rating > 5) throw new IllegalArgumentException("Số sao phải từ 1 đến 5");
        if (comment == null || comment.isBlank() || comment.length() > 2000) {
            throw new IllegalArgumentException("Bình luận không được trống và tối đa 2000 ký tự");
        }
        comment = comment.strip();
        if (mediaUrls == null) mediaUrls = List.of();
        for (String url : mediaUrls) {
            if (url == null || url.isBlank() || url.length() > 2048) throw invalidUrl();
            try {
                URI uri = URI.create(url);
                if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                        || uri.getHost() == null || uri.getUserInfo() != null) throw invalidUrl();
            } catch (IllegalArgumentException e) {
                throw invalidUrl();
            }
        }
        mediaUrls = List.copyOf(mediaUrls);
    }

    private static IllegalArgumentException invalidUrl() {
        return new IllegalArgumentException("mediaUrls phải chứa URL HTTP/HTTPS hợp lệ, tối đa 2048 ký tự mỗi URL");
    }
}
