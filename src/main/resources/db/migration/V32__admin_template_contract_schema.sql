ALTER TABLE templates
    ADD COLUMN IF NOT EXISTS summary VARCHAR(500);

ALTER TABLE templates
    ADD COLUMN IF NOT EXISTS runtime VARCHAR(40);

ALTER TABLE templates
    ADD COLUMN IF NOT EXISTS license VARCHAR(80);

ALTER TABLE templates
    ADD COLUMN IF NOT EXISTS source VARCHAR(120);

CREATE TABLE IF NOT EXISTS template_tags (
    template_id BIGINT NOT NULL REFERENCES templates(id) ON DELETE CASCADE,
    tag VARCHAR(80) NOT NULL
);

ALTER TABLE template_interview_questions
    ADD COLUMN IF NOT EXISTS answer_hint VARCHAR(1000) NOT NULL DEFAULT '';

CREATE TABLE IF NOT EXISTS template_test_cases (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES templates(id) ON DELETE CASCADE,
    input TEXT,
    expected_output TEXT,
    description TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_templates_title
    ON templates(title);

CREATE INDEX IF NOT EXISTS idx_templates_created_at
    ON templates(created_at);

CREATE INDEX IF NOT EXISTS idx_templates_runtime
    ON templates(runtime);

CREATE INDEX IF NOT EXISTS idx_template_tags_tag
    ON template_tags(tag);

CREATE INDEX IF NOT EXISTS idx_template_test_cases_template_order
    ON template_test_cases(template_id, order_index, id);
