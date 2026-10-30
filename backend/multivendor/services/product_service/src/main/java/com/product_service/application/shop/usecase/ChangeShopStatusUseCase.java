package com.product_service.application.shop.usecase;

import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.product_service.application.shop.mapper.ShopMapper;
import com.product_service.application.shop.result.ShopResult;
import com.product_service.domain.model.enumtype.ShopStatus;
import com.product_service.domain.model.vo.SellerId;
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
    public ShopResult change(UUID shopId, UUID actorSellerId, ShopStatus status,boolean isAdmin) {
        var shop = repository.findById(new ShopId(shopId)).orElseThrow(() -> new DomainException(ErrorCode.SHOP_NOT_FOUND));
        if(isAdmin) {
            shop.changeStatus(status, clock.instant());
            return ShopMapper.toResult(repository.save(shop));
        }
        else {
            shop.assertOwner(new SellerId(actorSellerId));
            shop.changeStatus(status, clock.instant());
            return ShopMapper.toResult(repository.save(shop));
        }

    }
}
