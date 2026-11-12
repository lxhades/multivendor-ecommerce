CREATE TABLE public.product_outbox_events (
    id uuid PRIMARY KEY,
    aggregate_type varchar(255) NOT NULL,
    aggregate_id uuid NOT NULL,
    event_type varchar(255) NOT NULL,
    payload text NOT NULL,
    status varchar(255) NOT NULL,
    retry_count integer NOT NULL DEFAULT 0,
    created_at timestamptz(6) NOT NULL,
    published_at timestamptz(6),
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT ck_product_outbox_status CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED')),
    CONSTRAINT ck_product_outbox_retry CHECK (retry_count >= 0),
    CONSTRAINT ck_product_outbox_payload CHECK (jsonb_typeof(payload::jsonb) = 'object')
);
CREATE TABLE public.processed_delivered_orders (
    order_id uuid PRIMARY KEY
);
COMMENT ON TABLE public.processed_delivered_orders IS 'Inbox deduplication for delivered-order sales events; no FK to Order database.';
