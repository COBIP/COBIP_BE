CREATE TABLE IF NOT EXISTS reports (
    id BIGSERIAL PRIMARY KEY,
    reporter_user_id BIGINT NOT NULL REFERENCES users(id),
    target_type VARCHAR(40) NOT NULL,
    target_id BIGINT NOT NULL,
    reason VARCHAR(120) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL,
    admin_memo TEXT,
    processed_by_user_id BIGINT REFERENCES users(id),
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_reports_status_created
    ON reports(status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_reports_target
    ON reports(target_type, target_id);

CREATE INDEX IF NOT EXISTS idx_reports_reporter_created
    ON reports(reporter_user_id, created_at DESC);
