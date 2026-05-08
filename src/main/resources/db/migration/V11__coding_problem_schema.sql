CREATE TABLE IF NOT EXISTS coding_problems (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    category VARCHAR(80) NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    description TEXT NOT NULL,
    input_description TEXT NOT NULL,
    output_description TEXT NOT NULL,
    sample_input TEXT NOT NULL,
    sample_output TEXT NOT NULL,
    time_limit_millis INTEGER NOT NULL DEFAULT 2000,
    memory_limit_mb INTEGER NOT NULL DEFAULT 256,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_coding_problems_public_list
    ON coding_problems(published, deleted_at, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_coding_problems_category
    ON coding_problems(category);

CREATE INDEX IF NOT EXISTS idx_coding_problems_difficulty
    ON coding_problems(difficulty);
