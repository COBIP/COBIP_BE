CREATE TABLE IF NOT EXISTS user_learning_daily_stats (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    activity_date DATE NOT NULL,
    study_seconds BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_user_learning_daily_stat_user_date UNIQUE (user_id, activity_date)
);

CREATE INDEX IF NOT EXISTS idx_user_learning_daily_stats_user_date
    ON user_learning_daily_stats(user_id, activity_date DESC);
