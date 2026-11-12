CREATE INDEX idx_users_role_status ON public.users (role, status);
CREATE INDEX idx_users_created_at ON public.users (created_at, id);
CREATE INDEX idx_user_addresses_user_id ON public.user_addresses (user_id);

-- A deferred UNIQUE constraint avoids rejecting Hibernate's temporary flush order
-- when a new default address is inserted before the previous one is updated.
-- NULL values allow any number of non-default addresses.
ALTER TABLE public.user_addresses
    ADD COLUMN default_user_id uuid GENERATED ALWAYS AS (
        CASE WHEN is_default THEN user_id ELSE NULL END
    ) STORED;
ALTER TABLE public.user_addresses
    ADD CONSTRAINT uk_user_addresses_one_default
    UNIQUE (default_user_id) DEFERRABLE INITIALLY DEFERRED;
COMMENT ON COLUMN public.user_addresses.default_user_id IS
    'Database-generated key enforcing at most one default address per account at transaction commit; not a JPA field.';
