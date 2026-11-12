CREATE TABLE public.shipments (
    id uuid NOT NULL,
    order_id uuid NOT NULL,
    tracking_code varchar(255) NOT NULL,
    shipping_address varchar(255) NOT NULL,
    status varchar(255) NOT NULL,
    created_at timestamptz(6) NOT NULL,
    shipped_at timestamptz(6),
    delivered_at timestamptz(6),
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT pk_shipments PRIMARY KEY (id),
    CONSTRAINT uk_shipments_order UNIQUE (order_id),
    CONSTRAINT uk_shipments_tracking_code UNIQUE (tracking_code),
    CONSTRAINT ck_shipments_tracking_code CHECK (length(btrim(tracking_code)) > 0),
    CONSTRAINT ck_shipments_address CHECK (length(btrim(shipping_address)) > 0),
    CONSTRAINT ck_shipments_version CHECK (version >= 0),
    CONSTRAINT ck_shipments_status CHECK (status IN ('PENDING', 'SHIPPED', 'DELIVERED')),
    CONSTRAINT ck_shipments_status_times CHECK (
        (status = 'PENDING' AND shipped_at IS NULL AND delivered_at IS NULL)
        OR (status = 'SHIPPED' AND shipped_at IS NOT NULL AND delivered_at IS NULL)
        OR (status = 'DELIVERED' AND shipped_at IS NOT NULL AND delivered_at IS NOT NULL)
    ),
    CONSTRAINT ck_shipments_time_order CHECK (
        (shipped_at IS NULL OR shipped_at >= created_at)
        AND (delivered_at IS NULL OR delivered_at >= shipped_at)
    )
);
COMMENT ON TABLE public.shipments IS 'Shipment lifecycle owned by Shipping service, one shipment per Order UUID.';
COMMENT ON COLUMN public.shipments.order_id IS 'Reference to Order service; no cross-database foreign key.';
COMMENT ON COLUMN public.shipments.shipping_address IS 'Delivery address snapshot; varchar(255) matches the current Shipment JPA entity.';
