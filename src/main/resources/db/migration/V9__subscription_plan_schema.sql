CREATE TABLE IF NOT EXISTS subscription_plans (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL,
    description TEXT NOT NULL,
    price_amount INTEGER NOT NULL,
    currency VARCHAR(10) NOT NULL,
    duration_days INTEGER NOT NULL,
    benefit_description TEXT NOT NULL,
    visible BOOLEAN NOT NULL,
    display_order INTEGER NOT NULL,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_subscription_plans_visible_order
    ON subscription_plans(visible, display_order);
