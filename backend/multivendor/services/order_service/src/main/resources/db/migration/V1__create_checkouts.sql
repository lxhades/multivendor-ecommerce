CREATE TABLE public.checkouts (
    id uuid PRIMARY KEY,
    buyer_id uuid NOT NULL,
    idempotency_key varchar(255) NOT NULL,
    payment_method varchar(255) NOT NULL,
    status varchar(255) NOT NULL,
    expires_at timestamptz(6) NOT NULL,
    created_at timestamptz(6) NOT NULL,
    updated_at timestamptz(6) NOT NULL,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT uk_checkout_buyer_idempotency UNIQUE (buyer_id, idempotency_key),
    CONSTRAINT ck_checkouts_key CHECK (length(btrim(idempotency_key)) > 0),
    CONSTRAINT ck_checkouts_payment_method CHECK (payment_method IN ('COD','VNPAY','MOCK','ATM')),
    CONSTRAINT ck_checkouts_status CHECK (status IN ('PENDING','PROCESSING','COMPLETED','FAILED','EXPIRED')),
    CONSTRAINT ck_checkouts_version CHECK (version >= 0),
    CONSTRAINT ck_checkouts_timestamps CHECK (updated_at >= created_at AND expires_at >= created_at)
);
COMMENT ON COLUMN public.checkouts.buyer_id IS 'Auth UUID; no foreign key across microservice databases.';
