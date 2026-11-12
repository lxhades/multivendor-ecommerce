-- variant_id and (order_id, inventory_id) already have UNIQUE indexes.
CREATE INDEX idx_inventories_shop_id ON public.inventories(shop_id);
CREATE INDEX idx_inventories_product_id ON public.inventories(product_id);
CREATE INDEX idx_reservation_order ON public.stock_reservations(order_id);
CREATE INDEX idx_reservation_inventory ON public.stock_reservations(inventory_id);
CREATE INDEX idx_reservation_status_created ON public.stock_reservations(status, created_at);
CREATE INDEX idx_kafka_inbox_processed_at ON public.kafka_inbox_events(processed_at);
CREATE INDEX idx_inventory_outbox_ready
    ON public.inventory_outbox_events(status, next_retry_at, created_at, id);
