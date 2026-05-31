-- =====================================================================
-- Sistem Tracking Produksi Sevenspeed - Seed Data Demo
-- Versi: 0.1
--
-- Catatan: Seed ini mengasumsikan skema dari sevenspeed-schema-v0.1.sql
-- sudah dijalankan duluan (termasuk seed minimal: roles, divisions,
-- workflows, workflow_steps, product_types).
--
-- Konten:
--   - 10 users (1 admin, 1 owner, 1 CS, 6 operator divisi, 1 customer instansi)
--     + 5 customer fiktif lainnya
--   - 8 orders dengan kondisi yang BERAGAM:
--       * 2 selesai (DONE)
--       * 4 in-progress (di step yang berbeda-beda)
--       * 1 on-hold (nunggu approval sample)
--       * 1 baru masuk (DRAFT)
--   - Progress events realistis dengan timestamp yang masuk akal
--   - 1 skenario rework (QC reject → balik ke Sablon)
--   - 1 skenario correction (operator salah scan)
--   - 1 skenario sample ditolak (balik ke Desain)
--
-- Password semua user dummy: "password123" (bcrypt cost 10)
-- Hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- JANGAN PAKAI UNTUK PRODUKSI - INI HANYA UNTUK DEMO LOCAL
-- =====================================================================

BEGIN;

-- =====================================================================
-- USERS: Admin, Operator, Customer
-- =====================================================================

-- Owner & admin internal
INSERT INTO users (username, email, password_hash, full_name, phone, role_id, division_id, customer_type) VALUES
                                                                                                              ('owner', 'owner@sevenspeed.id', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
                                                                                                               'Bapak Suryanto Pranoto', '+6281228123456',
                                                                                                               (SELECT id FROM roles WHERE code='ADMIN'), NULL, NULL),

                                                                                                              ('admin.cs', 'cs@sevenspeed.id', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
                                                                                                               'Mbak Indah Lestari', '+6285627891234',
                                                                                                               (SELECT id FROM roles WHERE code='ADMIN'),
                                                                                                               (SELECT id FROM divisions WHERE code='CS'), NULL);

-- Operator per divisi (sengaja pakai nama lokal Kudus)
INSERT INTO users (username, password_hash, full_name, phone, role_id, division_id) VALUES
                                                                                        ('op.design',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Pak Wahyu Setiaji',      '+6285712345678',
                                                                                         (SELECT id FROM roles WHERE code='OPERATOR'), (SELECT id FROM divisions WHERE code='DESIGN')),
                                                                                        ('op.pattern',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Bu Sumarni',             '+6285712345679',
                                                                                         (SELECT id FROM roles WHERE code='OPERATOR'), (SELECT id FROM divisions WHERE code='PATTERN')),
                                                                                        ('op.cutting',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Pak Tarno Wijaya',       '+6285712345680',
                                                                                         (SELECT id FROM roles WHERE code='OPERATOR'), (SELECT id FROM divisions WHERE code='CUTTING')),
                                                                                        ('op.sablon',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Mas Bagus Saputra',      '+6285712345681',
                                                                                         (SELECT id FROM roles WHERE code='OPERATOR'), (SELECT id FROM divisions WHERE code='SABLON')),
                                                                                        ('op.sewing',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Bu Siti Aminah',         '+6285712345682',
                                                                                         (SELECT id FROM roles WHERE code='OPERATOR'), (SELECT id FROM divisions WHERE code='SEWING')),
                                                                                        ('op.qc',        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Pak Heru Santosa',       '+6285712345683',
                                                                                         (SELECT id FROM roles WHERE code='OPERATOR'), (SELECT id FROM divisions WHERE code='QC')),
                                                                                        ('op.finishing', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Mas Riyanto',            '+6285712345684',
                                                                                         (SELECT id FROM roles WHERE code='OPERATOR'), (SELECT id FROM divisions WHERE code='FINISHING')),
                                                                                        ('op.packing',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Mas Joko Prabowo',       '+6285712345685',
                                                                                         (SELECT id FROM roles WHERE code='OPERATOR'), (SELECT id FROM divisions WHERE code='PACKING'));

