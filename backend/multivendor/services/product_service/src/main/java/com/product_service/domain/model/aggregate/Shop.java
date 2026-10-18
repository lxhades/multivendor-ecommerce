package com.product_service.domain.model.aggregate;

import java.time.Instant;
import com.product_service.domain.model.vo.*;
import com.product_service.domain.model.enumtype.ShopStatus;
import static com.product_service.domain.shared.Checks.*;

public final class Shop {

    private final ShopId id;
    private final SellerId sellerId;
    private String name;
    private Slug slug;
    private String logoUrl, bannerUrl, description, address, bankDetails;
    private ShopStatus status;
    private RatingSummary rating = RatingSummary.empty();
    private long productCount;
    private final Instant createdAt;
    private Instant updatedAt;


    public Shop(ShopId id, SellerId sellerId, String name, Slug slug, Instant now) {
        this.id = required(id, "id");
        this.sellerId = required(sellerId, "sellerId");
        this.name = text(name, "shop name", 200);
        this.slug = required(slug, "slug");
        createdAt = updatedAt = required(now, "now");
        status = ShopStatus.ACTIVE;
    }


    public void assertOwner(SellerId actor) {

        rule(sellerId.equals(actor), "Seller does not own shop");
    }


    public void assertCanSell() {

        rule(status == ShopStatus.ACTIVE, "Shop is not active");
    }


    public void updateProfile(String name, Slug slug, String logoUrl, String bannerUrl,
                              String description, String address, String bankDetails, Instant now) {
        String checked = text(name, "shop name", 200);
        required(slug, "slug");
        required(now, "now");
        rule(bankDetails == null || bankDetails.length() <= 200, "Bank details too long");

        this.name = checked;
        this.slug = slug;
        this.logoUrl = logoUrl;
        this.bannerUrl = bannerUrl;
        this.description = description;
        this.address = address;
        this.bankDetails = bankDetails;
        updatedAt = now;
    }


    public void changeStatus(ShopStatus status, Instant now) {
        required(status, "status");
        required(now, "now");
        this.status = status;
        updatedAt = now;
    }


    public void replaceStatistics(RatingSummary rating, long productCount, Instant now) {
        required(rating, "rating");
        required(now, "now");
        rule(productCount >= 0, "Negative product count");

        this.rating = rating;
        this.productCount = productCount;
        updatedAt = now;
    }

    public ShopId getId() {
        return id;
    }

    public SellerId getSellerId() {
        return sellerId;
    }

    public String getName() {
        return name;
    }

    public Slug getSlug() {
        return slug;
    }

    public ShopStatus getStatus() {
        return status;
    }

    public RatingSummary getRating() {
        return rating;
    }

    public long getProductCount() {
        return productCount;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public String getBankDetails() {
        return bankDetails;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }


    public static Shop restore(ShopId id, SellerId sellerId, String name, Slug slug,
                               String logoUrl, String bannerUrl, String description, String address,
                               String bankDetails, ShopStatus status, RatingSummary rating,
                               long productCount, Instant createdAt, Instant updatedAt) {
        Shop shop = new Shop(id, sellerId, name, slug, createdAt);
        shop.updateProfile(name, slug, logoUrl, bannerUrl, description, address, bankDetails, updatedAt);
        shop.status = required(status, "status");
        shop.rating = required(rating, "rating");
        rule(productCount >= 0, "Negative product count");
        shop.productCount = productCount;
        return shop;
    }
}
