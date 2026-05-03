CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    plan_name VARCHAR(80) NOT NULL,
    status VARCHAR(20) NOT NULL,
    started_at DATE,
    expired_at DATE,
    next_payment_at DATE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
