CREATE TABLE IF NOT EXISTS payment_histories (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    subscription_id BIGINT REFERENCES subscriptions(id),
    plan_name VARCHAR(80) NOT NULL,
    amount INTEGER NOT NULL,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_method VARCHAR(40),
    receipt_url TEXT,
    paid_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_payment_histories_user_paid_at
    ON payment_histories(user_id, paid_at DESC);

CREATE INDEX IF NOT EXISTS idx_payment_histories_status
    ON payment_histories(status);
