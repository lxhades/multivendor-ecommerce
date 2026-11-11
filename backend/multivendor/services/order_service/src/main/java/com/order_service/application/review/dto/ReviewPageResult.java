package com.order_service.application.review.dto;

import java.util.List;

public record ReviewPageResult(List<ReviewResult> items, int page, int size, long totalElements,
                               int totalPages, double averageRating) {}
