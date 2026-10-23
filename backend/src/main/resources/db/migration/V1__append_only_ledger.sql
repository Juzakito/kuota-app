-- Kuota append-only double-entry ledger.
-- Financial rows can only be inserted. Updates, deletes and truncates are rejected by triggers.

CREATE TABLE ledger_transaction (
    id UUID PRIMARY KEY,
    environment VARCHAR(16) NOT NULL,
    kind VARCHAR(64) NOT NULL,
    source_type VARCHAR(64) NOT NULL,
    source_id VARCHAR(200) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    currency CHAR(3) NOT NULL,
    description TEXT NOT NULL,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    CONSTRAINT ledger_transaction_environment_check CHECK (environment IN ('DEMO', 'PRODUCTION')),
    CONSTRAINT ledger_transaction_source_unique UNIQUE (environment, kind, source_type, source_id)
);

CREATE TABLE ledger_posting (
    id BIGSERIAL PRIMARY KEY,
    transaction_id UUID NOT NULL REFERENCES ledger_transaction(id) ON DELETE RESTRICT,
    account VARCHAR(64) NOT NULL,
    direction VARCHAR(6) NOT NULL,
    amount_cents BIGINT NOT NULL,
    currency CHAR(3) NOT NULL,
    CONSTRAINT ledger_posting_direction_check CHECK (direction IN ('DEBIT', 'CREDIT')),
    CONSTRAINT ledger_posting_amount_check CHECK (amount_cents > 0)
);

CREATE INDEX ledger_posting_transaction_idx ON ledger_posting(transaction_id);
CREATE INDEX ledger_transaction_created_idx ON ledger_transaction(created_at DESC);

CREATE OR REPLACE FUNCTION kuota_ledger_assert_balanced()
RETURNS trigger AS $$
DECLARE
    debit_total BIGINT;
    credit_total BIGINT;
BEGIN
    SELECT
        COALESCE(SUM(amount_cents) FILTER (WHERE direction = 'DEBIT'), 0),
        COALESCE(SUM(amount_cents) FILTER (WHERE direction = 'CREDIT'), 0)
    INTO debit_total, credit_total
    FROM ledger_posting
    WHERE transaction_id = NEW.id;

    IF debit_total = 0 THEN
        RAISE EXCEPTION 'ledger transaction % has no postings', NEW.id;
    END IF;

    IF debit_total <> credit_total THEN
        RAISE EXCEPTION 'ledger transaction % is unbalanced: debit=% credit=%', NEW.id, debit_total, credit_total;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER ledger_transaction_balanced
AFTER INSERT ON ledger_transaction
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW
EXECUTE FUNCTION kuota_ledger_assert_balanced();

CREATE OR REPLACE FUNCTION kuota_ledger_forbid_mutation()
RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'append-only ledger: % is not permitted on %', TG_OP, TG_TABLE_NAME;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER ledger_transaction_no_update_delete
BEFORE UPDATE OR DELETE ON ledger_transaction
FOR EACH ROW EXECUTE FUNCTION kuota_ledger_forbid_mutation();

CREATE TRIGGER ledger_transaction_no_truncate
BEFORE TRUNCATE ON ledger_transaction
FOR EACH STATEMENT EXECUTE FUNCTION kuota_ledger_forbid_mutation();

CREATE TRIGGER ledger_posting_no_update_delete
BEFORE UPDATE OR DELETE ON ledger_posting
FOR EACH ROW EXECUTE FUNCTION kuota_ledger_forbid_mutation();

CREATE TRIGGER ledger_posting_no_truncate
BEFORE TRUNCATE ON ledger_posting
FOR EACH STATEMENT EXECUTE FUNCTION kuota_ledger_forbid_mutation();

CREATE TABLE webhook_event (
    provider VARCHAR(64) NOT NULL,
    event_id VARCHAR(200) NOT NULL,
    environment VARCHAR(16) NOT NULL,
    payload_sha256 CHAR(64) NOT NULL,
    status VARCHAR(16) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 1,
    received_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ,
    note TEXT,
    PRIMARY KEY (provider, event_id),
    CONSTRAINT webhook_event_status_check CHECK (status IN ('RECEIVED', 'COMPLETED', 'FAILED', 'IGNORED'))
);

CREATE INDEX webhook_event_status_idx ON webhook_event(status, received_at);

COMMENT ON TABLE ledger_transaction IS 'Append-only double-entry ledger header. Never update or delete financial rows.';
COMMENT ON TABLE ledger_posting IS 'Append-only ledger lines. Debits must equal credits per transaction at commit.';
COMMENT ON TABLE webhook_event IS 'Durable payment-webhook deduplication inbox.';
