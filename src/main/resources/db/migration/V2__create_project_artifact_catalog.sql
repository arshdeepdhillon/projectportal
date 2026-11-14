-- Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored

drop table persistence_records;

create table academic_units (
    id UUID primary key,
    unit_type varchar(255) not null,
    name varchar(255),
    code varchar(255),
    course_code varchar(255),
    course_name varchar(255)
);

create table projects (
    id UUID primary key,
    title varchar(255) not null,
    description TEXT,
    created_at BIGINT not null,
    updated_at BIGINT not null,
    revision_version integer not null,
    deleted integer not null
);

create table project_members (
    id UUID primary key,
    project_id UUID not null references projects (id) on delete cascade,
    member_id varchar(255) not null,
    member_role varchar(255) not null
);

create table project_academic_units (
    project_id UUID not null references projects (id) on delete cascade,
    academic_unit_id UUID not null references academic_units (id),
    primary key (project_id, academic_unit_id)
);

create table artifacts (
    id UUID primary key,
    project_id UUID not null references projects (id) on delete cascade,
    artifact_type varchar(255) not null,
    deleted integer not null,
    repo_url TEXT,
    target_branch varchar(255),
    primary_tech_stack varchar(255),
    total_commit_count integer,
    latest_commit_timestamp BIGINT,
    manuscript_title varchar(255),
    manuscript_abstract TEXT,
    publication_year integer,
    publisher_name varchar(255),
    publisher_venue varchar(255),
    video_url TEXT,
    thumbnail_url TEXT,
    duration_millis BIGINT,
    video_description TEXT
);

create table manuscript_authors (
    artifact_id UUID not null references artifacts (id) on delete cascade,
    author_name varchar(255)
);

create table manuscript_keywords (
    artifact_id UUID not null references artifacts (id) on delete cascade,
    keyword varchar(255)
);

create table manuscript_files (
    artifact_id UUID primary key references artifacts (id) on delete cascade,
    sha256 varchar(64) not null,
    original_filename varchar(255) not null,
    media_type varchar(255) not null
);

create table manuscript_file_contents (
    artifact_id UUID primary key references manuscript_files (artifact_id) on delete cascade,
    file_content BYTEA not null
);

create index idx_projects_deleted on projects (deleted);
create index idx_artifacts_project_type_deleted on artifacts (project_id, artifact_type, deleted);
create index idx_academic_units_type on academic_units (unit_type);
