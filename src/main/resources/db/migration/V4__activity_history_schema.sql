CREATE TABLE IF NOT EXISTS activity_histories (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    type VARCHAR(40) NOT NULL,
    message VARCHAR(255) NOT NULL,
    target_type VARCHAR(60) NOT NULL,
    target_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_activity_histories_user_created ON activity_histories(user_id, created_at DESC);
