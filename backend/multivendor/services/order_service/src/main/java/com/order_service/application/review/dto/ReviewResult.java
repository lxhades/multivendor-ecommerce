package com.order_service.application.review.dto;

import com.order_service.adapter.out.persistence.entity.ProductReviewEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Public representation: does not expose the buyer UUID or the proof-of-purchase order ID. */
public record ReviewResult(UUID id, UUID productId, String buyerName, int rating, String comment,
                           List<String> mediaUrls, Instant createdAt, Instant updatedAt) {
    public static ReviewResult from(ProductReviewEntity review) {
        return new ReviewResult(review.id, review.productId, review.buyerName, review.rating, review.comment,
                List.copyOf(review.mediaUrls), review.createdAt, review.updatedAt);
    }
}
