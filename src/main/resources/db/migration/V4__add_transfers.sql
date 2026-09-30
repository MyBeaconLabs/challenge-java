CREATE TABLE transfers (
    id             BIGSERIAL PRIMARY KEY,
    from_wallet_id BIGINT      NOT NULL REFERENCES wallets (id),
    to_wallet_id   BIGINT      NOT NULL REFERENCES wallets (id),
    amount_minor   BIGINT      NOT NULL,
    status         VARCHAR(20) NOT NULL,
    note           VARCHAR(255),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_transfers_status ON transfers (status);

-- Per-wallet daily transfer limit, default 1,000.00
ALTER TABLE wallets ADD COLUMN daily_transfer_limit_minor BIGINT;
UPDATE wallets SET daily_transfer_limit_minor = 100000;
ALTER TABLE wallets ALTER COLUMN daily_transfer_limit_minor SET NOT NULL;

CREATE INDEX idx_wallets_daily_transfer_limit ON wallets (daily_transfer_limit_minor);
