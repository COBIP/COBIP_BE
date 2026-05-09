CREATE TABLE IF NOT EXISTS user_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    push_notification_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    email_notification_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    editor_font_size INTEGER NOT NULL DEFAULT 14,
    editor_theme VARCHAR(30) NOT NULL DEFAULT 'SYSTEM',
    editor_tab_size INTEGER NOT NULL DEFAULT 4,
    service_theme VARCHAR(30) NOT NULL DEFAULT 'SYSTEM',
    auto_login_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_user_settings_user UNIQUE (user_id)
);
