CREATE TABLE public.categories (
    id uuid PRIMARY KEY,
    parent_id uuid,
    name varchar(150) NOT NULL,
    slug varchar(180) NOT NULL,
    description text,
    image_url text,
    status varchar(20) NOT NULL,
    display_order integer NOT NULL DEFAULT 0,
    created_at timestamptz(6) NOT NULL,
    updated_at timestamptz(6) NOT NULL,
    CONSTRAINT uk_categories_slug UNIQUE (slug),
    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES public.categories(id),
    CONSTRAINT ck_categories_not_self_parent CHECK (parent_id IS NULL OR parent_id <> id),
    CONSTRAINT ck_categories_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT ck_categories_order CHECK (display_order >= 0),
    CONSTRAINT ck_categories_name_slug CHECK (length(btrim(name)) > 0 AND length(btrim(slug)) > 0),
    CONSTRAINT ck_categories_timestamps CHECK (updated_at >= created_at)
);
