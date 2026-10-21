package com.product_service.application.shop.usecase;

import com.product_service.application.shop.exception.ShopNotFoundException;
import com.product_service.application.shop.mapper.ShopMapper;
import com.product_service.application.shop.result.ShopResult;
import com.product_service.domain.model.enumtype.ShopStatus;
import com.product_service.domain.model.vo.ShopId;
import com.product_service.domain.repository.ShopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class ChangeShopStatusUseCase {
    private final ShopRepository repository;
    private final Clock clock;

    public ChangeShopStatusUseCase(ShopRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public ShopResult change(UUID shopId, UUID actorSellerId, ShopStatus status) {
        var shop = repository.findById(new ShopId(shopId)).orElseThrow(() -> new ShopNotFoundException(shopId));
        if (!actorSellerId.equals(shop.getSellerId().value()))
            throw new RuntimeException("Không có quyền truy cập vào shop của người khác");
        shop.changeStatus(status, clock.instant());
        return ShopMapper.toResult(repository.save(shop));
    }
}
