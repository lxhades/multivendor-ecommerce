package com.product_service.domain.model.entity;

import com.product_service.domain.model.enumtype.ApprovalStatus;

import java.time.Instant;
import java.util.UUID;

public record ProductApprovalHistory(
        UUID id,
        UUID actorId,
        ApprovalStatus decision,
        String reason,
        Instant createdAt) {
}