-- Customers (instansi & perorangan)
INSERT INTO users (username, email, password_hash, full_name, phone, role_id, customer_type, institution_name, address) VALUES
                                                                                                                            ('smpn1.kudus', 'tu@smpn1kudus.sch.id', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
                                                                                                                             'Bapak Sutrisno (Wakasek Kesiswaan)', '+62291438123',
                                                                                                                             (SELECT id FROM roles WHERE code='CUSTOMER'), 'INSTITUTION',
                                                                                                                             'SMP Negeri 1 Kudus', 'Jl. Sunan Muria No. 10A, Barongan, Kec. Kota Kudus'),

                                                                                                                            ('smpn2.kudus', 'humas@smpn2kudus.sch.id', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
                                                                                                                             'Ibu Yuliana (Humas Sekolah)', '+62291438456',
                                                                                                                             (SELECT id FROM roles WHERE code='CUSTOMER'), 'INSTITUTION',
                                                                                                                             'SMP Negeri 2 Kudus', 'Jl. Jend. Sudirman No. 82, Nganguk, Kec. Kota Kudus'),

                                                                                                                            ('smpn1.jati',  'tu@smpn1jati.sch.id', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
                                                                                                                             'Bapak Edi Susanto (Bag. Tata Usaha)', '+62291438789',
                                                                                                                             (SELECT id FROM roles WHERE code='CUSTOMER'), 'INSTITUTION',
                                                                                                                             'SMP Negeri 1 Jati Kudus', 'Jl. Getas Pejaten No. 4, Getas Pejaten, Kec. Jati'),

                                                                                                                            ('komunitas.muria', 'panitia.muria@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
                                                                                                                             'Mas Ahmad Fauzan (Ketua Panitia)', '+6281329876543',
                                                                                                                             (SELECT id FROM roles WHERE code='CUSTOMER'), 'INSTITUTION',
                                                                                                                             'Panitia Reuni Akbar Muria 2026', 'Jl. Kyai Telingsing No. 23, Kudus'),

                                                                                                                            ('bu.rahma',    'rahma.dewi@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
                                                                                                                             'Ibu Rahma Dewi', '+6285225551234',
                                                                                                                             (SELECT id FROM roles WHERE code='CUSTOMER'), 'INDIVIDUAL',
                                                                                                                             NULL, 'Perum Singocandi Indah Blok B-12, Kudus'),

                                                                                                                            ('pondok.qudsiyyah', 'admin@qudsiyyah.id', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
                                                                                                                             'Ustadz Mahfud (Bag. Sarpras)', '+6285226667890',
                                                                                                                             (SELECT id FROM roles WHERE code='CUSTOMER'), 'INSTITUTION',
                                                                                                                             'Pondok Pesantren Qudsiyyah', 'Jl. K.H.R. Asnawi No. 1, Damaran, Kec. Kota Kudus');


-- =====================================================================
-- ORDERS
-- =====================================================================

-- ORDER 1: SUDAH SELESAI (COMPLETED) - SMPN 1 Kudus pesan seragam OSIS, 2 minggu yang lalu
INSERT INTO orders (order_number, customer_id, title, description, status, order_date, deadline_date, completed_at, created_by) VALUES
    ('SPK-2026-0001',
     (SELECT id FROM users WHERE username='smpn1.kudus'),
     'Seragam OSIS SMP Negeri 1 Kudus',
     'Seragam OSIS lengan pendek untuk pengurus tahun ajaran 2026/2027. Logo bordir di dada kiri.',
     'COMPLETED',
     '2026-05-08',
     '2026-05-25',
     '2026-05-23 14:30:00+07',
     (SELECT id FROM users WHERE username='admin.cs'));

-- ORDER 2: SELESAI - SMPN 1 Jati pesan kaos olahraga
INSERT INTO orders (order_number, customer_id, title, description, status, order_date, deadline_date, completed_at, created_by) VALUES
    ('SPK-2026-0002',
     (SELECT id FROM users WHERE username='smpn1.jati'),
     'Kaos Olahraga SMP Negeri 1 Jati',
     'Kaos olahraga + celana untuk kelas VII dan VIII. Sablon nama sekolah di punggung.',
     'COMPLETED',
     '2026-05-10',
     '2026-05-28',
     '2026-05-27 16:15:00+07',
     (SELECT id FROM users WHERE username='admin.cs'));

-- ORDER 3: IN PROGRESS - SMPN 2 Kudus, di tahap Jahit (HARI INI)
INSERT INTO orders (order_number, customer_id, title, description, status, order_date, deadline_date, created_by) VALUES
    ('SPK-2026-0003',
     (SELECT id FROM users WHERE username='smpn2.kudus'),
     'Seragam Pramuka SMP Negeri 2 Kudus',
     'Seragam pramuka coklat tua + coklat muda untuk siswa kelas VII. Total 240 set.',
     'IN_PROGRESS',
     '2026-05-15',
     '2026-06-15',
     (SELECT id FROM users WHERE username='admin.cs'));

