CREATE TABLE IF NOT EXISTS community_comments (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES community_posts(id),
    author_user_id BIGINT NOT NULL REFERENCES users(id),
    parent_comment_id BIGINT REFERENCES community_comments(id),
    content VARCHAR(2000) NOT NULL,
    status VARCHAR(20) NOT NULL,
    like_count BIGINT NOT NULL DEFAULT 0,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_community_comments_post_created
    ON community_comments(post_id, created_at)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_community_comments_parent_created
    ON community_comments(parent_comment_id, created_at)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_community_comments_author_created
    ON community_comments(author_user_id, created_at DESC)
    WHERE deleted_at IS NULL;
