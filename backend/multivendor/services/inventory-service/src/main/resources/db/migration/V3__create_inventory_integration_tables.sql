CREATE TABLE public.kafka_inbox_events (
    id uuid NOT NULL,
    consumer_name varchar(100) NOT NULL,
    event_id uuid NOT NULL,
    event_type varchar(100) NOT NULL,
    processed_at timestamptz(6) NOT NULL,
    CONSTRAINT pk_kafka_inbox_events PRIMARY KEY (id),
    CONSTRAINT uk_kafka_inbox_consumer_event UNIQUE (consumer_name, event_id)
);
CREATE TABLE public.inventory_outbox_events (
    id uuid NOT NULL,
    aggregate_id uuid NOT NULL,
    event_type varchar(100) NOT NULL,
    payload jsonb NOT NULL,
    status varchar(20) NOT NULL,
    attempt_count integer NOT NULL DEFAULT 0,
    next_retry_at timestamptz(6),
    last_error text,
    created_at timestamptz(6) NOT NULL,
    published_at timestamptz(6),
    CONSTRAINT pk_inventory_outbox_events PRIMARY KEY (id),
    CONSTRAINT ck_inventory_outbox_status CHECK (status IN ('PENDING', 'PUBLISHED')),
    CONSTRAINT ck_inventory_outbox_attempt CHECK (attempt_count >= 0),
    CONSTRAINT ck_inventory_outbox_payload CHECK (jsonb_typeof(payload) = 'object')
);
COMMENT ON TABLE public.kafka_inbox_events IS 'Deduplication of consumed business events per consumer.';
COMMENT ON TABLE public.inventory_outbox_events IS 'Reservation result/release events waiting for Kafka publication.';