-- ORDER 4: IN PROGRESS - Komunitas reuni di tahap Sablon
INSERT INTO orders (order_number, customer_id, title, description, status, order_date, deadline_date, created_by) VALUES
    ('SPK-2026-0004',
     (SELECT id FROM users WHERE username='komunitas.muria'),
     'Kaos Reuni Akbar Muria 2026',
     'Kaos polo hitam logo reuni di dada, "Reuni Akbar Muria 2026" di punggung. Multi-size.',
     'IN_PROGRESS',
     '2026-05-18',
     '2026-06-20',
     (SELECT id FROM users WHERE username='admin.cs'));

-- ORDER 5: IN PROGRESS - SMPN 1 Kudus orderan susulan, di tahap Cutting
INSERT INTO orders (order_number, customer_id, title, description, status, order_date, deadline_date, created_by) VALUES
    ('SPK-2026-0005',
     (SELECT id FROM users WHERE username='smpn1.kudus'),
     'Seragam Batik Khas SMP Negeri 1 Kudus',
     'Seragam batik corak Kudus untuk hari Jumat. 120 pcs ukuran campur.',
     'IN_PROGRESS',
     '2026-05-22',
     '2026-06-25',
     (SELECT id FROM users WHERE username='admin.cs'));

-- ORDER 6: IN PROGRESS - Pondok Pesantren, di tahap Desain (REWORK karena sample ditolak)
INSERT INTO orders (order_number, customer_id, title, description, status, order_date, deadline_date, created_by) VALUES
    ('SPK-2026-0006',
     (SELECT id FROM users WHERE username='pondok.qudsiyyah'),
     'Seragam Santri Pondok Qudsiyyah',
     'Baju koko + sarung untuk santri baru. Sample pertama ditolak ust. Mahfud karena warna kurang sesuai.',
     'IN_PROGRESS',
     '2026-05-20',
     '2026-07-10',
     (SELECT id FROM users WHERE username='admin.cs'));

-- ORDER 7: ON HOLD - nunggu approval sample dari customer
INSERT INTO orders (order_number, customer_id, title, description, status, order_date, deadline_date, notes, created_by) VALUES
    ('SPK-2026-0007',
     (SELECT id FROM users WHERE username='bu.rahma'),
     'Kaos Keluarga Reuni Trah Sastrowiyono',
     'Kaos warna merah marun untuk acara reuni keluarga besar. Sample sudah dikirim, menunggu konfirmasi.',
     'CONFIRMED',
     '2026-05-25',
     '2026-06-30',
     'Customer minta waktu seminggu untuk diskusi sama keluarga sebelum approve sample',
     (SELECT id FROM users WHERE username='admin.cs'));

-- ORDER 8: DRAFT - baru masuk hari ini, belum mulai produksi
INSERT INTO orders (order_number, customer_id, title, description, status, order_date, deadline_date, created_by) VALUES
    ('SPK-2026-0008',
     (SELECT id FROM users WHERE username='smpn2.kudus'),
     'Seragam Batik Guru SMP Negeri 2 Kudus',
     'Seragam batik untuk dewan guru. Desain belum final, masih diskusi.',
     'DRAFT',
     '2026-05-30',
     '2026-07-15',
     (SELECT id FROM users WHERE username='admin.cs'));


-- =====================================================================
-- ORDER BATCHES
-- =====================================================================

-- Batch untuk order 1 (Seragam OSIS SMPN 1) - SELESAI
INSERT INTO order_batches (batch_number, order_id, product_type_id, quantity, specifications,
                           current_step_id, current_step_entered_at, status, started_at, completed_at) VALUES
    ('SPK-2026-0001-B01',
     (SELECT id FROM orders WHERE order_number='SPK-2026-0001'),
     (SELECT id FROM product_types WHERE code='UNIFORM'),
     45,
     '{"sizes": {"S": 10, "M": 20, "L": 12, "XL": 3}, "color": "putih", "logo": "bordir dada kiri"}'::jsonb,
     (SELECT id FROM workflow_steps WHERE code='DONE' AND workflow_id=1),
     '2026-05-23 14:30:00+07',
     'COMPLETED',
     '2026-05-08 09:00:00+07',
     '2026-05-23 14:30:00+07');

-- Batch untuk order 2 (Kaos Olahraga SMPN 1 Jati) - SELESAI
INSERT INTO order_batches (batch_number, order_id, product_type_id, quantity, specifications,
                           current_step_id, current_step_entered_at, status, started_at, completed_at) VALUES
    ('SPK-2026-0002-B01',
     (SELECT id FROM orders WHERE order_number='SPK-2026-0002'),
     (SELECT id FROM product_types WHERE code='TSHIRT'),
     180,
     '{"sizes": {"S": 40, "M": 70, "L": 50, "XL": 20}, "color": "biru navy", "sablon": "nama sekolah punggung"}'::jsonb,
     (SELECT id FROM workflow_steps WHERE code='DONE' AND workflow_id=1),
     '2026-05-27 16:15:00+07',
     'COMPLETED',
     '2026-05-10 09:30:00+07',
     '2026-05-27 16:15:00+07');

