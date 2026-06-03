CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(190) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    review_reason TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE student_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    student_no VARCHAR(60) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL,
    gender VARCHAR(20),
    college VARCHAR(120),
    major VARCHAR(120),
    class_name VARCHAR(120),
    grade VARCHAR(40),
    phone VARCHAR(40),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE teacher_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    teacher_no VARCHAR(60) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL,
    gender VARCHAR(20),
    college VARCHAR(120),
    title VARCHAR(120),
    phone VARCHAR(40),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE competitions (
    id UUID PRIMARY KEY,
    name VARCHAR(180) NOT NULL UNIQUE,
    default_grade VARCHAR(40) NOT NULL,
    organizer VARCHAR(240),
    co_organizer VARCHAR(240),
    description TEXT,
    website_url VARCHAR(500),
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE competition_levels (
    id UUID PRIMARY KEY,
    competition_id UUID NOT NULL REFERENCES competitions(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    grade VARCHAR(40),
    sort_order INTEGER NOT NULL,
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_competition_level UNIQUE (competition_id, name)
);

CREATE TABLE competition_tracks (
    id UUID PRIMARY KEY,
    competition_id UUID NOT NULL REFERENCES competitions(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_competition_track UNIQUE (competition_id, name)
);

CREATE TABLE competition_items (
    id UUID PRIMARY KEY,
    competition_id UUID NOT NULL REFERENCES competitions(id) ON DELETE CASCADE,
    level_id UUID REFERENCES competition_levels(id) ON DELETE SET NULL,
    track_id UUID REFERENCES competition_tracks(id) ON DELETE SET NULL,
    name VARCHAR(180) NOT NULL,
    grade VARCHAR(40),
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_competition_item UNIQUE (competition_id, level_id, track_id, name)
);

CREATE TABLE teams (
    id UUID PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    captain_student_id UUID REFERENCES student_profiles(id) ON DELETE SET NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE team_members (
    id UUID PRIMARY KEY,
    team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES student_profiles(id) ON DELETE CASCADE,
    captain BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_team_member UNIQUE (team_id, student_id)
);

CREATE TABLE awards (
    id UUID PRIMARY KEY,
    competition_id UUID NOT NULL REFERENCES competitions(id) ON DELETE RESTRICT,
    level_id UUID REFERENCES competition_levels(id) ON DELETE SET NULL,
    track_id UUID REFERENCES competition_tracks(id) ON DELETE SET NULL,
    item_id UUID REFERENCES competition_items(id) ON DELETE SET NULL,
    competition_grade VARCHAR(40) NOT NULL,
    award_level VARCHAR(60) NOT NULL,
    subject_type VARCHAR(20) NOT NULL,
    award_year INTEGER,
    award_date DATE,
    primary_student_id UUID REFERENCES student_profiles(id) ON DELETE SET NULL,
    teacher_subject_id UUID REFERENCES teacher_profiles(id) ON DELETE SET NULL,
    team_id UUID REFERENCES teams(id) ON DELETE SET NULL,
    team_name VARCHAR(160),
    team_award BOOLEAN NOT NULL,
    teacher_award_name VARCHAR(160),
    audit_status VARCHAR(20) NOT NULL,
    audit_opinion TEXT,
    declared_by_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    created_by_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE award_advisors (
    id UUID PRIMARY KEY,
    award_id UUID NOT NULL REFERENCES awards(id) ON DELETE CASCADE,
    teacher_id UUID NOT NULL REFERENCES teacher_profiles(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_award_advisor UNIQUE (award_id, teacher_id)
);

CREATE TABLE certificate_files (
    id UUID PRIMARY KEY,
    award_id UUID NOT NULL REFERENCES awards(id) ON DELETE CASCADE,
    original_name VARCHAR(260) NOT NULL,
    storage_path VARCHAR(600) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    file_size BIGINT NOT NULL,
    uploaded_by_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE review_logs (
    id UUID PRIMARY KEY,
    target_type VARCHAR(40) NOT NULL,
    target_id UUID NOT NULL,
    action VARCHAR(40) NOT NULL,
    opinion TEXT,
    reviewer_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE import_jobs (
    id UUID PRIMARY KEY,
    import_type VARCHAR(40) NOT NULL,
    file_name VARCHAR(260),
    status VARCHAR(20) NOT NULL,
    success_rows INTEGER NOT NULL,
    error_rows INTEGER NOT NULL,
    operator_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE import_error_rows (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL REFERENCES import_jobs(id) ON DELETE CASCADE,
    row_index INTEGER NOT NULL,
    field_name VARCHAR(120) NOT NULL,
    message TEXT NOT NULL,
    raw_value TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_awards_status ON awards(audit_status);
CREATE INDEX idx_awards_year ON awards(award_year);
CREATE INDEX idx_awards_primary_student ON awards(primary_student_id);
CREATE INDEX idx_awards_teacher_subject ON awards(teacher_subject_id);
CREATE INDEX idx_awards_team ON awards(team_id);
