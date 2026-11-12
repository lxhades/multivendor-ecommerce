-- Required by BuyerSearchAdapter: Vietnamese accent-insensitive full-text search.
CREATE EXTENSION IF NOT EXISTS unaccent WITH SCHEMA public;
CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA public;
CREATE TEXT SEARCH CONFIGURATION public.catalog_search (COPY = pg_catalog.simple);
ALTER TEXT SEARCH CONFIGURATION public.catalog_search
    ALTER MAPPING FOR hword, hword_part, word WITH public.unaccent, pg_catalog.simple;
ALTER TABLE public.products ADD COLUMN search_document tsvector GENERATED ALWAYS AS (
    setweight(to_tsvector('public.catalog_search'::regconfig, coalesce(name, '')), 'A') ||
    setweight(to_tsvector('public.catalog_search'::regconfig, coalesce(description, '')), 'B')
) STORED;
CREATE INDEX idx_products_search_document ON public.products USING gin(search_document);
CREATE INDEX idx_products_name_trgm ON public.products USING gin(name public.gin_trgm_ops);
CREATE INDEX idx_variants_sku_trgm ON public.product_variants USING gin(sku public.gin_trgm_ops);
