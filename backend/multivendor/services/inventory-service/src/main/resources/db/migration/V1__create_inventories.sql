CREATE TABLE public.inventories (
    id uuid NOT NULL,
    product_id uuid NOT NULL,
    variant_id uuid NOT NULL,
    shop_id uuid NOT NULL,
    on_hand_quantity bigint NOT NULL DEFAULT 0,
    reserved_quantity bigint NOT NULL DEFAULT 0,
    version bigint NOT NULL DEFAULT 0,
    created_at timestamptz(6) NOT NULL,
    updated_at timestamptz(6) NOT NULL,
    CONSTRAINT pk_inventories PRIMARY KEY (id),
    CONSTRAINT uk_inventories_variant UNIQUE (variant_id),
    CONSTRAINT ck_inventories_quantities CHECK (
        on_hand_quantity >= 0 AND reserved_quantity >= 0 AND reserved_quantity <= on_hand_quantity
    ),
    CONSTRAINT ck_inventories_version CHECK (version >= 0),
    CONSTRAINT ck_inventories_timestamps CHECK (updated_at >= created_at)
);
COMMENT ON TABLE public.inventories IS 'One inventory record per Product variant; product/shop IDs reference external services without cross-database FKs.';
COMMENT ON COLUMN public.inventories.on_hand_quantity IS 'Physical stock including stock currently reserved.';
COMMENT ON COLUMN public.inventories.reserved_quantity IS 'Stock held by active reservations; available stock is on_hand_quantity - reserved_quantity.';
