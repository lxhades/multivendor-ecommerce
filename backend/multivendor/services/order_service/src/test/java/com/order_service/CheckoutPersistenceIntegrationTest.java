package com.order_service;

import com.order_service.adapter.out.outbox.OrderOutboxJpaAdapter;
import com.order_service.application.checkout.dto.CreateCheckoutResult;
import com.order_service.application.checkout.usecase.CheckoutPersistenceService;
import com.order_service.domain.model.aggregate.Checkout;
import com.order_service.domain.model.aggregate.Order;
import com.order_service.domain.model.entity.OrderItem;
import com.order_service.domain.model.enums.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@Testcontainers
class CheckoutPersistenceIntegrationTest {

    // TODO 1: Khởi tạo PostgreSQL riêng cho integration test.
    // Không sử dụng database order_db của môi trường dev.
    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("order_test_db")
                    .withUsername("test")
                    .withPassword("test");

    // TODO 2: Ghi đè datasource của Spring Boot.
    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private CheckoutPersistenceService persistenceService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // TODO 3: Spy Adapter để mô phỏng lỗi Outbox.
    // Các test khác vẫn sử dụng implementation thật.
    @MockitoSpyBean
    private OrderOutboxJpaAdapter outboxAdapter;

    // =========================================================
    // TEST 1: COMMIT THÀNH CÔNG
    // =========================================================
    @Test
    void shouldCommitCheckoutOrdersAndOutbox() {
        // TODO 1: Chuẩn bị Checkout + 2 Orders thuộc 2 Shop.
        TestData data = createTestData();

        // TODO 2: Gọi service thật.
        CreateCheckoutResult result = persistenceService.persist(
                data.checkout(),
                data.orders()
        );

        // TODO 3: Kiểm tra kết quả trả về.
        assertNotNull(result);
        assertEquals(2, result.orders().size());

        // TODO 4: Kiểm tra Checkout đã COMMIT.
        assertEquals(1L, count("SELECT COUNT(*) FROM checkouts WHERE id = ?", data.checkout().getId()));

        // TODO 5: Kiểm tra có 2 Orders.
        assertEquals(2L, count("SELECT COUNT(*) FROM orders WHERE checkout_id = ?", data.checkout().getId()));

        // TODO 6: Kiểm tra có 3 OrderItems.
        assertEquals(3L, count("""
                SELECT COUNT(*)
                FROM order_items oi
                JOIN orders o ON oi.order_id = o.id
                WHERE o.checkout_id = ?
                """, data.checkout().getId()));

        // TODO 7: Kiểm tra Outbox được lưu.
        assertEquals(1L, count("""
                SELECT COUNT(*)
                FROM order_outbox_events
                WHERE aggregate_id = ?
                """, data.checkout().getId()));

        // TODO 8: Kiểm tra trạng thái Outbox.
        String status = jdbcTemplate.queryForObject("""
                SELECT status
                FROM order_outbox_events
                WHERE aggregate_id = ?
                """, String.class, data.checkout().getId());

        assertEquals("PENDING", status);
    }

    // =========================================================
    // TEST 2: ROLLBACK KHI OUTBOX THẤT BẠI
    // =========================================================
    @Test
    void shouldRollbackEverythingWhenOutboxFails() {
        // TODO 1: Chuẩn bị dữ liệu.
        TestData data = createTestData();

        // TODO 2: Giả lập lỗi khi lưu Outbox.
        // Lỗi phát sinh sau khi Checkout và Orders đã save.
        doThrow(new IllegalStateException("Simulated outbox failure"))
                .when(outboxAdapter)
                .saveInventoryReservationRequested(any());

        // TODO 3: Gọi service và xác nhận exception.
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> persistenceService.persist(data.checkout(), data.orders())
        );

        // Vì Spring bọc lỗi gốc vào làm 'cause', nên ta cần lấy message từ cause nếu có.
        String actualMessage = exception.getCause() != null
                ? exception.getCause().getMessage()
                : exception.getMessage();

        assertEquals("Simulated outbox failure", actualMessage);

        // TODO 4: Xác nhận Checkout bị ROLLBACK.
        assertEquals(0L, count("SELECT COUNT(*) FROM checkouts WHERE id = ?", data.checkout().getId()));

        // TODO 5: Xác nhận Orders bị ROLLBACK.
        assertEquals(0L, count("SELECT COUNT(*) FROM orders WHERE checkout_id = ?", data.checkout().getId()));

        // TODO 6: Xác nhận OrderItems không tồn tại.
        assertEquals(0L, count("""
                SELECT COUNT(*)
                FROM order_items oi
                JOIN orders o ON oi.order_id = o.id
                WHERE o.checkout_id = ?
                """, data.checkout().getId()));

