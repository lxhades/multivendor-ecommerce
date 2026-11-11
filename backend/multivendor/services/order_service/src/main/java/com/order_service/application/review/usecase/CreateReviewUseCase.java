package com.order_service.application.review.usecase;

import com.order_service.adapter.out.persistence.entity.ProductReviewEntity;
import com.order_service.adapter.out.persistence.jpa.ProductReviewRepository;
import com.order_service.adapter.out.persistence.jpa.SpringOrderJpaRepository;
import com.order_service.application.review.command.CreateReviewCommand;
import com.order_service.application.review.dto.ReviewResult;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.util.Locale;
import java.util.UUID;

@Service
public class CreateReviewUseCase {
    public static final String PURCHASE_REQUIRED = "Bạn chưa mua sản phẩm này hoặc đơn hàng chưa hoàn thành";
    private final SpringOrderJpaRepository orders;
    private final ProductReviewRepository reviews;
    private final Clock clock;

    public CreateReviewUseCase(SpringOrderJpaRepository orders, ProductReviewRepository reviews, Clock clock) {
        this.orders = orders;
        this.reviews = reviews;
        this.clock = clock;
    }

    @Transactional
    public ReviewResult execute(UUID buyerId, String buyerName, CreateReviewCommand command) {
        // All purchase checks are done in this service's own orders/order_items tables.
        if (buyerId == null || !orders.hasDeliveredProduct(command.orderId(), buyerId, command.productId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, PURCHASE_REQUIRED);
        }
        if (reviews.existsByOrderIdAndProductId(command.orderId(), command.productId())) throw duplicate();

        ProductReviewEntity review = new ProductReviewEntity();
        review.id = UUID.randomUUID();
        review.orderId = command.orderId();
        review.productId = command.productId();
        review.buyerId = buyerId;
        review.buyerName = buyerName == null || buyerName.isBlank() ? "Người mua" : buyerName.strip();
        if (review.buyerName.length() > 255) review.buyerName = review.buyerName.substring(0, 255);
        review.rating = command.rating();
        review.comment = command.comment();
        review.mediaUrls = command.mediaUrls();
        review.createdAt = clock.instant();
        review.updatedAt = review.createdAt;
        try {
            return ReviewResult.from(reviews.saveAndFlush(review));
        } catch (DataIntegrityViolationException e) {
            // A pre-check alone cannot prevent two simultaneous requests from creating a review.
            for (Throwable cause = e; cause != null; cause = cause.getCause()) {
                if (cause instanceof ConstraintViolationException violation
                        && violation.getConstraintName() != null
                        && violation.getConstraintName().toLowerCase(Locale.ROOT).contains("uk_product_reviews_order_product")) {
                    throw duplicate();
                }
            }
            throw e;
        }
    }

    private ResponseStatusException duplicate() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Bạn đã đánh giá sản phẩm này trong đơn hàng này");
    }
}
