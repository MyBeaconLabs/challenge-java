-- Double-entry ledger. Every money movement is one ledger_transaction whose
-- entries sum to zero. wallets.balance_minor is a cache of SUM(amount_minor)
-- per wallet and is only ever updated by LedgerService in the same transaction.
CREATE TABLE ledger_transactions (
    id              BIGSERIAL PRIMARY KEY,
    type            VARCHAR(32)  NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    description     VARCHAR(255),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_ledger_transactions_idempotency UNIQUE (type, idempotency_key)
);

CREATE TABLE ledger_entries (
    id             BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT      NOT NULL REFERENCES ledger_transactions (id),
    wallet_id      BIGINT      NOT NULL REFERENCES wallets (id),
    amount_minor   BIGINT      NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_ledger_entries_non_zero CHECK (amount_minor <> 0)
);

CREATE INDEX idx_ledger_entries_wallet ON ledger_entries (wallet_id, id);
CREATE INDEX idx_ledger_entries_transaction ON ledger_entries (transaction_id);
