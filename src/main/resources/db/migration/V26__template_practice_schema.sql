CREATE TABLE IF NOT EXISTS template_practice_files (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES templates(id) ON DELETE CASCADE,
    file_path VARCHAR(255) NOT NULL,
    language VARCHAR(40) NOT NULL,
    content TEXT NOT NULL,
    read_only BOOLEAN NOT NULL DEFAULT FALSE,
    order_index INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_template_practice_files_template_path UNIQUE (template_id, file_path)
);

CREATE TABLE IF NOT EXISTS template_practice_missions (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES templates(id) ON DELETE CASCADE,
    title VARCHAR(120) NOT NULL,
    description TEXT NOT NULL,
    mission_type VARCHAR(30) NOT NULL,
    order_index INTEGER NOT NULL,
    guide_content TEXT,
    validation_json JSONB,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_template_practice_missions_template_order UNIQUE (template_id, order_index)
);

CREATE TABLE IF NOT EXISTS template_practice_progresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    template_id BIGINT NOT NULL REFERENCES templates(id) ON DELETE CASCADE,
    current_mission_id BIGINT REFERENCES template_practice_missions(id),
    status VARCHAR(20) NOT NULL,
    progress_percent INTEGER NOT NULL DEFAULT 0,
    completed_mission_count INTEGER NOT NULL DEFAULT 0,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    last_accessed_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_template_practice_progress_user_template UNIQUE (user_id, template_id)
);

CREATE TABLE IF NOT EXISTS template_practice_mission_progresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    mission_id BIGINT NOT NULL REFERENCES template_practice_missions(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_template_practice_mission_progress_user_mission UNIQUE (user_id, mission_id)
);

CREATE INDEX IF NOT EXISTS idx_template_practice_files_template_order
    ON template_practice_files(template_id, order_index);

CREATE INDEX IF NOT EXISTS idx_template_practice_missions_template_order
    ON template_practice_missions(template_id, order_index);

CREATE INDEX IF NOT EXISTS idx_template_practice_progresses_user_status
    ON template_practice_progresses(user_id, status);
