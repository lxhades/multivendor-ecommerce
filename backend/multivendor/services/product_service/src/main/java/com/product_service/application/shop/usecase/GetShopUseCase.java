package com.product_service.application.shop.usecase;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.product_service.application.shop.exception.ShopNotFoundException;
import com.product_service.application.shop.mapper.ShopMapper;
import com.product_service.application.shop.result.ShopResult;
import com.product_service.domain.model.vo.ShopId;
import com.product_service.domain.repository.ShopRepository;
@Service
public class GetShopUseCase {
    private final ShopRepository repository;
    public GetShopUseCase(ShopRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ShopResult get(UUID id) {
        return ShopMapper.toResult(
                repository.findById(new ShopId(id)).orElseThrow(() -> new ShopNotFoundException(id)));
    }
}
