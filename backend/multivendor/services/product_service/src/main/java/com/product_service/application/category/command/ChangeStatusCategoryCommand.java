package com.product_service.application.category.command;

import java.util.UUID;

public record ChangeStatusCategoryCommand(
        UUID id,
        String status
) {
}
