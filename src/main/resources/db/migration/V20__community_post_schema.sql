CREATE TABLE IF NOT EXISTS community_posts (
    id BIGSERIAL PRIMARY KEY,
    author_user_id BIGINT NOT NULL REFERENCES users(id),
    category VARCHAR(30) NOT NULL,
    title VARCHAR(120) NOT NULL,
    content_json JSONB NOT NULL,
    searchable_text TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    view_count BIGINT NOT NULL DEFAULT 0,
    comment_count BIGINT NOT NULL DEFAULT 0,
    like_count BIGINT NOT NULL DEFAULT 0,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_community_posts_category_created
    ON community_posts(category, created_at DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_community_posts_status_created
    ON community_posts(status, created_at DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_community_posts_author_created
    ON community_posts(author_user_id, created_at DESC)
    WHERE deleted_at IS NULL;
