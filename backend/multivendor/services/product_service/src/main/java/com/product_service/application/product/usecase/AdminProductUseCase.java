package com.product_service.application.product.usecase;

import com.product_service.application.product.dto.ProductDtos.Seller;
import com.product_service.domain.model.enumtype.ApprovalStatus;
import com.product_service.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

@Service
public class AdminProductUseCase {

    private final ProductRepository repo;
    private final Clock clock;

    public AdminProductUseCase(ProductRepository repo, Clock clock) {
        this.repo = repo;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<Seller> pending() {
        return repo.findPending()
                .stream()
                .map(Seller::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public Seller details(UUID id) {
        return repo.findById(id)
                .map(Seller::of)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
    }

    @Transactional
    public Seller decide(UUID adminId, UUID id, ApprovalStatus status, String reason) {
        var p = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        p.decide(status, adminId, reason, clock.instant());

        return Seller.of(repo.save(p));
    }
}