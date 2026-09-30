-- Local development data. Not loaded by the test suite.
INSERT INTO users (name, email) VALUES
    ('John Doe', 'john.doe@example.com'),
    ('Jane Smith', 'jane.smith@example.com'),
    ('Bob Johnson', 'bob.johnson@example.com')
ON CONFLICT (email) DO NOTHING;

INSERT INTO wallets (user_id, type, currency, daily_transfer_limit_minor)
SELECT u.id, 'USER', c.currency, 100000
FROM users u
CROSS JOIN (VALUES ('USD'), ('EUR')) AS c (currency)
WHERE u.email IN ('john.doe@example.com', 'jane.smith@example.com', 'bob.johnson@example.com')
ON CONFLICT DO NOTHING;
