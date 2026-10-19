package com.product_service.application.shop.usecase;

import java.time.Clock;
import java.util.UUID;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.product_service.application.shop.command.UpdateShopCommand;
import com.product_service.application.shop.exception.*;
import com.product_service.application.shop.mapper.ShopMapper;
import com.product_service.application.shop.result.ShopResult;
import com.product_service.domain.model.vo.*;
import com.product_service.domain.repository.ShopRepository;

@Service
public class UpdateShopUseCase {

    private final ShopRepository repository;
    private final Clock clock;

    public UpdateShopUseCase(ShopRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public ShopResult update(UUID id, UUID actorSellerId, UpdateShopCommand command) {
        Objects.requireNonNull(command, "command");

        var shop = repository.findById(new ShopId(id))
                .orElseThrow(() -> new ShopNotFoundException(id));

        shop.assertOwner(new SellerId(actorSellerId));

        Slug slug = new Slug(command.slug());

        if (repository.existsBySlug(slug, shop.getId())) {
            throw new ShopSlugAlreadyExistsException(slug.value());
        }

        shop.updateProfile(
                command.name(),
                slug,
                command.logoUrl(),
                command.bannerUrl(),
                command.description(),
                command.address(),
                command.bankDetails(),
                clock.instant()
        );

        return ShopMapper.toResult(repository.save(shop));
    }
}