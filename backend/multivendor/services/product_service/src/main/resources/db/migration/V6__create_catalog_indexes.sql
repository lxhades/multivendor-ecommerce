CREATE INDEX idx_categories_parent_order ON public.categories(parent_id, display_order, id);
CREATE INDEX idx_shops_status_created ON public.shops(status, created_at DESC, id);
CREATE INDEX idx_products_shop_created ON public.products(shop_id, created_at DESC, id);
CREATE INDEX idx_products_category ON public.products(category_id);
CREATE INDEX idx_products_visible_created ON public.products(created_at DESC, id)
    WHERE deleted_at IS NULL AND status = 'ACTIVE' AND approval_status = 'APPROVED';
CREATE INDEX idx_products_visible_price ON public.products(min_price, id)
    WHERE deleted_at IS NULL AND status = 'ACTIVE' AND approval_status = 'APPROVED';
CREATE UNIQUE INDEX uk_products_shop_slug_live ON public.products(shop_id, slug) WHERE deleted_at IS NULL;
CREATE INDEX idx_variants_product ON public.product_variants(product_id);
CREATE INDEX idx_variants_product_live ON public.product_variants(product_id) WHERE deleted_at IS NULL AND active;
CREATE INDEX idx_approval_histories_product_created ON public.product_approval_histories(product_id, created_at DESC);
CREATE INDEX idx_product_outbox_pending ON public.product_outbox_events(created_at, id)
    WHERE status = 'PENDING' AND retry_count < 5;
