CREATE TABLE IF NOT EXISTS grammar_learning_progresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    grammar_template_id BIGINT NOT NULL REFERENCES grammar_templates(id),
    current_chapter_id BIGINT REFERENCES grammar_template_chapters(id),
    progress_percent INTEGER NOT NULL DEFAULT 0,
    last_step VARCHAR(255),
    study_seconds BIGINT NOT NULL DEFAULT 0,
    last_accessed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_grammar_learning_progress_user_template UNIQUE (user_id, grammar_template_id)
);

CREATE INDEX IF NOT EXISTS idx_grammar_learning_progresses_user_accessed
    ON grammar_learning_progresses(user_id, last_accessed_at DESC);

CREATE INDEX IF NOT EXISTS idx_grammar_learning_progresses_template
    ON grammar_learning_progresses(grammar_template_id);
