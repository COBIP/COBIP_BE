CREATE TABLE IF NOT EXISTS learning_progresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    template_id BIGINT NOT NULL REFERENCES templates(id),
    progress_percent INTEGER NOT NULL,
    last_step VARCHAR(255),
    solved_count INTEGER NOT NULL DEFAULT 0,
    correct_count INTEGER NOT NULL DEFAULT 0,
    study_seconds BIGINT NOT NULL DEFAULT 0,
    last_accessed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_learning_progress_user_template UNIQUE (user_id, template_id)
);

CREATE TABLE IF NOT EXISTS certificates (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    template_id BIGINT NOT NULL REFERENCES templates(id),
    certificate_number VARCHAR(80) NOT NULL UNIQUE,
    issued_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_certificate_user_template UNIQUE (user_id, template_id)
);

CREATE INDEX IF NOT EXISTS idx_learning_progress_user ON learning_progresses(user_id);
