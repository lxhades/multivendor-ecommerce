CREATE TABLE public.product_reviews (
    id uuid PRIMARY KEY,
    product_id uuid NOT NULL,
    buyer_id uuid NOT NULL,
    buyer_name varchar(255) NOT NULL,
    order_id uuid NOT NULL REFERENCES public.orders(id),
    rating integer NOT NULL,
    comment text NOT NULL,
    media_urls text NOT NULL,
    created_at timestamptz(6) NOT NULL,
    updated_at timestamptz(6) NOT NULL,
    CONSTRAINT uk_product_reviews_order_product UNIQUE (order_id, product_id),
    CONSTRAINT ck_product_reviews_rating_comment CHECK (rating BETWEEN 1 AND 5 AND char_length(trim(comment)) BETWEEN 1 AND 2000),
    CONSTRAINT ck_product_reviews_name CHECK (length(btrim(buyer_name)) > 0),
    CONSTRAINT ck_product_reviews_media CHECK (jsonb_typeof(media_urls::jsonb) = 'array'),
    CONSTRAINT ck_product_reviews_timestamps CHECK (updated_at >= created_at)
);
COMMENT ON TABLE public.product_reviews IS 'Separate review records; verified purchase is checked against local orders and order_items in the application.';
COMMENT ON COLUMN public.product_reviews.media_urls IS 'JSON array stored as TEXT by ReviewMediaUrlsConverter; no physical media files in Order service.';
