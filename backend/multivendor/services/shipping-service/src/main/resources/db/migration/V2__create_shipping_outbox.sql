CREATE TABLE public.shipping_outbox_events (
    id uuid NOT NULL,
    aggregate_id uuid NOT NULL,
    event_type varchar(100) NOT NULL,
    payload text NOT NULL,
    status varchar(20) NOT NULL,
    created_at timestamptz(6) NOT NULL,
    published_at timestamptz(6),
    attempt_count integer NOT NULL DEFAULT 0,
    next_retry_at timestamptz(6) NOT NULL,
    last_error varchar(2000),
    CONSTRAINT pk_shipping_outbox_events PRIMARY KEY (id),
    CONSTRAINT ck_shipping_outbox_status CHECK (status IN ('PENDING', 'PUBLISHED')),
    CONSTRAINT ck_shipping_outbox_attempt CHECK (attempt_count >= 0),
    CONSTRAINT ck_shipping_outbox_payload CHECK (jsonb_typeof(payload::jsonb) = 'object'),
    CONSTRAINT ck_shipping_outbox_published CHECK (status <> 'PUBLISHED' OR published_at IS NOT NULL)
);
COMMENT ON COLUMN public.shipping_outbox_events.aggregate_id IS 'Order UUID, as assigned by ShippingOutboxService.';
COMMENT ON COLUMN public.shipping_outbox_events.payload IS 'JSON object stored as TEXT, matching ShippingOutboxEvent mapping.';
