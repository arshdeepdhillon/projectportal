-- Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored

DROP TABLE persistence_records;

CREATE TABLE academic_units (
    id UUID PRIMARY KEY,
    unit_type VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    code VARCHAR(255),
    course_code VARCHAR(255),
    course_name VARCHAR(255)
);

CREATE TABLE projects (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    revision_version INTEGER NOT NULL,
    deleted INTEGER NOT NULL
);

CREATE TABLE project_members (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    member_id VARCHAR(255) NOT NULL,
    member_role VARCHAR(255) NOT NULL
);

CREATE TABLE project_academic_units (
    project_id UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    academic_unit_id UUID NOT NULL REFERENCES academic_units (id),
    PRIMARY KEY (project_id, academic_unit_id)
);

CREATE TABLE artifacts (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    artifact_type VARCHAR(255) NOT NULL,
    deleted INTEGER NOT NULL,
    repo_url TEXT,
    target_branch VARCHAR(255),
    primary_tech_stack VARCHAR(255),
    total_commit_count INTEGER,
    latest_commit_timestamp BIGINT,
    manuscript_title VARCHAR(255),
    manuscript_abstract TEXT,
    publication_year INTEGER,
    publisher_name VARCHAR(255),
    publisher_venue VARCHAR(255),
    video_url TEXT,
    thumbnail_url TEXT,
    duration_millis BIGINT,
    video_description TEXT
);

CREATE TABLE manuscript_authors (
    artifact_id UUID NOT NULL REFERENCES artifacts (id) ON DELETE CASCADE,
    author_name VARCHAR(255)
);

CREATE TABLE manuscript_keywords (
    artifact_id UUID NOT NULL REFERENCES artifacts (id) ON DELETE CASCADE,
    keyword VARCHAR(255)
);

CREATE TABLE manuscript_files (
    artifact_id UUID PRIMARY KEY REFERENCES artifacts (id) ON DELETE CASCADE,
    sha256 VARCHAR(64) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    media_type VARCHAR(255) NOT NULL
);

CREATE TABLE manuscript_file_contents (
    artifact_id UUID PRIMARY KEY REFERENCES manuscript_files (artifact_id) ON DELETE CASCADE,
    file_content BYTEA NOT NULL
);

CREATE INDEX idx_projects_deleted ON projects (deleted);
CREATE INDEX idx_artifacts_project_type_deleted ON artifacts (project_id, artifact_type, deleted);
CREATE INDEX idx_academic_units_type ON academic_units (unit_type);
