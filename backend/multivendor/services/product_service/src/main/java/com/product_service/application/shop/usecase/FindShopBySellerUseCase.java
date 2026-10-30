package com.product_service.application.shop.usecase;

import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.product_service.application.shop.mapper.ShopMapper;
import com.product_service.application.shop.result.ShopResult;
import com.product_service.domain.model.vo.SellerId;
import com.product_service.domain.repository.ShopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class FindShopBySellerUseCase {
    private final ShopRepository repository;

    public FindShopBySellerUseCase(ShopRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ShopResult get(UUID id) {
        return ShopMapper.toResult(
                repository.findBySellerId(new SellerId(id)).orElseThrow(() -> new DomainException(ErrorCode.SHOP_NOT_FOUND)));
    }
}
