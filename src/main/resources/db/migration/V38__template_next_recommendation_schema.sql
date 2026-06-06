CREATE TABLE IF NOT EXISTS template_next_recommendations (
    template_id BIGINT NOT NULL REFERENCES templates(id) ON DELETE CASCADE,
    feature_name VARCHAR(120) NOT NULL,
    reason TEXT,
    expected_learning TEXT,
    priority INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_template_next_recommendations_template_priority
    ON template_next_recommendations(template_id, priority);