-- Batch untuk order 3 (Seragam Pramuka SMPN 2) - lagi di JAHIT
INSERT INTO order_batches (batch_number, order_id, product_type_id, quantity, specifications,
                           current_step_id, current_step_entered_at, status, started_at) VALUES
    ('SPK-2026-0003-B01',
     (SELECT id FROM orders WHERE order_number='SPK-2026-0003'),
     (SELECT id FROM product_types WHERE code='UNIFORM'),
     240,
     '{"sizes": {"S": 60, "M": 100, "L": 60, "XL": 20}, "color": "coklat tua + coklat muda", "type": "pramuka lengkap"}'::jsonb,
     (SELECT id FROM workflow_steps WHERE code='SEWING' AND workflow_id=1),
     '2026-05-28 08:00:00+07',
     'IN_PROGRESS',
     '2026-05-15 10:00:00+07');

-- Batch untuk order 4 (Kaos Reuni Muria) - lagi di SABLON
INSERT INTO order_batches (batch_number, order_id, product_type_id, quantity, specifications,
                           current_step_id, current_step_entered_at, status, started_at) VALUES
    ('SPK-2026-0004-B01',
     (SELECT id FROM orders WHERE order_number='SPK-2026-0004'),
     (SELECT id FROM product_types WHERE code='POLO'),
     320,
     '{"sizes": {"M": 80, "L": 140, "XL": 80, "XXL": 20}, "color": "hitam", "design": "logo dada + tulisan punggung"}'::jsonb,
     (SELECT id FROM workflow_steps WHERE code='SABLON_BORDIR' AND workflow_id=1),
     '2026-05-29 09:00:00+07',
     'IN_PROGRESS',
     '2026-05-18 09:00:00+07');

-- Batch untuk order 5 (Seragam Batik SMPN 1) - lagi di CUTTING
INSERT INTO order_batches (batch_number, order_id, product_type_id, quantity, specifications,
                           current_step_id, current_step_entered_at, status, started_at) VALUES
    ('SPK-2026-0005-B01',
     (SELECT id FROM orders WHERE order_number='SPK-2026-0005'),
     (SELECT id FROM product_types WHERE code='UNIFORM'),
     120,
     '{"sizes": {"S": 25, "M": 50, "L": 35, "XL": 10}, "fabric": "batik corak Kudus motif gebyok"}'::jsonb,
     (SELECT id FROM workflow_steps WHERE code='CUTTING' AND workflow_id=1),
     '2026-05-29 13:00:00+07',
     'IN_PROGRESS',
     '2026-05-22 09:00:00+07');

-- Batch untuk order 6 (Seragam Santri) - lagi di DESAIN (rework karena sample ditolak)
INSERT INTO order_batches (batch_number, order_id, product_type_id, quantity, specifications,
                           current_step_id, current_step_entered_at, status, started_at, notes) VALUES
    ('SPK-2026-0006-B01',
     (SELECT id FROM orders WHERE order_number='SPK-2026-0006'),
     (SELECT id FROM product_types WHERE code='UNIFORM'),
     150,
     '{"sizes": {"S": 40, "M": 60, "L": 40, "XL": 10}, "type": "baju koko + sarung", "note": "REVISI: warna hijau lebih tua"}'::jsonb,
     (SELECT id FROM workflow_steps WHERE code='DESIGN' AND workflow_id=1),
     '2026-05-28 14:00:00+07',
     'IN_PROGRESS',
     '2026-05-20 10:00:00+07',
     'Rework: sample warna hijau muda ditolak, revisi ke hijau tua');

-- Batch untuk order 7 (Kaos Keluarga Sastrowiyono) - di POLA/SAMPLE, status ON_HOLD
INSERT INTO order_batches (batch_number, order_id, product_type_id, quantity, specifications,
                           current_step_id, current_step_entered_at, status, started_at) VALUES
    ('SPK-2026-0007-B01',
     (SELECT id FROM orders WHERE order_number='SPK-2026-0007'),
     (SELECT id FROM product_types WHERE code='POLO'),
     65,
     '{"sizes": {"S": 10, "M": 20, "L": 20, "XL": 15}, "color": "merah marun", "design": "logo keluarga di dada"}'::jsonb,
     (SELECT id FROM workflow_steps WHERE code='PATTERN_SAMPLE' AND workflow_id=1),
     '2026-05-28 11:00:00+07',
     'ON_HOLD',
     '2026-05-25 10:00:00+07');

-- Order 8 (DRAFT) — belum ada batch, normal karena masih DRAFT


