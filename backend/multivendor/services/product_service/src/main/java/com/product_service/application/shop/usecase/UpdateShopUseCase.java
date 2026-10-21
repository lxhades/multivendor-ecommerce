package com.product_service.application.shop.usecase;

import com.product_service.application.shop.command.UpdateShopCommand;
import com.product_service.application.shop.exception.ShopNotFoundException;
import com.product_service.application.shop.exception.ShopSlugAlreadyExistsException;
import com.product_service.application.shop.mapper.ShopMapper;
import com.product_service.application.shop.result.ShopResult;
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

        if (!actorSellerId.equals(shop.getSellerId().value()))
            throw new RuntimeException("Không có quyền truy cập vào shop của người khác");
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