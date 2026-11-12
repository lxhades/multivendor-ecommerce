-- Auth owns this schema. No references to another service's database.
CREATE TABLE public.users (
    id uuid NOT NULL,
    email varchar(255) NOT NULL,
    phone varchar(255) NOT NULL,
    password_hash varchar(255) NOT NULL,
    status varchar(255) NOT NULL,
    role varchar(20) NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT ck_users_email_not_blank CHECK (length(btrim(email)) > 0),
    CONSTRAINT ck_users_phone_length CHECK (length(phone) >= 8),
    CONSTRAINT ck_users_password_hash_length CHECK (length(password_hash) >= 8),
    CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'LOCKED')),
    CONSTRAINT ck_users_role CHECK (role IN ('BUYER', 'SELLER', 'ADMIN')),
    CONSTRAINT ck_users_timestamps CHECK (updated_at >= created_at)
);
COMMENT ON TABLE public.users IS 'Accounts owned by Auth service; other services reference the UUID only.';
COMMENT ON COLUMN public.users.password_hash IS 'Encoded password, never plaintext.';
COMMENT ON COLUMN public.users.role IS 'Primary role; effective JWT roles are calculated in the application.';
