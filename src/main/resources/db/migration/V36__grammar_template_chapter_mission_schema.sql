CREATE TABLE IF NOT EXISTS grammar_template_chapter_missions (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES grammar_templates(id) ON DELETE CASCADE,
    chapter_id BIGINT NOT NULL REFERENCES grammar_template_chapters(id) ON DELETE CASCADE,
    title VARCHAR(120) NOT NULL,
    description TEXT NOT NULL,
    mission_type VARCHAR(30) NOT NULL,
    order_index INTEGER NOT NULL,
    guide_content TEXT,
    validation_json JSONB,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_grammar_template_chapter_missions_chapter_order
    ON grammar_template_chapter_missions(template_id, chapter_id, order_index ASC, id ASC);
