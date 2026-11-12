-- Five additional sellers for ten one-owner shops in Product. Existing migrations stay unchanged.
-- Included only by application-local.yml. No plaintext password is committed.
-- Supply AUTH_INITIAL_PASSWORD_HASH as a BCrypt encoded password before startup.
DO $auth_seed$
DECLARE
    encoded_password text := '${authInitialPasswordHash}';
BEGIN
    IF encoded_password !~ '^[$]2[aby][$](0[4-9]|[12][0-9]|3[01])[$][./A-Za-z0-9]{53}$' THEN
        RAISE EXCEPTION 'AUTH_INITIAL_PASSWORD_HASH must be a valid BCrypt encoded password';
    END IF;
END;
$auth_seed$;

INSERT INTO public.users (id, email, phone, password_hash, status, role, created_at, updated_at)
VALUES
    ('10000000-0000-4000-8000-000000000016'::uuid, 'phuc.an@example.com', '0901000027', '${authInitialPasswordHash}', 'ACTIVE', 'SELLER', '2026-10-27T08:00:00+07:00'::timestamptz, '2026-10-27T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000017'::uuid, 'kim.ngan@example.com', '0901000028', '${authInitialPasswordHash}', 'ACTIVE', 'SELLER', '2026-10-28T08:00:00+07:00'::timestamptz, '2026-10-28T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000018'::uuid, 'gia.bao@example.com', '0901000029', '${authInitialPasswordHash}', 'ACTIVE', 'SELLER', '2026-10-29T08:00:00+07:00'::timestamptz, '2026-10-29T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000019'::uuid, 'thu.uyen@example.com', '0901000030', '${authInitialPasswordHash}', 'ACTIVE', 'SELLER', '2026-10-30T08:00:00+07:00'::timestamptz, '2026-10-30T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000020'::uuid, 'tien.dung@example.com', '0901000031', '${authInitialPasswordHash}', 'ACTIVE', 'SELLER', '2026-10-31T08:00:00+07:00'::timestamptz, '2026-10-31T08:00:00+07:00'::timestamptz);

-- Let IDENTITY assign address IDs, including when addresses already exist.
INSERT INTO public.user_addresses (receiver_name, phone, location, detail, is_default, user_id)
VALUES
    ('Nguyễn Phúc An', '0901000027', 'Hà Nội', '92 Trần Duy Hưng, cửa hàng tầng 1', true, '10000000-0000-4000-8000-000000000016'::uuid),
    ('Trần Kim Ngân', '0901000028', 'TP. Hồ Chí Minh', '57 Nguyễn Đình Chiểu, cửa hàng tầng trệt', true, '10000000-0000-4000-8000-000000000017'::uuid),
    ('Lê Gia Bảo', '0901000029', 'Đà Nẵng', '64 Hàm Nghi, tầng 1', true, '10000000-0000-4000-8000-000000000018'::uuid),
    ('Phạm Thu Uyên', '0901000030', 'Cần Thơ', '118 Nguyễn Văn Cừ, cửa hàng tầng trệt', true, '10000000-0000-4000-8000-000000000019'::uuid),
    ('Võ Tiến Dũng', '0901000031', 'Hải Phòng', '40 Văn Cao, cửa hàng tầng trệt', true, '10000000-0000-4000-8000-000000000020'::uuid);
