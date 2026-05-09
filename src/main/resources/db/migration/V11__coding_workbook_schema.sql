CREATE TABLE IF NOT EXISTS coding_workbooks (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(120) NOT NULL UNIQUE,
    title VARCHAR(120) NOT NULL,
    category VARCHAR(80) NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS coding_problems (
    id BIGSERIAL PRIMARY KEY,
    workbook_id BIGINT NOT NULL REFERENCES coding_workbooks(id),
    title VARCHAR(120) NOT NULL,
    category VARCHAR(80) NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    content_json JSONB NOT NULL,
    explanation_json JSONB,
    order_index INTEGER NOT NULL,
    time_limit_millis INTEGER NOT NULL DEFAULT 2000,
    memory_limit_mb INTEGER NOT NULL DEFAULT 256,
    status VARCHAR(20) NOT NULL,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS coding_problem_test_cases (
    id BIGSERIAL PRIMARY KEY,
    problem_id BIGINT NOT NULL REFERENCES coding_problems(id),
    input TEXT NOT NULL,
    expected_output TEXT NOT NULL,
    sample BOOLEAN NOT NULL DEFAULT FALSE,
    order_index INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS coding_problem_starter_codes (
    id BIGSERIAL PRIMARY KEY,
    problem_id BIGINT NOT NULL REFERENCES coding_problems(id),
    language VARCHAR(30) NOT NULL,
    code TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_coding_problem_starter_codes_problem_language UNIQUE (problem_id, language)
);

CREATE TABLE IF NOT EXISTS coding_submissions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    problem_id BIGINT NOT NULL REFERENCES coding_problems(id),
    language VARCHAR(30) NOT NULL,
    source_code TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    passed_count INTEGER NOT NULL DEFAULT 0,
    total_count INTEGER NOT NULL DEFAULT 0,
    judge_token VARCHAR(120),
    stdout TEXT,
    stderr TEXT,
    compile_output TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_coding_workbooks_public_list
    ON coding_workbooks(status, deleted_at, display_order ASC, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_coding_problems_workbook_order
    ON coding_problems(workbook_id, status, deleted_at, order_index ASC);

CREATE INDEX IF NOT EXISTS idx_coding_problem_test_cases_problem_order
    ON coding_problem_test_cases(problem_id, sample, order_index ASC);

CREATE INDEX IF NOT EXISTS idx_coding_submissions_user_problem_created
    ON coding_submissions(user_id, problem_id, created_at DESC);
