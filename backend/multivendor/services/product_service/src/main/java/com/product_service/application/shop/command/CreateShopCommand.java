package com.product_service.application.shop.command;

import java.util.UUID;

public record CreateShopCommand(
        String name,
        String slug,
        String logoUrl,
        String bannerUrl,
        String description,
        String address,
        String bankDetails) {
}
