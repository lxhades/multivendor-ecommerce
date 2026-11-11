package com.order_service.application.review.usecase;

import com.order_service.adapter.out.persistence.jpa.ProductReviewRepository;
import com.order_service.application.review.dto.ReviewPageResult;
import com.order_service.application.review.dto.ReviewResult;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class ListProductReviewsUseCase {
    private final ProductReviewRepository reviews;

    public ListProductReviewsUseCase(ProductReviewRepository reviews) { this.reviews = reviews; }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public ReviewPageResult execute(UUID productId, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page phải >= 0, size phải từ 1 đến 100");
        }
        var result = reviews.findByProductId(productId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id")));
        return new ReviewPageResult(result.getContent().stream().map(ReviewResult::from).toList(),
                page, size, result.getTotalElements(), result.getTotalPages(), reviews.averageRating(productId));
    }
}