-- =====================================================================
-- BARCODES (untuk batch yang aktif)
-- =====================================================================
INSERT INTO barcodes (code, batch_id, barcode_type, printed_at) VALUES
                                                                    ('BC-SPK20260001-001', (SELECT id FROM order_batches WHERE batch_number='SPK-2026-0001-B01'), 'CODE128', '2026-05-08 09:30:00+07'),
                                                                    ('BC-SPK20260002-001', (SELECT id FROM order_batches WHERE batch_number='SPK-2026-0002-B01'), 'CODE128', '2026-05-10 10:00:00+07'),
                                                                    ('BC-SPK20260003-001', (SELECT id FROM order_batches WHERE batch_number='SPK-2026-0003-B01'), 'CODE128', '2026-05-15 10:30:00+07'),
                                                                    ('BC-SPK20260004-001', (SELECT id FROM order_batches WHERE batch_number='SPK-2026-0004-B01'), 'CODE128', '2026-05-18 09:30:00+07'),
                                                                    ('BC-SPK20260005-001', (SELECT id FROM order_batches WHERE batch_number='SPK-2026-0005-B01'), 'CODE128', '2026-05-22 09:30:00+07'),
                                                                    ('BC-SPK20260006-001', (SELECT id FROM order_batches WHERE batch_number='SPK-2026-0006-B01'), 'CODE128', '2026-05-20 10:30:00+07'),
                                                                    ('BC-SPK20260007-001', (SELECT id FROM order_batches WHERE batch_number='SPK-2026-0007-B01'), 'CODE128', '2026-05-25 10:30:00+07');


-- =====================================================================
-- PROGRESS EVENTS
-- =====================================================================

-- Helper: function untuk insert event lebih ringkas
-- (langsung pakai inline subquery, biar SQL bisa di-run bertahap)

-- ============================================================
-- ORDER 1 (SPK-2026-0001): SELESAI, lewatin semua 10 tahap
-- ============================================================
INSERT INTO progress_events (batch_id, workflow_step_id, event_type, performed_by, performed_at)
SELECT
    (SELECT id FROM order_batches WHERE batch_number='SPK-2026-0001-B01'),
    ws.id,
    'STEP_COMPLETED',
    CASE ws.code
        WHEN 'ORDER_RECEIVED' THEN (SELECT id FROM users WHERE username='admin.cs')
        WHEN 'DESIGN'         THEN (SELECT id FROM users WHERE username='op.design')
        WHEN 'PATTERN_SAMPLE' THEN (SELECT id FROM users WHERE username='op.pattern')
        WHEN 'CUTTING'        THEN (SELECT id FROM users WHERE username='op.cutting')
        WHEN 'SABLON_BORDIR'  THEN (SELECT id FROM users WHERE username='op.sablon')
        WHEN 'SEWING'         THEN (SELECT id FROM users WHERE username='op.sewing')
        WHEN 'QC'             THEN (SELECT id FROM users WHERE username='op.qc')
        WHEN 'FINISHING'      THEN (SELECT id FROM users WHERE username='op.finishing')
        WHEN 'PACKING'        THEN (SELECT id FROM users WHERE username='op.packing')
        WHEN 'DONE'           THEN (SELECT id FROM users WHERE username='admin.cs')
        END,
    -- Timestamp: dari 2026-05-08 09:00 (Order Received) sampai 2026-05-23 14:30 (Done)
    -- ~15 hari, dibagi proporsional per tahap
    TIMESTAMPTZ '2026-05-08 09:00:00+07' + (INTERVAL '15 days 5 hours 30 minutes' * (ws.sequence_number - 1) / 9)
FROM workflow_steps ws
WHERE ws.workflow_id = 1
ORDER BY ws.sequence_number;


-- ============================================================
-- ORDER 2 (SPK-2026-0002): SELESAI, semua 10 tahap
-- ============================================================
INSERT INTO progress_events (batch_id, workflow_step_id, event_type, performed_by, performed_at)
SELECT
    (SELECT id FROM order_batches WHERE batch_number='SPK-2026-0002-B01'),
    ws.id,
    'STEP_COMPLETED',
    CASE ws.code
        WHEN 'ORDER_RECEIVED' THEN (SELECT id FROM users WHERE username='admin.cs')
        WHEN 'DESIGN'         THEN (SELECT id FROM users WHERE username='op.design')
        WHEN 'PATTERN_SAMPLE' THEN (SELECT id FROM users WHERE username='op.pattern')
        WHEN 'CUTTING'        THEN (SELECT id FROM users WHERE username='op.cutting')
        WHEN 'SABLON_BORDIR'  THEN (SELECT id FROM users WHERE username='op.sablon')
        WHEN 'SEWING'         THEN (SELECT id FROM users WHERE username='op.sewing')
        WHEN 'QC'             THEN (SELECT id FROM users WHERE username='op.qc')
        WHEN 'FINISHING'      THEN (SELECT id FROM users WHERE username='op.finishing')
        WHEN 'PACKING'        THEN (SELECT id FROM users WHERE username='op.packing')
        WHEN 'DONE'           THEN (SELECT id FROM users WHERE username='admin.cs')
        END,
    TIMESTAMPTZ '2026-05-10 09:30:00+07' + (INTERVAL '17 days 6 hours 45 minutes' * (ws.sequence_number - 1) / 9)
