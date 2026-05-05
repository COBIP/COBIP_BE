CREATE TABLE IF NOT EXISTS grammar_templates (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(120) NOT NULL,
    title VARCHAR(120) NOT NULL,
    language VARCHAR(30) NOT NULL,
    category VARCHAR(80) NOT NULL,
    difficulty VARCHAR(30) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    content_json JSONB NOT NULL,
    searchable_text TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_grammar_templates_slug_active
    ON grammar_templates(slug)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_grammar_templates_status_deleted_created
    ON grammar_templates(status, deleted_at, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_grammar_templates_language_category_deleted
    ON grammar_templates(language, category, deleted_at);
