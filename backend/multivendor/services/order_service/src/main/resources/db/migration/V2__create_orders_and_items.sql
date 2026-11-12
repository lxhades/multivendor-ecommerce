CREATE TABLE public.orders (
    id uuid PRIMARY KEY,
    checkout_id uuid NOT NULL REFERENCES public.checkouts(id),
    buyer_id uuid NOT NULL,
    shop_id uuid NOT NULL,
    shipping_address text NOT NULL,
    subtotal bigint NOT NULL,
    shipping_fee bigint NOT NULL,
    total_amount bigint NOT NULL,
    payment_method varchar(255) NOT NULL,
    payment_status varchar(255) NOT NULL,
    order_status varchar(255) NOT NULL,
    created_at timestamptz(6) NOT NULL,
    updated_at timestamptz(6) NOT NULL,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT uk_orders_checkout_shop UNIQUE (checkout_id, shop_id),
    CONSTRAINT ck_orders_address CHECK (length(btrim(shipping_address)) > 0),
    CONSTRAINT ck_orders_amounts CHECK (subtotal >= 0 AND shipping_fee >= 0 AND total_amount::numeric = subtotal::numeric + shipping_fee::numeric),
    CONSTRAINT ck_orders_payment_method CHECK (payment_method IN ('COD','VNPAY','MOCK','ATM')),
    CONSTRAINT ck_orders_payment_status CHECK (payment_status IN ('UNPAID','PENDING','PAID','FAILED','REFUND_PENDING')),
    CONSTRAINT ck_orders_status CHECK (order_status IN ('PENDING','PENDING_PAYMENT','CONFIRMED','PROCESSING','SHIPPED','DELIVERED','CANCELLED')),
    CONSTRAINT ck_orders_version CHECK (version >= 0),
    CONSTRAINT ck_orders_timestamps CHECK (updated_at >= created_at)
);
CREATE TABLE public.order_items (
    id uuid PRIMARY KEY,
    order_id uuid NOT NULL REFERENCES public.orders(id) ON DELETE CASCADE,
    product_id uuid NOT NULL,
    variant_id uuid NOT NULL,
    product_name varchar(255) NOT NULL,
    variant_name text,
    variant_image_url text,
    unit_price bigint NOT NULL,
    quantity integer NOT NULL,
    subtotal bigint NOT NULL,
    CONSTRAINT uk_order_items_order_variant UNIQUE (order_id, variant_id),
    CONSTRAINT ck_order_items_product_name CHECK (length(btrim(product_name)) > 0),
    CONSTRAINT ck_order_items_amounts CHECK (unit_price >= 0 AND quantity > 0 AND subtotal::numeric = unit_price::numeric * quantity::numeric)
);
COMMENT ON TABLE public.order_items IS 'Purchase-time snapshots of product/variant names, image URL, unit price and quantity.';
COMMENT ON COLUMN public.orders.updated_at IS 'Current report date basis for DELIVERED orders; no delivered_at field in the current entity.';
