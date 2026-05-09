CREATE TABLE IF NOT EXISTS community_post_saves (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    post_id BIGINT NOT NULL REFERENCES community_posts(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_community_post_saves_user_post UNIQUE (user_id, post_id)
);

CREATE INDEX IF NOT EXISTS idx_community_post_saves_user_created
    ON community_post_saves(user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_community_post_saves_post
    ON community_post_saves(post_id);
