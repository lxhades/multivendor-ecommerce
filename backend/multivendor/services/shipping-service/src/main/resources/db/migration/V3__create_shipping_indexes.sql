-- order_id and tracking_code already have UNIQUE indexes from V1.
CREATE INDEX idx_shipments_status_shipped ON public.shipments(status, shipped_at);
CREATE INDEX idx_shipping_outbox_ready ON public.shipping_outbox_events(status, next_retry_at, created_at, id);
