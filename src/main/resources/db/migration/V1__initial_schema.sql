-- =====================================================================
-- Sistem Tracking Produksi Sevenspeed - Skema PostgreSQL
-- Versi: 0.1 (POC)
-- Tanggal: Mei 2026
--
-- Konvensi:
--   - snake_case, plural untuk nama tabel
--   - BIGSERIAL untuk PK
--   - TIMESTAMPTZ untuk semua timestamp (timezone-aware)
--   - CHECK constraint untuk enum (bukan PostgreSQL ENUM type)
--   - Soft delete via deleted_at (nullable)
--   - Audit: created_at, updated_at, created_by (kalau relevan)
-- =====================================================================

-- =====================================================================
-- 1. roles
-- Definisi role: admin, operator, customer
-- Pakai tabel (bukan enum) biar fleksibel kalau nanti ada role baru
-- =====================================================================
CREATE TABLE roles (
                       id          BIGSERIAL PRIMARY KEY,
                       code        VARCHAR(50) NOT NULL UNIQUE,    -- 'ADMIN', 'OPERATOR', 'CUSTOMER'
                       name        VARCHAR(100) NOT NULL,           -- 'Administrator', 'Operator Divisi', dll
                       description TEXT,
                       created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE roles IS 'Definisi role user dalam sistem';


-- =====================================================================
-- 2. divisions
-- Divisi produksi: Desain, Cutting, Sablon, Jahit, QC, Finishing, Packing, dll
-- =====================================================================
CREATE TABLE divisions (
                           id          BIGSERIAL PRIMARY KEY,
                           code        VARCHAR(50) NOT NULL UNIQUE,    -- 'DESIGN', 'CUTTING', 'SABLON', dst
                           name        VARCHAR(100) NOT NULL,           -- 'Divisi Desain', 'Divisi Cutting'
                           description TEXT,
                           is_active   BOOLEAN NOT NULL DEFAULT TRUE,
                           created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                           updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                           deleted_at  TIMESTAMPTZ
);

COMMENT ON TABLE divisions IS 'Divisi produksi di studio. Tiap workflow_step di-assign ke salah satu divisi';


-- =====================================================================
-- 3. users
-- Semua orang yang login: admin, operator, customer
-- Customer profile digabung di sini (sesuai keputusan POC)
-- =====================================================================
CREATE TABLE users (
                       id              BIGSERIAL PRIMARY KEY,
                       username        VARCHAR(100) NOT NULL UNIQUE,
                       email           VARCHAR(255) UNIQUE,             -- nullable: operator bisa ga punya email
                       password_hash   VARCHAR(255) NOT NULL,           -- bcrypt hash
                       full_name       VARCHAR(200) NOT NULL,
                       phone           VARCHAR(30),
                       role_id         BIGINT NOT NULL REFERENCES roles(id),
                       division_id     BIGINT REFERENCES divisions(id), -- nullable: hanya operator yang punya divisi

    -- Customer-specific fields (nullable, hanya terisi kalau role = CUSTOMER)
                       customer_type   VARCHAR(20) CHECK (customer_type IN ('INDIVIDUAL', 'INSTITUTION')),
                       institution_name VARCHAR(255),                   -- nama instansi kalau customer_type = INSTITUTION
                       address         TEXT,

                       is_active       BOOLEAN NOT NULL DEFAULT TRUE,
                       last_login_at   TIMESTAMPTZ,
                       created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       deleted_at      TIMESTAMPTZ
);

CREATE INDEX idx_users_role ON users(role_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_division ON users(division_id) WHERE deleted_at IS NULL AND division_id IS NOT NULL;

COMMENT ON TABLE users IS 'Semua user sistem (admin/operator/customer). Customer profile digabung di sini di POC';
COMMENT ON COLUMN users.division_id IS 'Hanya untuk operator. Nullable untuk admin & customer';
COMMENT ON COLUMN users.customer_type IS 'Hanya terisi kalau role = CUSTOMER';


-- =====================================================================
-- 4. workflows
-- Definisi alur produksi. Tiap product type punya workflow-nya.
-- =====================================================================
CREATE TABLE workflows (
                           id          BIGSERIAL PRIMARY KEY,
                           code        VARCHAR(50) NOT NULL UNIQUE,    -- 'UNIFORM_STANDARD', 'CALENDAR_STANDARD'
                           name        VARCHAR(150) NOT NULL,
                           description TEXT,
                           is_active   BOOLEAN NOT NULL DEFAULT TRUE,
                           created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                           updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                           deleted_at  TIMESTAMPTZ
);

COMMENT ON TABLE workflows IS 'Definisi alur produksi. Satu workflow bisa dipakai banyak product type';


-- =====================================================================
-- 5. workflow_steps
-- Tahap-tahap dalam workflow. Sequence_number nentuin urutan.
-- =====================================================================
CREATE TABLE workflow_steps (
                                id              BIGSERIAL PRIMARY KEY,
                                workflow_id     BIGINT NOT NULL REFERENCES workflows(id),
                                division_id     BIGINT NOT NULL REFERENCES divisions(id),
                                sequence_number INT NOT NULL,                    -- urutan: 1, 2, 3, ...
                                code            VARCHAR(50) NOT NULL,            -- 'ORDER_RECEIVED', 'DESIGN', 'CUTTING'
                                name            VARCHAR(150) NOT NULL,           -- 'Order Diterima', 'Desain & Approval'
                                description     TEXT,
                                is_final        BOOLEAN NOT NULL DEFAULT FALSE,  -- tahap akhir (Done/Delivery)
                                is_checkpoint   BOOLEAN NOT NULL DEFAULT FALSE,  -- tahap yang butuh approval (mis. Sample)
                                created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- Sequence harus unique per workflow
                                CONSTRAINT uq_workflow_sequence UNIQUE (workflow_id, sequence_number),
    -- Code juga unique per workflow
                                CONSTRAINT uq_workflow_step_code UNIQUE (workflow_id, code)
);

CREATE INDEX idx_workflow_steps_workflow ON workflow_steps(workflow_id, sequence_number);

COMMENT ON TABLE workflow_steps IS 'Tahap dalam workflow. sequence_number nentuin urutan, is_final menandai tahap akhir';
COMMENT ON COLUMN workflow_steps.is_checkpoint IS 'TRUE kalau tahap ini butuh approval customer (mis. Sample)';


-- =====================================================================
-- 6. product_types
-- Jenis produk: Seragam, Kaos Polo, Kalender, Tas, dll
-- =====================================================================
CREATE TABLE product_types (
                               id          BIGSERIAL PRIMARY KEY,
                               code        VARCHAR(50) NOT NULL UNIQUE,    -- 'UNIFORM', 'POLO', 'CALENDAR', 'BAG'
                               name        VARCHAR(150) NOT NULL,
                               workflow_id BIGINT NOT NULL REFERENCES workflows(id),
                               description TEXT,
                               is_active   BOOLEAN NOT NULL DEFAULT TRUE,
                               created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                               updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                               deleted_at  TIMESTAMPTZ
);

CREATE INDEX idx_product_types_workflow ON product_types(workflow_id);

COMMENT ON TABLE product_types IS 'Jenis produk. Tiap product_type punya workflow yang nentuin alur produksinya';


-- =====================================================================
-- 7. orders
-- Order/SPK dari customer. Container untuk batch.
-- =====================================================================
CREATE TABLE orders (
                        id              BIGSERIAL PRIMARY KEY,
                        order_number    VARCHAR(50) NOT NULL UNIQUE,    -- nomor SPK, mis. 'SPK-2026-0001'
                        customer_id     BIGINT NOT NULL REFERENCES users(id),
                        title           VARCHAR(255) NOT NULL,           -- 'Seragam OSIS SMP 2 Kudus 120pcs'
                        description     TEXT,

    -- Lifecycle status order (beda dengan batch status)
                        status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
                            CHECK (status IN ('DRAFT', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),

                        order_date      DATE NOT NULL DEFAULT CURRENT_DATE,
                        deadline_date   DATE,
                        completed_at    TIMESTAMPTZ,

                        notes           TEXT,
                        created_by      BIGINT NOT NULL REFERENCES users(id),   -- admin yang input order
                        created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        deleted_at      TIMESTAMPTZ
);

CREATE INDEX idx_orders_customer ON orders(customer_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_orders_status ON orders(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_orders_deadline ON orders(deadline_date) WHERE deleted_at IS NULL AND status != 'COMPLETED';

COMMENT ON TABLE orders IS 'Order/SPK dari customer. Satu order bisa berisi banyak batch';


-- =====================================================================
-- 8. order_batches
-- Unit yang di-track. Satu order = satu atau lebih batch.
-- Punya current_step_id (cached) dan status.
-- =====================================================================
CREATE TABLE order_batches (
                               id                  BIGSERIAL PRIMARY KEY,
                               batch_number        VARCHAR(50) NOT NULL UNIQUE,    -- mis. 'SPK-2026-0001-B01'
                               order_id            BIGINT NOT NULL REFERENCES orders(id),
                               product_type_id     BIGINT NOT NULL REFERENCES product_types(id),

                               quantity            INT NOT NULL CHECK (quantity > 0),
                               unit                VARCHAR(20) NOT NULL DEFAULT 'pcs',  -- pcs, set, lusin
                               specifications      JSONB,                              -- size breakdown, warna, dll (fleksibel)

    -- CACHED: current step. Di-update tiap progress_event baru.
    -- Sumber kebenaran tetep progress_events. Bisa di-recompute dari log.
                               current_step_id     BIGINT REFERENCES workflow_steps(id),
                               current_step_entered_at TIMESTAMPTZ,                   -- kapan masuk step current ini

    -- Lifecycle status batch (beda dengan current_step)
                               status              VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
                                   CHECK (status IN ('DRAFT', 'IN_PROGRESS', 'ON_HOLD', 'COMPLETED', 'CANCELLED')),

                               started_at          TIMESTAMPTZ,                       -- saat pertama kali masuk produksi
                               completed_at        TIMESTAMPTZ,                       -- saat selesai (status COMPLETED)

                               notes               TEXT,
                               created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                               updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                               deleted_at          TIMESTAMPTZ
);

-- Index buat operator divisi: "batch apa aja yang di divisi gw"
CREATE INDEX idx_batches_current_step ON order_batches(current_step_id)
    WHERE deleted_at IS NULL AND status = 'IN_PROGRESS';

CREATE INDEX idx_batches_order ON order_batches(order_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_batches_status ON order_batches(status) WHERE deleted_at IS NULL;

COMMENT ON TABLE order_batches IS 'Unit yang di-track progress-nya. current_step_id adalah cache dari progress_events terakhir';
COMMENT ON COLUMN order_batches.specifications IS 'JSONB fleksibel untuk size breakdown, warna, dll';
COMMENT ON COLUMN order_batches.current_step_id IS 'CACHED dari progress_events terakhir. Di-update via service layer dalam transaction yang sama';


-- =====================================================================
-- 9. barcodes
-- Barcode yang di-print untuk batch. Bisa multiple per batch.
-- Bisa di-regenerate (yang lama di-deactivate, generate baru).
-- =====================================================================
CREATE TABLE barcodes (
                          id              BIGSERIAL PRIMARY KEY,
                          code            VARCHAR(100) NOT NULL UNIQUE,   -- string yang di-encode di barcode
                          batch_id        BIGINT NOT NULL REFERENCES order_batches(id),

                          barcode_type    VARCHAR(20) NOT NULL DEFAULT 'CODE128'
                              CHECK (barcode_type IN ('CODE128', 'QR', 'CODE39')),

                          is_active       BOOLEAN NOT NULL DEFAULT TRUE,  -- kalau di-regenerate, yang lama di-set FALSE
                          printed_at      TIMESTAMPTZ,
                          notes           VARCHAR(255),                    -- mis. 'cetak ulang karena rusak'

                          created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                          updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_barcodes_batch ON barcodes(batch_id) WHERE is_active = TRUE;

COMMENT ON TABLE barcodes IS 'Barcode untuk scan. Bisa multiple per batch (cetak ulang, multi-lembar SPK)';


-- =====================================================================
-- 10. progress_events
-- APPEND-ONLY log. Sumber kebenaran histori progress.
-- TIDAK ADA UPDATE atau DELETE. Koreksi = event baru bertipe CORRECTION.
-- =====================================================================
CREATE TABLE progress_events (
                                 id                  BIGSERIAL PRIMARY KEY,
                                 batch_id            BIGINT NOT NULL REFERENCES order_batches(id),
                                 workflow_step_id    BIGINT NOT NULL REFERENCES workflow_steps(id),

                                 event_type          VARCHAR(20) NOT NULL DEFAULT 'STEP_COMPLETED'
                                     CHECK (event_type IN (
                                                           'STEP_STARTED',     -- masuk ke step ini
                                                           'STEP_COMPLETED',   -- selesai step ini
                                                           'STEP_FAILED',      -- gagal (mis. QC reject)
                                                           'REWORK',           -- mundur ke step sebelumnya
                                                           'CORRECTION',       -- koreksi event sebelumnya
                                                           'ON_HOLD',          -- pause
                                                           'RESUMED'           -- lanjut lagi setelah hold
                                         )),

                                 performed_by        BIGINT NOT NULL REFERENCES users(id),  -- siapa yang scan/update
                                 performed_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- Konteks tambahan
                                 notes               TEXT,
                                 metadata            JSONB,                      -- fleksibel: alasan reject, foto path, dll

    -- Kalau ini CORRECTION, refer ke event yang dikoreksi
                                 corrects_event_id   BIGINT REFERENCES progress_events(id),

                                 created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
    -- TIDAK ADA updated_at: append-only
);

CREATE INDEX idx_progress_events_batch ON progress_events(batch_id, performed_at);
CREATE INDEX idx_progress_events_step ON progress_events(workflow_step_id, performed_at);
CREATE INDEX idx_progress_events_performer ON progress_events(performed_by);

COMMENT ON TABLE progress_events IS 'APPEND-ONLY event log. Sumber kebenaran histori produksi. JANGAN UPDATE/DELETE';
COMMENT ON COLUMN progress_events.metadata IS 'JSONB fleksibel untuk konteks: alasan reject, foto path, dll';


-- =====================================================================
-- TRIGGER: Auto-update timestamp
-- =====================================================================
CREATE OR REPLACE FUNCTION trg_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply ke semua tabel yang punya updated_at
DO $$
DECLARE
t TEXT;
BEGIN
FOR t IN
SELECT table_name FROM information_schema.columns
WHERE table_schema = 'public'
  AND column_name = 'updated_at'
  AND table_name NOT IN ('progress_events')  -- exclude append-only
    LOOP
        EXECUTE format(
            'CREATE TRIGGER trg_%I_updated_at BEFORE UPDATE ON %I
             FOR EACH ROW EXECUTE FUNCTION trg_set_updated_at()',
            t, t
        );
END LOOP;
END $$;


-- =====================================================================
-- SEED DATA MINIMAL (yang dibutuhkan biar sistem bisa jalan)
-- =====================================================================

-- Roles
INSERT INTO roles (code, name, description) VALUES
                                                ('ADMIN', 'Administrator', 'Akses penuh ke semua fitur admin'),
                                                ('OPERATOR', 'Operator Divisi', 'Update progress di divisi masing-masing'),
                                                ('CUSTOMER', 'Customer', 'Lihat order milik sendiri');

-- Divisions (sesuai workflow 10 tahap)
INSERT INTO divisions (code, name) VALUES
                                       ('CS', 'Customer Service'),
                                       ('DESIGN', 'Divisi Desain'),
                                       ('PATTERN', 'Divisi Pola'),
                                       ('CUTTING', 'Divisi Cutting'),
                                       ('SABLON', 'Divisi Sablon / Bordir'),
                                       ('SEWING', 'Divisi Jahit'),
                                       ('QC', 'Divisi QC'),
                                       ('FINISHING', 'Divisi Finishing'),
                                       ('PACKING', 'Divisi Packing');

-- Workflow standar untuk seragam
INSERT INTO workflows (code, name, description) VALUES
    ('UNIFORM_STANDARD', 'Workflow Standar Seragam', 'Alur 10 tahap untuk produksi seragam standar');

-- Workflow steps (10 tahap)
-- Asumsi: workflow ID = 1, division IDs sesuai urutan insert
INSERT INTO workflow_steps (workflow_id, division_id, sequence_number, code, name, is_final, is_checkpoint) VALUES
                                                                                                                (1, (SELECT id FROM divisions WHERE code = 'CS'),        1,  'ORDER_RECEIVED', 'Order Masuk',         FALSE, FALSE),
                                                                                                                (1, (SELECT id FROM divisions WHERE code = 'DESIGN'),    2,  'DESIGN',         'Desain & Approval',   FALSE, TRUE),
                                                                                                                (1, (SELECT id FROM divisions WHERE code = 'PATTERN'),   3,  'PATTERN_SAMPLE', 'Pola & Sample',       FALSE, TRUE),
                                                                                                                (1, (SELECT id FROM divisions WHERE code = 'CUTTING'),   4,  'CUTTING',        'Cutting',             FALSE, FALSE),
                                                                                                                (1, (SELECT id FROM divisions WHERE code = 'SABLON'),    5,  'SABLON_BORDIR',  'Sablon / Bordir',     FALSE, FALSE),
                                                                                                                (1, (SELECT id FROM divisions WHERE code = 'SEWING'),    6,  'SEWING',         'Jahit',               FALSE, FALSE),
                                                                                                                (1, (SELECT id FROM divisions WHERE code = 'QC'),        7,  'QC',             'Quality Control',     FALSE, TRUE),
                                                                                                                (1, (SELECT id FROM divisions WHERE code = 'FINISHING'), 8,  'FINISHING',      'Finishing',           FALSE, FALSE),
                                                                                                                (1, (SELECT id FROM divisions WHERE code = 'PACKING'),   9,  'PACKING',        'Packing',             FALSE, FALSE),
                                                                                                                (1, (SELECT id FROM divisions WHERE code = 'CS'),        10, 'DONE',           'Selesai / Delivery',  TRUE,  FALSE);

-- Product type default
INSERT INTO product_types (code, name, workflow_id, description) VALUES
                                                                     ('UNIFORM', 'Seragam', 1, 'Seragam dinas, OSIS, kantor, dll'),
                                                                     ('POLO',    'Kaos Polo', 1, 'Kaos polo untuk komunitas/event'),
                                                                     ('TSHIRT',  'Kaos Sablon', 1, 'Kaos sablon umum');
-- Catatan: produk lain (kalender, tas) butuh workflow berbeda, di-add di phase 2

-- =====================================================================
-- END OF SCHEMA
-- =====================================================================