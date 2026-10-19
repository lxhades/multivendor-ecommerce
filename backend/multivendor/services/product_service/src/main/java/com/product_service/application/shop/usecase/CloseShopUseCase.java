package com.product_service.application.shop.usecase;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.product_service.application.shop.exception.ShopNotFoundException;
import com.product_service.domain.model.enumtype.ShopStatus;
import com.product_service.domain.model.vo.*;
import com.product_service.domain.repository.ShopRepository;
@Service
public class CloseShopUseCase {
    private final ShopRepository repository;
    private final Clock clock;
    public CloseShopUseCase(ShopRepository repository, Clock clock) {
        this.repository = repository; this.clock = clock;
    }

    @Transactional
    public void close(UUID id, UUID actorSellerId) {
        var shop = repository.findById(new ShopId(id)).orElseThrow(() -> new ShopNotFoundException(id));
        shop.assertOwner(new SellerId(actorSellerId));
        shop.changeStatus(ShopStatus.CLOSED, clock.instant());
        repository.save(shop);
    }
}
