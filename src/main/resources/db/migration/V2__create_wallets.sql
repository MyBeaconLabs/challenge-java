-- A USER wallet belongs to exactly one user and can never go negative.
-- A SYSTEM wallet is an internal account (one per currency) that funds deposits;
-- its balance is the negative of all money that has entered the platform.
CREATE TABLE wallets (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT REFERENCES users (id),
    type          VARCHAR(16) NOT NULL,
    currency      VARCHAR(3)  NOT NULL,
    balance_minor BIGINT      NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_wallets_type CHECK (type IN ('USER', 'SYSTEM')),
    CONSTRAINT chk_wallets_owner CHECK ((type = 'USER') = (user_id IS NOT NULL)),
    CONSTRAINT chk_wallets_user_non_negative CHECK (type = 'SYSTEM' OR balance_minor >= 0)
);

CREATE UNIQUE INDEX uq_wallets_user_currency ON wallets (user_id, currency) WHERE type = 'USER';
CREATE UNIQUE INDEX uq_wallets_system_currency ON wallets (currency) WHERE type = 'SYSTEM';

INSERT INTO wallets (type, currency) VALUES ('SYSTEM', 'USD'), ('SYSTEM', 'EUR'), ('SYSTEM', 'JPY');
