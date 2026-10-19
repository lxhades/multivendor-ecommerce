package com.product_service.application.shop.usecase;
import java.time.Clock;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.product_service.application.shop.command.CreateShopCommand;
import com.product_service.application.shop.exception.ShopSlugAlreadyExistsException;
import com.product_service.application.shop.mapper.ShopMapper;
import com.product_service.application.shop.result.ShopResult;
import com.product_service.domain.model.aggregate.Shop;
import com.product_service.domain.model.vo.*;
import com.product_service.domain.repository.ShopRepository;
@Service
public class CreateShopUseCase {
    private final ShopRepository repository;
    private final Clock clock;
    public CreateShopUseCase(ShopRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;

    }

    @Transactional
    public ShopResult create(CreateShopCommand command) {
        Objects.requireNonNull(command, "command");
        Slug slug = new Slug(command.slug());
        if (repository.existsBySlug(slug, null)) throw new ShopSlugAlreadyExistsException(slug.value());

        Shop shop = new Shop(ShopId.random(),
                new SellerId(command.sellerId()), command.name(), slug, clock.instant());

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