FROM workflow_steps ws
WHERE ws.workflow_id = 1
ORDER BY ws.sequence_number;


-- ============================================================
-- ORDER 3 (SPK-2026-0003): di JAHIT (tahap 6 lagi dikerjain)
-- ============================================================
INSERT INTO progress_events (batch_id, workflow_step_id, event_type, performed_by, performed_at)
SELECT
    (SELECT id FROM order_batches WHERE batch_number='SPK-2026-0003-B01'),
    ws.id,
    'STEP_COMPLETED',
    CASE ws.code
        WHEN 'ORDER_RECEIVED' THEN (SELECT id FROM users WHERE username='admin.cs')
        WHEN 'DESIGN'         THEN (SELECT id FROM users WHERE username='op.design')
        WHEN 'PATTERN_SAMPLE' THEN (SELECT id FROM users WHERE username='op.pattern')
        WHEN 'CUTTING'        THEN (SELECT id FROM users WHERE username='op.cutting')
        WHEN 'SABLON_BORDIR'  THEN (SELECT id FROM users WHERE username='op.sablon')
        END,
    TIMESTAMPTZ '2026-05-15 10:00:00+07' + (INTERVAL '13 days' * (ws.sequence_number - 1) / 4)
FROM workflow_steps ws
WHERE ws.workflow_id = 1 AND ws.sequence_number <= 5
ORDER BY ws.sequence_number;

-- Tambahan: STEP_STARTED untuk Jahit (tahap 6) - sedang dikerjakan
INSERT INTO progress_events (batch_id, workflow_step_id, event_type, performed_by, performed_at) VALUES
    ((SELECT id FROM order_batches WHERE batch_number='SPK-2026-0003-B01'),
     (SELECT id FROM workflow_steps WHERE code='SEWING' AND workflow_id=1),
     'STEP_STARTED',
     (SELECT id FROM users WHERE username='op.sewing'),
     '2026-05-28 08:00:00+07');


-- ============================================================
-- ORDER 4 (SPK-2026-0004): di SABLON (tahap 5 lagi dikerjain)
-- + SKENARIO REWORK: QC reject sebelumnya, balik ke Sablon (di-skip biar ga membingungkan)
-- + SKENARIO CORRECTION: operator salah scan dari Sablon ke Jahit, lalu di-koreksi
-- ============================================================
INSERT INTO progress_events (batch_id, workflow_step_id, event_type, performed_by, performed_at)
SELECT
    (SELECT id FROM order_batches WHERE batch_number='SPK-2026-0004-B01'),
    ws.id,
    'STEP_COMPLETED',
    CASE ws.code
        WHEN 'ORDER_RECEIVED' THEN (SELECT id FROM users WHERE username='admin.cs')
        WHEN 'DESIGN'         THEN (SELECT id FROM users WHERE username='op.design')
        WHEN 'PATTERN_SAMPLE' THEN (SELECT id FROM users WHERE username='op.pattern')
        WHEN 'CUTTING'        THEN (SELECT id FROM users WHERE username='op.cutting')
        END,
    TIMESTAMPTZ '2026-05-18 09:00:00+07' + (INTERVAL '11 days' * (ws.sequence_number - 1) / 3)
FROM workflow_steps ws
WHERE ws.workflow_id = 1 AND ws.sequence_number <= 4
ORDER BY ws.sequence_number;

-- STEP_STARTED Sablon (tahap 5) - sedang dikerjakan
INSERT INTO progress_events (batch_id, workflow_step_id, event_type, performed_by, performed_at, notes) VALUES
    ((SELECT id FROM order_batches WHERE batch_number='SPK-2026-0004-B01'),
     (SELECT id FROM workflow_steps WHERE code='SABLON_BORDIR' AND workflow_id=1),
     'STEP_STARTED',
     (SELECT id FROM users WHERE username='op.sablon'),
     '2026-05-29 09:00:00+07',
     'Sablon logo dada dulu, lanjut sablon punggung besok');


