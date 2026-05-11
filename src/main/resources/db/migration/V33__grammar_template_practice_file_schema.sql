CREATE TABLE IF NOT EXISTS grammar_template_practice_files (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES grammar_templates(id) ON DELETE CASCADE,
    chapter_id BIGINT NOT NULL REFERENCES grammar_template_chapters(id) ON DELETE CASCADE,
    node_type VARCHAR(20) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    language VARCHAR(40),
    content TEXT,
    read_only BOOLEAN NOT NULL DEFAULT FALSE,
    order_index INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_grammar_template_practice_files_chapter_path UNIQUE (chapter_id, file_path)
);

CREATE INDEX IF NOT EXISTS idx_grammar_template_practice_files_chapter_order
    ON grammar_template_practice_files(template_id, chapter_id, order_index ASC, id ASC);
