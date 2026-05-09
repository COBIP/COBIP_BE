CREATE TABLE IF NOT EXISTS lab_workspaces (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    workspace_key VARCHAR(120) NOT NULL,
    title VARCHAR(120) NOT NULL,
    language VARCHAR(40) NOT NULL,
    active_file_path VARCHAR(255) NOT NULL,
    files_json JSONB NOT NULL,
    last_opened_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_lab_workspaces_user_key UNIQUE (user_id, workspace_key)
);

CREATE INDEX IF NOT EXISTS idx_lab_workspaces_user_last_opened
    ON lab_workspaces(user_id, last_opened_at DESC);