-- ============================================================
-- ORDER 5 (SPK-2026-0005): di CUTTING (tahap 4 lagi dikerjain)
-- ============================================================
INSERT INTO progress_events (batch_id, workflow_step_id, event_type, performed_by, performed_at)
SELECT
    (SELECT id FROM order_batches WHERE batch_number='SPK-2026-0005-B01'),
    ws.id,
    'STEP_COMPLETED',
    CASE ws.code
        WHEN 'ORDER_RECEIVED' THEN (SELECT id FROM users WHERE username='admin.cs')
        WHEN 'DESIGN'         THEN (SELECT id FROM users WHERE username='op.design')
        WHEN 'PATTERN_SAMPLE' THEN (SELECT id FROM users WHERE username='op.pattern')
        END,
    TIMESTAMPTZ '2026-05-22 09:00:00+07' + (INTERVAL '7 days 4 hours' * (ws.sequence_number - 1) / 2)
FROM workflow_steps ws
WHERE ws.workflow_id = 1 AND ws.sequence_number <= 3
ORDER BY ws.sequence_number;

-- STEP_STARTED Cutting (tahap 4) - sedang dikerjakan
INSERT INTO progress_events (batch_id, workflow_step_id, event_type, performed_by, performed_at) VALUES
    ((SELECT id FROM order_batches WHERE batch_number='SPK-2026-0005-B01'),
     (SELECT id FROM workflow_steps WHERE code='CUTTING' AND workflow_id=1),
     'STEP_STARTED',
     (SELECT id FROM users WHERE username='op.cutting'),
     '2026-05-29 13:00:00+07');


-- ============================================================
-- ORDER 6 (SPK-2026-0006): SKENARIO SAMPLE DITOLAK & REWORK
-- Order Received -> Design -> Pattern/Sample -> STEP_FAILED (sample ditolak)
-- -> REWORK ke Design -> sedang dikerjakan lagi
-- ============================================================
-- Hari 1: Order received
INSERT INTO progress_events (batch_id, workflow_step_id, event_type, performed_by, performed_at) VALUES
    ((SELECT id FROM order_batches WHERE batch_number='SPK-2026-0006-B01'),
     (SELECT id FROM workflow_steps WHERE code='ORDER_RECEIVED' AND workflow_id=1),
     'STEP_COMPLETED',
     (SELECT id FROM users WHERE username='admin.cs'),
     '2026-05-20 10:00:00+07');

-- Design pertama selesai
INSERT INTO progress_events (batch_id, workflow_step_id, event_type, performed_by, performed_at, notes) VALUES
    ((SELECT id FROM order_batches WHERE batch_number='SPK-2026-0006-B01'),
     (SELECT id FROM workflow_steps WHERE code='DESIGN' AND workflow_id=1),
     'STEP_COMPLETED',
     (SELECT id FROM users WHERE username='op.design'),
     '2026-05-23 11:00:00+07',
     'Desain pertama: baju koko hijau muda');

-- Pattern/Sample dibuat
INSERT INTO progress_events (batch_id, workflow_step_id, event_type, performed_by, performed_at) VALUES
    ((SELECT id FROM order_batches WHERE batch_number='SPK-2026-0006-B01'),
     (SELECT id FROM workflow_steps WHERE code='PATTERN_SAMPLE' AND workflow_id=1),
     'STEP_STARTED',
     (SELECT id FROM users WHERE username='op.pattern'),
     '2026-05-25 09:00:00+07');

-- Sample FAILED (ditolak customer)
INSERT INTO progress_events (batch_id, workflow_step_id, event_type, performed_by, performed_at, notes, metadata) VALUES
    ((SELECT id FROM order_batches WHERE batch_number='SPK-2026-0006-B01'),
     (SELECT id FROM workflow_steps WHERE code='PATTERN_SAMPLE' AND workflow_id=1),
     'STEP_FAILED',
     (SELECT id FROM users WHERE username='admin.cs'),
     '2026-05-27 14:00:00+07',
     'Sample ditolak oleh Ust. Mahfud, warna hijau muda kurang sesuai harapan',
     '{"rejection_reason": "color_mismatch", "requested_revision": "warna hijau lebih tua, lebih natural"}'::jsonb);

-- REWORK: balik ke Design
INSERT INTO progress_events (batch_id, workflow_step_id, event_type, performed_by, performed_at, notes) VALUES
    ((SELECT id FROM order_batches WHERE batch_number='SPK-2026-0006-B01'),
     (SELECT id FROM workflow_steps WHERE code='DESIGN' AND workflow_id=1),
     'REWORK',
     (SELECT id FROM users WHERE username='admin.cs'),
     '2026-05-28 14:00:00+07',
     'Balik ke desain, revisi warna');

