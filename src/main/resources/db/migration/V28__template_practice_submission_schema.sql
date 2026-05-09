CREATE TABLE IF NOT EXISTS template_practice_submissions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    template_id BIGINT NOT NULL REFERENCES templates(id) ON DELETE CASCADE,
    mission_id BIGINT NOT NULL REFERENCES template_practice_missions(id) ON DELETE CASCADE,
    language VARCHAR(30) NOT NULL,
    source_code TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    passed_count INTEGER NOT NULL,
    total_count INTEGER NOT NULL,
    judge_token VARCHAR(120),
    stdout TEXT,
    stderr TEXT,
    compile_output TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_template_practice_submissions_user_template
    ON template_practice_submissions(user_id, template_id);

CREATE INDEX IF NOT EXISTS idx_template_practice_submissions_mission
    ON template_practice_submissions(mission_id);
