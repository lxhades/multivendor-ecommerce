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
    ('10000000-0000-4000-8000-000000000001'::uuid, 'quan.tri@example.com', '0901000001', '${authInitialPasswordHash}', 'ACTIVE', 'ADMIN', '2026-10-01T08:00:00+07:00'::timestamptz, '2026-10-01T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000011'::uuid, 'quang.huy@example.com', '0901000002', '${authInitialPasswordHash}', 'ACTIVE', 'SELLER', '2026-10-02T08:00:00+07:00'::timestamptz, '2026-10-02T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000012'::uuid, 'thu.trang@example.com', '0901000003', '${authInitialPasswordHash}', 'ACTIVE', 'SELLER', '2026-10-03T08:00:00+07:00'::timestamptz, '2026-10-03T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000013'::uuid, 'hoang.nam@example.com', '0901000004', '${authInitialPasswordHash}', 'ACTIVE', 'SELLER', '2026-10-04T08:00:00+07:00'::timestamptz, '2026-10-04T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000014'::uuid, 'ngoc.lan@example.com', '0901000005', '${authInitialPasswordHash}', 'ACTIVE', 'SELLER', '2026-10-05T08:00:00+07:00'::timestamptz, '2026-10-05T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000015'::uuid, 'duc.minh@example.com', '0901000006', '${authInitialPasswordHash}', 'ACTIVE', 'SELLER', '2026-10-06T08:00:00+07:00'::timestamptz, '2026-10-06T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000101'::uuid, 'hai.yen@example.com', '0901000007', '${authInitialPasswordHash}', 'ACTIVE', 'BUYER', '2026-10-07T08:00:00+07:00'::timestamptz, '2026-10-07T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000102'::uuid, 'tuan.kiet@example.com', '0901000008', '${authInitialPasswordHash}', 'ACTIVE', 'BUYER', '2026-10-08T08:00:00+07:00'::timestamptz, '2026-10-08T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000103'::uuid, 'bao.ngoc@example.com', '0901000009', '${authInitialPasswordHash}', 'ACTIVE', 'BUYER', '2026-10-09T08:00:00+07:00'::timestamptz, '2026-10-09T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000104'::uuid, 'minh.khang@example.com', '0901000010', '${authInitialPasswordHash}', 'ACTIVE', 'BUYER', '2026-10-10T08:00:00+07:00'::timestamptz, '2026-10-10T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000105'::uuid, 'thu.ha@example.com', '0901000011', '${authInitialPasswordHash}', 'ACTIVE', 'BUYER', '2026-10-11T08:00:00+07:00'::timestamptz, '2026-10-11T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000106'::uuid, 'thanh.tam@example.com', '0901000012', '${authInitialPasswordHash}', 'ACTIVE', 'BUYER', '2026-10-12T08:00:00+07:00'::timestamptz, '2026-10-12T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000107'::uuid, 'khanh.linh@example.com', '0901000013', '${authInitialPasswordHash}', 'ACTIVE', 'BUYER', '2026-10-13T08:00:00+07:00'::timestamptz, '2026-10-13T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000108'::uuid, 'anh.tuan@example.com', '0901000014', '${authInitialPasswordHash}', 'ACTIVE', 'BUYER', '2026-10-14T08:00:00+07:00'::timestamptz, '2026-10-14T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000109'::uuid, 'my.duyen@example.com', '0901000015', '${authInitialPasswordHash}', 'ACTIVE', 'BUYER', '2026-10-15T08:00:00+07:00'::timestamptz, '2026-10-15T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000110'::uuid, 'quoc.bao@example.com', '0901000016', '${authInitialPasswordHash}', 'ACTIVE', 'BUYER', '2026-10-16T08:00:00+07:00'::timestamptz, '2026-10-16T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000111'::uuid, 'thao.vy@example.com', '0901000017', '${authInitialPasswordHash}', 'ACTIVE', 'BUYER', '2026-10-17T08:00:00+07:00'::timestamptz, '2026-10-17T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000112'::uuid, 'gia.han@example.com', '0901000018', '${authInitialPasswordHash}', 'ACTIVE', 'BUYER', '2026-10-18T08:00:00+07:00'::timestamptz, '2026-10-18T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000113'::uuid, 'hoang.long@example.com', '0901000019', '${authInitialPasswordHash}', 'ACTIVE', 'BUYER', '2026-10-19T08:00:00+07:00'::timestamptz, '2026-10-19T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000114'::uuid, 'ngoc.anh@example.com', '0901000020', '${authInitialPasswordHash}', 'ACTIVE', 'BUYER', '2026-10-20T08:00:00+07:00'::timestamptz, '2026-10-20T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000115'::uuid, 'thanh.dat@example.com', '0901000021', '${authInitialPasswordHash}', 'ACTIVE', 'BUYER', '2026-10-21T08:00:00+07:00'::timestamptz, '2026-10-21T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000116'::uuid, 'phuong.thao@example.com', '0901000022', '${authInitialPasswordHash}', 'ACTIVE', 'BUYER', '2026-10-22T08:00:00+07:00'::timestamptz, '2026-10-22T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000117'::uuid, 'nhat.minh@example.com', '0901000023', '${authInitialPasswordHash}', 'ACTIVE', 'BUYER', '2026-10-23T08:00:00+07:00'::timestamptz, '2026-10-23T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000118'::uuid, 'dieu.linh@example.com', '0901000024', '${authInitialPasswordHash}', 'ACTIVE', 'BUYER', '2026-10-24T08:00:00+07:00'::timestamptz, '2026-10-24T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000119'::uuid, 'anh.thu@example.com', '0901000025', '${authInitialPasswordHash}', 'ACTIVE', 'BUYER', '2026-10-25T08:00:00+07:00'::timestamptz, '2026-10-25T08:00:00+07:00'::timestamptz),
    ('10000000-0000-4000-8000-000000000120'::uuid, 'huu.phuoc@example.com', '0901000026', '${authInitialPasswordHash}', 'LOCKED', 'BUYER', '2026-10-26T08:00:00+07:00'::timestamptz, '2026-11-10T14:30:00+07:00'::timestamptz);
