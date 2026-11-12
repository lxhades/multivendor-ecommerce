package com.order_service.application.report.usecase;

import com.order_service.application.checkout.port.ShopOwnershipPort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

@Component
public class SellerReportAccess {
    private final ShopOwnershipPort ownership;
    public SellerReportAccess(ShopOwnershipPort ownership) { this.ownership = ownership; }

    public void requireOwner(UUID sellerId, UUID shopId) {
        boolean owner;
        try { owner = ownership.isOwner(sellerId, shopId); }
        catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Không thể xác minh quyền sở hữu cửa hàng", e);
        }
        if (!owner) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không sở hữu cửa hàng này");
    }
}
