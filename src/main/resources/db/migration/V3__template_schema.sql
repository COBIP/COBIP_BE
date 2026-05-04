CREATE TABLE IF NOT EXISTS templates (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(120) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(80) NOT NULL,
    difficulty VARCHAR(30) NOT NULL,
    design_intent TEXT,
    requirements_spec TEXT,
    erd TEXT,
    api_spec TEXT,
    project_structure TEXT,
    visibility VARCHAR(20) NOT NULL,
    access_level VARCHAR(20) NOT NULL,
    file_url VARCHAR(1000),
    file_key VARCHAR(500),
    thumbnail_url VARCHAR(1000),
    thumbnail_key VARCHAR(500),
    view_count BIGINT NOT NULL DEFAULT 0,
    favorite_count BIGINT NOT NULL DEFAULT 0,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS template_tech_stacks (
    template_id BIGINT NOT NULL REFERENCES templates(id) ON DELETE CASCADE,
    tech_stack VARCHAR(80) NOT NULL
);

CREATE TABLE IF NOT EXISTS template_interview_questions (
    template_id BIGINT NOT NULL REFERENCES templates(id) ON DELETE CASCADE,
    question VARCHAR(1000) NOT NULL
);

CREATE TABLE IF NOT EXISTS template_favorites (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    template_id BIGINT NOT NULL REFERENCES templates(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_template_favorite_user_template UNIQUE (user_id, template_id)
);

CREATE INDEX IF NOT EXISTS idx_templates_visibility_deleted ON templates(visibility, deleted_at);
CREATE INDEX IF NOT EXISTS idx_templates_category ON templates(category);
