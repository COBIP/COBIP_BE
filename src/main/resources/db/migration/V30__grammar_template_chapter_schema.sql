CREATE TABLE IF NOT EXISTS grammar_template_chapters (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES grammar_templates(id) ON DELETE CASCADE,
    title VARCHAR(120) NOT NULL,
    order_index INTEGER NOT NULL,
    content_json JSONB NOT NULL,
    searchable_text TEXT NOT NULL,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_grammar_template_chapters_template_order
    ON grammar_template_chapters(template_id, deleted_at, order_index ASC, id ASC);