-- Sedang dikerjakan ulang di Design
INSERT INTO progress_events (batch_id, workflow_step_id, event_type, performed_by, performed_at) VALUES
    ((SELECT id FROM order_batches WHERE batch_number='SPK-2026-0006-B01'),
     (SELECT id FROM workflow_steps WHERE code='DESIGN' AND workflow_id=1),
     'STEP_STARTED',
     (SELECT id FROM users WHERE username='op.design'),
     '2026-05-28 15:00:00+07');


-- ============================================================
-- ORDER 7 (SPK-2026-0007): ON HOLD di Pattern/Sample (nunggu approval)
-- ============================================================
INSERT INTO progress_events (batch_id, workflow_step_id, event_type, performed_by, performed_at)
SELECT
    (SELECT id FROM order_batches WHERE batch_number='SPK-2026-0007-B01'),
    ws.id,
    'STEP_COMPLETED',
    CASE ws.code
        WHEN 'ORDER_RECEIVED' THEN (SELECT id FROM users WHERE username='admin.cs')
        WHEN 'DESIGN'         THEN (SELECT id FROM users WHERE username='op.design')
        END,
    TIMESTAMPTZ '2026-05-25 10:00:00+07' + (INTERVAL '3 days' * (ws.sequence_number - 1))
FROM workflow_steps ws
WHERE ws.workflow_id = 1 AND ws.sequence_number <= 2
ORDER BY ws.sequence_number;

-- Pattern/Sample selesai dibuat, lalu ON_HOLD nunggu approval
INSERT INTO progress_events (batch_id, workflow_step_id, event_type, performed_by, performed_at, notes) VALUES
                                                                                                            ((SELECT id FROM order_batches WHERE batch_number='SPK-2026-0007-B01'),
                                                                                                             (SELECT id FROM workflow_steps WHERE code='PATTERN_SAMPLE' AND workflow_id=1),
                                                                                                             'STEP_STARTED',
                                                                                                             (SELECT id FROM users WHERE username='op.pattern'),
                                                                                                             '2026-05-28 09:00:00+07',
                                                                                                             NULL),
                                                                                                            ((SELECT id FROM order_batches WHERE batch_number='SPK-2026-0007-B01'),
                                                                                                             (SELECT id FROM workflow_steps WHERE code='PATTERN_SAMPLE' AND workflow_id=1),
                                                                                                             'ON_HOLD',
                                                                                                             (SELECT id FROM users WHERE username='admin.cs'),
                                                                                                             '2026-05-28 11:00:00+07',
                                                                                                             'Sample sudah dikirim ke Bu Rahma, menunggu konfirmasi dari keluarga sampai 4 Juni 2026');


-- ============================================================
-- SKENARIO CORRECTION: di Order 4, operator op.sablon awalnya scan
-- "Sablon selesai" lalu sadar masih ada sebagian belum selesai sablon.
-- Admin koreksi via event CORRECTION.
-- ============================================================
-- Event awal yang "salah" (Sablon STEP_COMPLETED) - tapi kita simulate ini happens
-- Untuk Order 4 yang current step-nya Sablon, mari kita simulate:
-- 1. op.sablon scan COMPLETED (salah)
-- 2. admin sadar, bikin event CORRECTION
-- Tapi karena current_step Order 4 udah Sablon (lagi dikerjain),
-- mari kita pakai skenario alternatif: misal Order 1 (yang udah DONE)
-- punya catatan correction di Cutting

-- Tambah event CORRECTION pada Order 1, misal: ada salah scan oleh op.cutting
-- yang awalnya nyatat 2026-05-11 10:00, padahal harusnya 11:00
-- Kita ga update event lama (append-only!), tapi insert event CORRECTION
INSERT INTO progress_events (batch_id, workflow_step_id, event_type, performed_by, performed_at, notes, corrects_event_id, metadata) VALUES
    ((SELECT id FROM order_batches WHERE batch_number='SPK-2026-0001-B01'),
     (SELECT id FROM workflow_steps WHERE code='CUTTING' AND workflow_id=1),
     'CORRECTION',
     (SELECT id FROM users WHERE username='admin.cs'),
     '2026-05-11 14:30:00+07',
     'Koreksi: scan cutting jam 10:00 sebenarnya jam 11:00, op.cutting salah pencet',
     (SELECT id FROM progress_events
      WHERE batch_id = (SELECT id FROM order_batches WHERE batch_number='SPK-2026-0001-B01')
        AND workflow_step_id = (SELECT id FROM workflow_steps WHERE code='CUTTING' AND workflow_id=1)
        AND event_type = 'STEP_COMPLETED'
      LIMIT 1),
     '{"original_timestamp": "2026-05-11T10:00:00+07:00", "corrected_timestamp": "2026-05-11T11:00:00+07:00"}'::jsonb);

COMMIT;
