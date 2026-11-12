CREATE TABLE public.kafka_inbox_events (
    id uuid PRIMARY KEY,
    consumer_name varchar(100) NOT NULL,
    event_id uuid NOT NULL,
    event_type varchar(100) NOT NULL,
    processed_at timestamptz(6) NOT NULL,
    CONSTRAINT uk_order_inbox_consumer_event UNIQUE (consumer_name, event_id)
);
CREATE TABLE public.order_outbox_events (
    id uuid PRIMARY KEY,
    aggregate_id uuid NOT NULL,
    event_type varchar(100) NOT NULL,
    payload jsonb NOT NULL,
    status varchar(20) NOT NULL,
    created_at timestamptz(6) NOT NULL,
    published_at timestamptz(6),
    attempt_count integer NOT NULL DEFAULT 0,
    next_retry_at timestamptz(6),
    last_error text,
    CONSTRAINT ck_order_outbox_status CHECK (status IN ('PENDING','PROCESSING','PUBLISHED')),
    CONSTRAINT ck_order_outbox_attempt CHECK (attempt_count >= 0),
    CONSTRAINT ck_order_outbox_payload CHECK (jsonb_typeof(payload) = 'object')
);
