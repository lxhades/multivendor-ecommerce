package com.product_service.application.shop.command;

public record UpdateShopCommand(
        String name,
        String slug,
        String logoUrl,
        String bannerUrl,
        String description,
        String address,
        String bankDetails) {
}
