CREATE TABLE IF NOT EXISTS ai_template_progresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    ai_template_id VARCHAR(120) NOT NULL,
    template_title VARCHAR(120) NOT NULL,
    template_snapshot_json JSONB NOT NULL,
    sections_json JSONB,
    last_learning_position_json JSONB,
    progress_percent INTEGER NOT NULL DEFAULT 0,
    last_step VARCHAR(255),
    study_seconds BIGINT NOT NULL DEFAULT 0,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    last_accessed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_ai_template_progress_user_template UNIQUE (user_id, ai_template_id)
);

CREATE INDEX IF NOT EXISTS idx_ai_template_progresses_user_accessed
    ON ai_template_progresses(user_id, last_accessed_at DESC);
