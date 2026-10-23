-- ============================================================================
-- KUOTA MIRROR SCHEMA — SUPABASE POSTGRESQL
-- Réplica secundaria controlada por el servidor desde Firebase Cloud Functions
-- Idempotencia, trazabilidad, conciliación y auditoría para MYPEs peruanas
-- ============================================================================

-- Habilitar extensión UUID si no existe
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ----------------------------------------------------------------------------
-- 1. TABLA: merchants_mirror (Perfil del negocio MYPE)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS merchants_mirror (
    merchant_id VARCHAR(128) PRIMARY KEY, -- Coincide con Firestore UID / documentId
    owner_name VARCHAR(200),
    business_name VARCHAR(200) NOT NULL,
    email VARCHAR(200),
    phone VARCHAR(20),
    ruc VARCHAR(20),
    preferred_payment_method VARCHAR(50) DEFAULT 'Yape / Plin',
    kyc_status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    replicated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_merchants_phone ON merchants_mirror(phone);
CREATE INDEX IF NOT EXISTS idx_merchants_ruc ON merchants_mirror(ruc);

-- ----------------------------------------------------------------------------
-- 2. TABLA: sales_mirror (Registro de ventas a crédito y cuotas)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sales_mirror (
    sale_id VARCHAR(128) PRIMARY KEY, -- Coincide con Firestore saleId
    merchant_id VARCHAR(128) NOT NULL REFERENCES merchants_mirror(merchant_id) ON DELETE CASCADE,
    total_amount_cents BIGINT NOT NULL CHECK (total_amount_cents >= 0),
    down_payment_cents BIGINT NOT NULL DEFAULT 0 CHECK (down_payment_cents >= 0),
    installments_count INTEGER NOT NULL DEFAULT 1 CHECK (installments_count >= 1),
    status VARCHAR(32) NOT NULL CHECK (status IN ('PENDING', 'PARTIALLY_PAID', 'PAID', 'OVERDUE', 'CANCELLED')),
    customer_name VARCHAR(200),
    customer_phone VARCHAR(20),
    customer_dni VARCHAR(20),
    currency CHAR(3) NOT NULL DEFAULT 'PEN',
    sale_created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    replicated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_sales_merchant ON sales_mirror(merchant_id);
CREATE INDEX IF NOT EXISTS idx_sales_status ON sales_mirror(status);
CREATE INDEX IF NOT EXISTS idx_sales_created ON sales_mirror(sale_created_at DESC);

-- ----------------------------------------------------------------------------
-- 3. TABLA: installments_mirror (Desglose individual de cuotas amortizables)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS installments_mirror (
    installment_id VARCHAR(160) PRIMARY KEY, -- Clave compuesta: sale_id + '_' + number
    sale_id VARCHAR(128) NOT NULL REFERENCES sales_mirror(sale_id) ON DELETE CASCADE,
    installment_number INTEGER NOT NULL CHECK (installment_number >= 1),
    amount_cents BIGINT NOT NULL CHECK (amount_cents > 0),
    due_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL CHECK (status IN ('PENDING', 'PAID', 'OVERDUE')),
    paid_at TIMESTAMPTZ,
    replicated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_installments_sale ON installments_mirror(sale_id);
CREATE INDEX IF NOT EXISTS idx_installments_due_date ON installments_mirror(due_date);

-- ----------------------------------------------------------------------------
-- 4. TABLA: claims_mirror (Libro de Reclamaciones - Ley N° 29571 INDECOPI)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS claims_mirror (
    claim_number VARCHAR(64) PRIMARY KEY, -- Formato: REC-2026-XXXXXX
    client_claim_id VARCHAR(128),
    consumer_name VARCHAR(200) NOT NULL,
    document_type VARCHAR(16) NOT NULL,
    document_number VARCHAR(32) NOT NULL,
    address TEXT,
    phone VARCHAR(20),
    email VARCHAR(200),
    good_type VARCHAR(32),
    good_description TEXT,
    amount_claimed_cents BIGINT,
    operation_reference VARCHAR(128),
    detail TEXT NOT NULL,
    consumer_request TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'RECEIVED',
    response_due_date DATE NOT NULL,
    response_text TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    replicated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_claims_doc ON claims_mirror(document_number);
CREATE INDEX IF NOT EXISTS idx_claims_created ON claims_mirror(created_at DESC);

-- ----------------------------------------------------------------------------
-- 5. TABLA: sync_outbox_log (Auditoría de sincronización, reintentos e idempotencia)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sync_outbox_log (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(128) NOT NULL,
    entity_type VARCHAR(64) NOT NULL, -- 'MERCHANT', 'SALE', 'INSTALLMENT', 'CLAIM'
    entity_id VARCHAR(128) NOT NULL,
    operation VARCHAR(16) NOT NULL, -- 'UPSERT', 'DELETE'
    status VARCHAR(16) NOT NULL CHECK (status IN ('SUCCESS', 'FAILED', 'RETRY')),
    attempt_count INTEGER NOT NULL DEFAULT 1,
    error_message TEXT,
    payload_digest CHAR(64), -- SHA-256 del contenido para detectar cambios reales
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_sync_log_entity ON sync_outbox_log(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_sync_log_created ON sync_outbox_log(created_at DESC);

-- ----------------------------------------------------------------------------
-- 6. POLÍTICAS DE SEGURIDAD (ROW LEVEL SECURITY - RLS)
-- ----------------------------------------------------------------------------
ALTER TABLE merchants_mirror ENABLE ROW LEVEL SECURITY;
ALTER TABLE sales_mirror ENABLE ROW LEVEL SECURITY;
ALTER TABLE installments_mirror ENABLE ROW LEVEL SECURITY;
ALTER TABLE claims_mirror ENABLE ROW LEVEL SECURITY;
ALTER TABLE sync_outbox_log ENABLE ROW LEVEL SECURITY;

-- Por defecto: Denegar acceso directo desde el cliente anónimo público.
-- Únicamente el backend autenticado (service_role) tiene permiso completo de inserción/actualización.
CREATE POLICY "service_role_all_merchants" ON merchants_mirror
    FOR ALL TO service_role USING (true) WITH CHECK (true);

CREATE POLICY "service_role_all_sales" ON sales_mirror
    FOR ALL TO service_role USING (true) WITH CHECK (true);

CREATE POLICY "service_role_all_installments" ON installments_mirror
    FOR ALL TO service_role USING (true) WITH CHECK (true);

CREATE POLICY "service_role_all_claims" ON claims_mirror
    FOR ALL TO service_role USING (true) WITH CHECK (true);

CREATE POLICY "service_role_all_sync_log" ON sync_outbox_log
    FOR ALL TO service_role USING (true) WITH CHECK (true);
