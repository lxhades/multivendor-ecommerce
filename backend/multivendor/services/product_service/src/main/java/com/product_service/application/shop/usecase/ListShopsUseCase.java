package com.product_service.application.shop.usecase;

import com.product_service.adapter.out.persistence.shop.entity.ShopJpaEntity;
import com.product_service.application.shop.mapper.ShopMapper;
import com.product_service.application.shop.result.ShopResult;
import com.product_service.domain.model.aggregate.Shop;
import com.product_service.domain.model.vo.SellerId;
import com.product_service.domain.repository.ShopRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListShopsUseCase {
    private final ShopRepository repository;

    public ListShopsUseCase(ShopRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<ShopResult> list(UUID sellerId, Pageable pageable) {
        Page<Shop> shopPage = repository.findAll(pageable);
        return shopPage.map(ShopMapper::toResult);
    }
}
