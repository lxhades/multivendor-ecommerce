package com.order_service.application.checkout.usecase;

import com.order_service.application.checkout.dto.CheckoutResult;
import com.order_service.domain.repository.CheckoutRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetCheckoutUseCase {
    private final CheckoutRepository checkouts;

    public GetCheckoutUseCase(CheckoutRepository checkouts) {
        this.checkouts = checkouts;
    }

    @Transactional(readOnly = true)
    public CheckoutResult execute(UUID checkoutId) {

        return CheckoutResult.from(
                checkouts.findById(checkoutId)
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên thanh toán")));
    }
}
