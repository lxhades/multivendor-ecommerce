package com.product_service.domain.model.vo;

import java.util.UUID;

import static com.product_service.domain.shared.Checks.required;

public record ApprovalHistoryId(UUID value) {
    public ApprovalHistoryId {
        required(value, "ApprovalHistoryId");
    }

    public static ApprovalHistoryId random() {
        return new ApprovalHistoryId(UUID.randomUUID());
    }
}