        // TODO 7: Xác nhận Outbox không tồn tại.
        assertEquals(0L, count("""
                SELECT COUNT(*)
                FROM order_outbox_events
                WHERE aggregate_id = ?
                """, data.checkout().getId()));
    }

    // =========================================================
    // TEST 3: JSONB PAYLOAD
    // =========================================================
    @Test
    void shouldStoreOutboxPayloadAsJsonbObject() {
        // TODO 1: Tạo dữ liệu và lưu bằng Adapter thật.
        TestData data = createTestData();

        persistenceService.persist(
                data.checkout(),
                data.orders()
        );

        // TODO 2: Kiểm tra kiểu PostgreSQL là jsonb.
        String columnType = jdbcTemplate.queryForObject("""
                SELECT pg_typeof(payload)::text
                FROM order_outbox_events
                WHERE aggregate_id = ?
                """, String.class, data.checkout().getId());

        assertEquals("jsonb", columnType);

        // TODO 3: Kiểm tra payload là JSON object.
        // Nếu bị double-encode, kết quả có thể là "string".
        String jsonType = jdbcTemplate.queryForObject("""
                SELECT jsonb_typeof(payload)
                FROM order_outbox_events
                WHERE aggregate_id = ?
                """, String.class, data.checkout().getId());

        assertEquals("object", jsonType);

        // TODO 4: Kiểm tra checkoutId trong JSON.
        String checkoutId = jdbcTemplate.queryForObject("""
                SELECT payload ->> 'checkoutId'
                FROM order_outbox_events
                WHERE aggregate_id = ?
                """, String.class, data.checkout().getId());

        assertEquals(data.checkout().getId().toString(), checkoutId);

        // TODO 5: Kiểm tra buyerId trong JSON.
        String buyerId = jdbcTemplate.queryForObject("""
                SELECT payload ->> 'buyerId'
                FROM order_outbox_events
                WHERE aggregate_id = ?
                """, String.class, data.checkout().getId());

        assertEquals(data.checkout().getBuyerId().toString(), buyerId);

        // TODO 6: Kiểm tra số Item trong Event.
        Integer itemCount = jdbcTemplate.queryForObject("""
                SELECT jsonb_array_length(payload -> 'items')
                FROM order_outbox_events
                WHERE aggregate_id = ?
                """, Integer.class, data.checkout().getId());

        assertEquals(3, itemCount);

        // TODO 7: Kiểm tra eventType trong JSON.
        String eventType = jdbcTemplate.queryForObject("""
                SELECT payload ->> 'eventType'
                FROM order_outbox_events
                WHERE aggregate_id = ?
                """, String.class, data.checkout().getId());

        assertEquals("InventoryReservationRequested", eventType);
    }

    // =========================================================
    // HELPER: TẠO TEST DATA
    // =========================================================
    private TestData createTestData() {
        // TODO 1: Tạo Buyer và Checkout.
        UUID buyerId = UUID.randomUUID();
        UUID checkoutId = UUID.randomUUID();
        Instant now = Instant.now();

        Checkout checkout = Checkout.create(
                checkoutId,
                buyerId,
                UUID.randomUUID().toString(),
                PaymentMethod.COD,
                now
        );

        // TODO 2: Tạo 2 sản phẩm cho Shop A.
        OrderItem itemA1 = OrderItem.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "Keyboard", 500_000L, 2
        );

        OrderItem itemA2 = OrderItem.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "Mouse", 200_000L, 1
        );

        // TODO 3: Tạo 1 sản phẩm cho Shop B.
        OrderItem itemB1 = OrderItem.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "Monitor", 3_000_000L, 1
        );

        // TODO 4: Tạo Order cho Shop A.
        Order orderA = Order.create(
                UUID.randomUUID(), checkoutId, buyerId, UUID.randomUUID(),
                List.of(itemA1, itemA2), "Ha Noi", 0L, PaymentMethod.COD, now
        );

        // TODO 5: Tạo Order cho Shop B.
        Order orderB = Order.create(
                UUID.randomUUID(), checkoutId, buyerId, UUID.randomUUID(),
                List.of(itemB1), "Ha Noi", 0L, PaymentMethod.COD, now
        );

        return new TestData(checkout, List.of(orderA, orderB));
    }

    // =========================================================
    // HELPER: ĐẾM BẢN GHI POSTGRESQL
    // =========================================================
    private long count(String sql, Object... params) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class, params);
        return value == null ? 0L : value;
    }

    private record TestData(Checkout checkout, List<Order> orders) {
    }
}