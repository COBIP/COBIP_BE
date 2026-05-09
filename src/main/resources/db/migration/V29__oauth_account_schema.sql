CREATE TABLE IF NOT EXISTS oauth_accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    provider VARCHAR(20) NOT NULL,
    provider_user_id VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_oauth_accounts_provider_user UNIQUE (provider, provider_user_id),
    CONSTRAINT uk_oauth_accounts_user_provider UNIQUE (user_id, provider)
);

CREATE INDEX IF NOT EXISTS idx_oauth_accounts_user
    ON oauth_accounts(user_id);
