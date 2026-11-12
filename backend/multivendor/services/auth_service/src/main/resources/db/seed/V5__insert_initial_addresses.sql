-- Every seeded account has one default delivery address; six have a second address.
INSERT INTO public.user_addresses (id, receiver_name, phone, location, detail, is_default, user_id)
VALUES
    (1, 'Nguyễn Minh Anh', '0901000001', 'TP. Hồ Chí Minh', '18 Nguyễn Văn Thủ, căn hộ 502', true, '10000000-0000-4000-8000-000000000001'::uuid),
    (2, 'Trần Quang Huy', '0901000002', 'TP. Hồ Chí Minh', '124 Nguyễn Trãi, tầng 1', true, '10000000-0000-4000-8000-000000000011'::uuid),
    (3, 'Lê Thu Trang', '0901000003', 'Hà Nội', '46 Hàng Bông, cửa hàng tầng trệt', true, '10000000-0000-4000-8000-000000000012'::uuid),
    (4, 'Phạm Hoàng Nam', '0901000004', 'Đà Nẵng', '87 Nguyễn Văn Linh, cửa hàng tầng trệt', true, '10000000-0000-4000-8000-000000000013'::uuid),
    (5, 'Võ Ngọc Lan', '0901000005', 'Cần Thơ', '32 Mậu Thân, kho nhận hàng phía sau', true, '10000000-0000-4000-8000-000000000014'::uuid),
    (6, 'Đặng Đức Minh', '0901000006', 'Hải Phòng', '65 Lạch Tray, cửa hàng số 2', true, '10000000-0000-4000-8000-000000000015'::uuid),
    (7, 'Nguyễn Hải Yến', '0901000007', 'Hà Nội', '28 Kim Mã, căn hộ 1203', true, '10000000-0000-4000-8000-000000000101'::uuid),
    (8, 'Trần Tuấn Kiệt', '0901000008', 'TP. Hồ Chí Minh', '96 Điện Biên Phủ, tầng 3', true, '10000000-0000-4000-8000-000000000102'::uuid),
    (9, 'Lê Bảo Ngọc', '0901000009', 'Đà Nẵng', '14 Lê Duẩn, nhà riêng', true, '10000000-0000-4000-8000-000000000103'::uuid),
    (10, 'Phạm Minh Khang', '0901000010', 'Cần Thơ', '76 Nguyễn Văn Cừ, nhà riêng', true, '10000000-0000-4000-8000-000000000104'::uuid),
    (11, 'Hoàng Thu Hà', '0901000011', 'Hải Phòng', '21 Tô Hiệu, căn hộ 301', true, '10000000-0000-4000-8000-000000000105'::uuid),
    (12, 'Vũ Thanh Tâm', '0901000012', 'Hà Nội', '54 Nguyễn Chí Thanh, tầng 4', true, '10000000-0000-4000-8000-000000000106'::uuid),
    (13, 'Đỗ Khánh Linh', '0901000013', 'TP. Hồ Chí Minh', '38 Phan Đăng Lưu, căn hộ 802', true, '10000000-0000-4000-8000-000000000107'::uuid),
    (14, 'Bùi Anh Tuấn', '0901000014', 'Đà Nẵng', '63 Trần Phú, tầng 2', true, '10000000-0000-4000-8000-000000000108'::uuid),
    (15, 'Đặng Mỹ Duyên', '0901000015', 'Cần Thơ', '12 Trần Hưng Đạo, nhà riêng', true, '10000000-0000-4000-8000-000000000109'::uuid),
    (16, 'Hồ Quốc Bảo', '0901000016', 'Hải Phòng', '49 Trần Nguyên Hãn, nhà riêng', true, '10000000-0000-4000-8000-000000000110'::uuid),
    (17, 'Ngô Thảo Vy', '0901000017', 'Hà Nội', '72 Giảng Võ, căn hộ 905', true, '10000000-0000-4000-8000-000000000111'::uuid),
    (18, 'Dương Gia Hân', '0901000018', 'TP. Hồ Chí Minh', '25 Nguyễn Đình Chiểu, tầng 2', true, '10000000-0000-4000-8000-000000000112'::uuid),
    (19, 'Lý Hoàng Long', '0901000019', 'Đà Nẵng', '36 Hàm Nghi, nhà riêng', true, '10000000-0000-4000-8000-000000000113'::uuid),
    (20, 'Phan Ngọc Ánh', '0901000020', 'Cần Thơ', '58 Lý Tự Trọng, nhà riêng', true, '10000000-0000-4000-8000-000000000114'::uuid),
    (21, 'Trương Thành Đạt', '0901000021', 'Hải Phòng', '83 Văn Cao, căn hộ 406', true, '10000000-0000-4000-8000-000000000115'::uuid),
    (22, 'Mai Phương Thảo', '0901000022', 'Hà Nội', '17 Láng Hạ, tầng 5', true, '10000000-0000-4000-8000-000000000116'::uuid),
    (23, 'Tạ Nhật Minh', '0901000023', 'TP. Hồ Chí Minh', '62 Võ Thị Sáu, căn hộ 701', true, '10000000-0000-4000-8000-000000000117'::uuid),
    (24, 'Cao Diệu Linh', '0901000024', 'Đà Nẵng', '41 Hoàng Diệu, tầng 1', true, '10000000-0000-4000-8000-000000000118'::uuid),
    (25, 'Tô Anh Thư', '0901000025', 'Cần Thơ', '29 Nguyễn Trãi, nhà riêng', true, '10000000-0000-4000-8000-000000000119'::uuid),
    (26, 'Đinh Hữu Phước', '0901000026', 'Hải Phòng', '108 Đà Nẵng, nhà riêng', true, '10000000-0000-4000-8000-000000000120'::uuid),
    (27, 'Nguyễn Minh Anh', '0901000001', 'Hà Nội', '35 Trần Duy Hưng, văn phòng tầng 6', false, '10000000-0000-4000-8000-000000000001'::uuid),
    (28, 'Trần Quang Huy', '0901000002', 'TP. Hồ Chí Minh', '205 Lý Thường Kiệt, kho hàng A', false, '10000000-0000-4000-8000-000000000011'::uuid),
    (29, 'Nguyễn Hải Yến', '0901000007', 'Hà Nội', '61 Thái Hà, văn phòng tầng 3', false, '10000000-0000-4000-8000-000000000101'::uuid),
    (30, 'Trần Tuấn Kiệt', '0901000008', 'TP. Hồ Chí Minh', '140 Cách Mạng Tháng Tám, quầy lễ tân', false, '10000000-0000-4000-8000-000000000102'::uuid),
    (31, 'Đỗ Khánh Linh', '0901000013', 'TP. Hồ Chí Minh', '89 Hoàng Văn Thụ, văn phòng tầng 4', false, '10000000-0000-4000-8000-000000000107'::uuid),
    (32, 'Mai Phương Thảo', '0901000022', 'Hà Nội', '24 Nguyễn Thị Định, quầy lễ tân', false, '10000000-0000-4000-8000-000000000116'::uuid);

-- Explicit seed IDs must not collide with later JPA IDENTITY inserts.
SELECT setval(
    pg_get_serial_sequence('public.user_addresses', 'id'),
    (SELECT COALESCE(MAX(id), 1) FROM public.user_addresses),
    EXISTS (SELECT 1 FROM public.user_addresses)
);
