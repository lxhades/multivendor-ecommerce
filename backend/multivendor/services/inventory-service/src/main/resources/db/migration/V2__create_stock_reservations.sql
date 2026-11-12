CREATE TABLE public.stock_reservations (
    id uuid NOT NULL,
    order_id uuid NOT NULL,
    inventory_id uuid NOT NULL,
    quantity bigint NOT NULL,
    status varchar(20) NOT NULL,
    created_at timestamptz(6) NOT NULL,
    updated_at timestamptz(6) NOT NULL,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT pk_stock_reservations PRIMARY KEY (id),
    CONSTRAINT uk_reservation_order_inventory UNIQUE (order_id, inventory_id),
    CONSTRAINT fk_stock_reservations_inventory FOREIGN KEY (inventory_id) REFERENCES public.inventories(id),
    CONSTRAINT ck_stock_reservations_quantity CHECK (quantity > 0),
    CONSTRAINT ck_stock_reservations_status CHECK (status IN ('RESERVED', 'CONFIRMED', 'RELEASED')),
    CONSTRAINT ck_stock_reservations_version CHECK (version >= 0),
    CONSTRAINT ck_stock_reservations_timestamps CHECK (updated_at >= created_at)
);
COMMENT ON COLUMN public.stock_reservations.order_id IS 'Order UUID; no FK to the Order service database.';
