package com.order_service.application.checkout.usecase;

import com.order_service.application.checkout.command.CheckoutItemCommand;
import com.order_service.application.checkout.command.CreateCheckoutCommand;
import com.order_service.application.checkout.dto.CheckoutResult;
import com.order_service.application.checkout.dto.CreateCheckoutResult;

import com.order_service.application.checkout.port.ProductPricingPort;
import com.order_service.application.order.dto.OrderResult;
import com.order_service.domain.model.aggregate.Checkout;
import com.order_service.domain.model.aggregate.Order;
import com.order_service.domain.model.entity.OrderItem;
import com.order_service.domain.repository.CheckoutRepository;
import com.order_service.domain.repository.OrderRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.*;

@Service
public class CreateCheckoutUseCase {

    private final CheckoutRepository checkoutRepository;
    private final OrderRepository orderRepository;
    private final ProductPricingPort productPricingPort;
    private final CheckoutPersistenceService persistenceService;
    private final Clock clock;

    public CreateCheckoutUseCase(
            CheckoutRepository checkoutRepository,
            OrderRepository orderRepository,
            ProductPricingPort productPricingPort,
            CheckoutPersistenceService persistenceService,
            Clock clock
    ) {
        this.checkoutRepository = checkoutRepository;
        this.orderRepository = orderRepository;
        this.productPricingPort = productPricingPort;
        this.persistenceService = persistenceService;
        this.clock = clock;
    }

    public CreateCheckoutResult execute(CreateCheckoutCommand command) {

        Objects.requireNonNull(command, "Command is required");

        // IDEMPOTENCY CHECK
        Optional<CreateCheckoutResult> existingResult =
                findExistingCheckout(command);
        if (existingResult.isPresent()) {
            return existingResult.get();
        }

        // VERIFY VARIANTS + VALIDATE RESPONSE
        Map<UUID, ProductPricingPort.VerifiedVariant> verifiedMap =
                verifyAndValidateVariants(command);

        // GROUP BY SHOP
        Map<UUID, List<OrderItem>> itemsByShop =
                groupItemsByShop(command, verifiedMap);

        // CREATE CHECKOUT / ORDERS
        Instant now = clock.instant();
        Checkout checkout = createCheckout(command, now);
        List<Order> newOrders =
                createOrders(command, checkout, itemsByShop, now);

        // PERSIST TRANSACTION
        try {
            return persistenceService.persist(checkout, newOrders);

        } catch (DataIntegrityViolationException ex) {
            return findExistingCheckout(command)
                    .orElseThrow(() -> ex);
        }
    }


      //IDEMPOTENCY CHECK

    private Optional<CreateCheckoutResult> findExistingCheckout(
            CreateCheckoutCommand command
    ) {
        return checkoutRepository
                .findByBuyerIdAndIdempotencyKey(
                        command.buyerId(),
                        command.idempotencyKey()
                )
                .map(checkout -> new CreateCheckoutResult(
                        CheckoutResult.from(checkout),
                        orderRepository.findByCheckoutId(checkout.getId())
                                .stream()
                                .map(OrderResult::from)
                                .toList()
                ));
    }


    private Map<UUID, ProductPricingPort.VerifiedVariant> verifyAndValidateVariants(
            CreateCheckoutCommand command
    ) {
        List<UUID> variantIds = command.items()
                .stream()
                .map(CheckoutItemCommand::variantId)
                .toList();

        List<ProductPricingPort.VerifiedVariant> verifiedVariants =
                productPricingPort.verifyVariants(variantIds);

        return validateAndIndexVariants(variantIds, verifiedVariants);
    }

    private Map<UUID, List<OrderItem>> groupItemsByShop(
            CreateCheckoutCommand command,
            Map<UUID, ProductPricingPort.VerifiedVariant> verifiedMap
    ) {
        Map<UUID, List<OrderItem>> itemsByShop = new LinkedHashMap<>();

        for (CheckoutItemCommand item : command.items()) {

            ProductPricingPort.VerifiedVariant variant =
                    verifiedMap.get(item.variantId());

            OrderItem orderItem = OrderItem.create(
                    UUID.randomUUID(),
                    variant.productId(),
                    variant.variantId(),
                    variant.productName(),
                    variant.unitPrice(),
                    item.quantity()
            );

            itemsByShop
                    .computeIfAbsent(
                            variant.shopId(),
                            key -> new ArrayList<>()
                    )
                    .add(orderItem);
        }

        return itemsByShop;
    }

    private Checkout createCheckout(
            CreateCheckoutCommand command,
            Instant now
    ) {
        Checkout checkout = Checkout.create(
                UUID.randomUUID(),
                command.buyerId(),
                command.idempotencyKey(),
                command.paymentMethod(),
                now
        );

        checkout.startProcessing(now);
        return checkout;
    }


    private List<Order> createOrders(
            CreateCheckoutCommand command,
            Checkout checkout,
            Map<UUID, List<OrderItem>> itemsByShop,
            Instant now
    ) {
        List<Order> newOrders = new ArrayList<>();

        for (Map.Entry<UUID, List<OrderItem>> entry
                : itemsByShop.entrySet()) {

            UUID shopId = entry.getKey();
            List<OrderItem> orderItems = entry.getValue();

            Order order = Order.create(
                    UUID.randomUUID(),
                    checkout.getId(),
                    command.buyerId(),
                    shopId,
                    orderItems,
                    command.shippingAddress(),
                    //TODO mock payment fee
                    0L,
                    command.paymentMethod(),
                    now
            );

            newOrders.add(order);
        }

        return newOrders;
    }


    private Map<UUID, ProductPricingPort.VerifiedVariant> validateAndIndexVariants(
            List<UUID> requestedIds,
            List<ProductPricingPort.VerifiedVariant> verifiedVariants
    ) {

        if (verifiedVariants == null) {
            throw new IllegalStateException(
                    "Product Service returned null response"
            );
        }

        Set<UUID> requestedSet = new HashSet<>(requestedIds);

        if (requestedSet.size() != requestedIds.size()) {
            throw new IllegalArgumentException(
                    "Duplicate variantId in request"
            );
        }

        if (verifiedVariants.size() != requestedSet.size()) {
            throw new IllegalStateException(
                    "Product Service returned incomplete variants"
            );
        }

        Map<UUID, ProductPricingPort.VerifiedVariant> verifiedMap =
                new HashMap<>();

        for (ProductPricingPort.VerifiedVariant variant : verifiedVariants) {

            if (variant == null) {
                throw new IllegalStateException(
                        "Null variant from Product Service"
                );
            }

            System.out.println("variant available: " + variant+ " "+variant.available());
            if (!variant.available()) {
                throw new IllegalArgumentException(
                        "Variant is not available: " + variant.variantId()
                );
            }

            if (variant.productId() == null
                    || variant.variantId() == null
                    || variant.shopId() == null
                    || variant.productName() == null
                    || variant.productName().isBlank()
                    || variant.unitPrice() < 0) {

                throw new IllegalStateException(
                        "Invalid product data returned by Product Service"
                );
            }

            if (!requestedSet.contains(variant.variantId())) {
                throw new IllegalStateException(
                        "Unexpected variant: " + variant.variantId()
                );
            }

            if (verifiedMap.putIfAbsent(
                    variant.variantId(), variant
            ) != null) {
                throw new IllegalStateException(
                        "Duplicate verified variant: "
                                + variant.variantId()
                );
            }
        }

        return verifiedMap;
    }
}