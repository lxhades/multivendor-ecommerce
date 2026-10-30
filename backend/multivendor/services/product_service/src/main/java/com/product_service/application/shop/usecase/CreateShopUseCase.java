package com.product_service.application.shop.usecase;

import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.product_service.application.shop.command.CreateShopCommand;
import com.product_service.application.shop.mapper.ShopMapper;
import com.product_service.application.shop.result.ShopResult;
import com.product_service.domain.model.aggregate.Shop;
import com.product_service.domain.model.vo.SellerId;
import com.product_service.domain.model.vo.ShopId;
import com.product_service.domain.model.vo.Slug;
import com.product_service.domain.repository.ShopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

@Service
public class CreateShopUseCase {
    private final ShopRepository repository;
    private final Clock clock;

    public CreateShopUseCase(ShopRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;

    }

    @Transactional
    public ShopResult create(UUID sellerId, CreateShopCommand command) {
        if(repository.existsShopBySellerId(new SellerId(sellerId))){
            throw new RuntimeException(("Bạn đã có cửa hàng, không thể tạo thêm!"));
        }

        Objects.requireNonNull(command, "command");
        Slug slug = new Slug(command.slug());
        if (repository.existsBySlug(slug, null)) throw new DomainException(ErrorCode.SHOP_SLUG_ALREADY_EXISTS);

        Shop shop = new Shop(
                ShopId.random(),
                new SellerId(sellerId),
                command.name(),
                slug,
                clock.instant());

        shop.updateProfile(command.name(), slug,
                command.logoUrl(),
                command.bannerUrl(),
                command.description(),
                command.address(),
                command.bankDetails(),
                clock.instant());

        return ShopMapper.toResult(repository.save(shop));
    }
}
