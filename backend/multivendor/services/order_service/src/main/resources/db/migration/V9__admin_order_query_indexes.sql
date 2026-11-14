CREATE INDEX IF NOT EXISTS idx_orders_admin_created_id ON public.orders(created_at DESC, id DESC);
CREATE INDEX IF NOT EXISTS idx_orders_admin_updated_id ON public.orders(updated_at DESC, id DESC);
CREATE INDEX IF NOT EXISTS idx_orders_delivered_updated_shop ON public.orders(updated_at, shop_id) WHERE order_status = 'DELIVERED';
