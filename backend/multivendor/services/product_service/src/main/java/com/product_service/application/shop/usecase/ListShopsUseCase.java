package com.product_service.application.shop.usecase;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.product_service.application.shop.mapper.ShopMapper;
import com.product_service.application.shop.result.ShopResult;
import com.product_service.domain.model.vo.SellerId;
import com.product_service.domain.repository.ShopRepository;
@Service
public class ListShopsUseCase {
    private final ShopRepository repository;
    public ListShopsUseCase(ShopRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ShopResult> list(UUID sellerId) {
        var shops = sellerId == null ? repository.findAll() : repository.findBySellerId(new SellerId(sellerId));
        return shops.stream().map(ShopMapper::toResult).toList();
    }
}
