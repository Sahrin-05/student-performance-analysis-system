-- ===================================================================
-- SPAS Initial Database Schema Baseline (Flyway Migration V1)
-- Matches all JPA Entities and unique constraints
-- ===================================================================

-- 1. Users
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    identifier VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    CONSTRAINT uk_users_identifier UNIQUE (identifier),
    CONSTRAINT uk_users_email UNIQUE (email)
);

-- 2. Departments
CREATE TABLE IF NOT EXISTS departments (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT uk_departments_code UNIQUE (code),
    CONSTRAINT uk_departments_name UNIQUE (name)
);

-- 3. Courses
CREATE TABLE IF NOT EXISTS courses (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    department_id BIGINT NOT NULL REFERENCES departments(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT uk_courses_code UNIQUE (code)
);

-- 4. Subjects
CREATE TABLE IF NOT EXISTS subjects (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    credits INTEGER NOT NULL,
    semester INTEGER NOT NULL DEFAULT 1,
    course_id BIGINT NOT NULL REFERENCES courses(id),
    department_id BIGINT NOT NULL REFERENCES departments(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT uk_subjects_code UNIQUE (code)
);

-- 5. Lecturer Assignments
CREATE TABLE IF NOT EXISTS lecturer_assignments (
    id BIGSERIAL PRIMARY KEY,
    subject_id BIGINT NOT NULL REFERENCES subjects(id),
    lecturer_id BIGINT NOT NULL REFERENCES users(id),
    academic_year VARCHAR(20) NOT NULL,
    semester VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    assigned_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_lecturer_assignments_subject_lecturer_year_sem UNIQUE (subject_id, lecturer_id, academic_year, semester)
);

-- 6. Enrollments
CREATE TABLE IF NOT EXISTS enrollments (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id),
    course_id BIGINT NOT NULL REFERENCES courses(id),
    subject_id BIGINT NOT NULL REFERENCES subjects(id),
    academic_year VARCHAR(20) NOT NULL,
    semester VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    enrolled_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_enrollments_student_subject_year_sem UNIQUE (student_id, subject_id, academic_year, semester)
);

-- 7. Assessments
CREATE TABLE IF NOT EXISTS assessments (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    type VARCHAR(30) NOT NULL,
    max_marks DOUBLE PRECISION NOT NULL,
    weight_percentage DOUBLE PRECISION NOT NULL,
    subject_id BIGINT NOT NULL REFERENCES subjects(id),
    academic_year VARCHAR(20) NOT NULL,
    semester VARCHAR(20) NOT NULL,
    creator_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- 8. Student Marks
CREATE TABLE IF NOT EXISTS student_marks (
    id BIGSERIAL PRIMARY KEY,
    assessment_id BIGINT NOT NULL REFERENCES assessments(id),
    enrollment_id BIGINT NOT NULL REFERENCES enrollments(id),
    score DOUBLE PRECISION NOT NULL,
    comments VARCHAR(500),
    marked_by_id BIGINT NOT NULL REFERENCES users(id),
    last_modified_by_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT uk_student_marks_assessment_enrollment UNIQUE (assessment_id, enrollment_id)
);

-- 9. Subject Result Publications
CREATE TABLE IF NOT EXISTS subject_result_publications (
    id BIGSERIAL PRIMARY KEY,
    subject_id BIGINT NOT NULL REFERENCES subjects(id),
    academic_year VARCHAR(20) NOT NULL,
    semester VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    published_by_id BIGINT REFERENCES users(id),
    published_at TIMESTAMP,
    unlocked_by_id BIGINT REFERENCES users(id),
    unlocked_at TIMESTAMP,
    unlock_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT uk_result_pub_subj_year_sem UNIQUE (subject_id, academic_year, semester)
);

-- 10. Attendance Sessions
CREATE TABLE IF NOT EXISTS attendance_sessions (
    id BIGSERIAL PRIMARY KEY,
    subject_id BIGINT NOT NULL REFERENCES subjects(id),
    session_date DATE NOT NULL,
    session_topic VARCHAR(200) NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    semester VARCHAR(20) NOT NULL,
    recorded_by_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- 11. Attendance Records
CREATE TABLE IF NOT EXISTS attendance_records (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES attendance_sessions(id),
    enrollment_id BIGINT NOT NULL REFERENCES enrollments(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PRESENT',
    remarks VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT uk_attendance_session_enrollment UNIQUE (session_id, enrollment_id)
);

-- 12. Risk Alerts
CREATE TABLE IF NOT EXISTS risk_alerts (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id),
    subject_id BIGINT NOT NULL REFERENCES subjects(id),
    academic_year VARCHAR(20) NOT NULL,
    semester VARCHAR(20) NOT NULL,
    risk_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    metric_value DOUBLE PRECISION,
    metric_details TEXT,
    status VARCHAR(30) NOT NULL,
    detected_at TIMESTAMP NOT NULL,
    reviewed_by_id BIGINT REFERENCES users(id),
    reviewed_at TIMESTAMP,
    action_notes TEXT,
    action_recorded_by_id BIGINT REFERENCES users(id),
    action_recorded_at TIMESTAMP,
    resolved_by_id BIGINT REFERENCES users(id),
    resolved_at TIMESTAMP,
    resolution_notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_risk_alerts_student ON risk_alerts(student_id);
CREATE INDEX IF NOT EXISTS idx_risk_alerts_subject ON risk_alerts(subject_id);
CREATE INDEX IF NOT EXISTS idx_risk_alerts_status ON risk_alerts(status);
CREATE INDEX IF NOT EXISTS idx_risk_alerts_risk_type ON risk_alerts(risk_type);
CREATE INDEX IF NOT EXISTS idx_risk_alerts_dedup ON risk_alerts(student_id, subject_id, academic_year, semester, risk_type, status);

-- 13. Mark Audit Logs
CREATE TABLE IF NOT EXISTS mark_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    mark_id BIGINT NOT NULL,
    assessment_id BIGINT NOT NULL,
    enrollment_id BIGINT NOT NULL,
    student_identifier VARCHAR(50) NOT NULL,
    previous_score DOUBLE PRECISION,
    new_score DOUBLE PRECISION NOT NULL,
    action VARCHAR(50) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    performed_by_id BIGINT NOT NULL REFERENCES users(id),
    performed_at TIMESTAMP NOT NULL
);

-- 14. Admin Audit Logs
-- NOTE: Included here as part of the initial clean-install schema baseline.
-- For pre-existing databases that are baselined at version 1 without this table,
-- migration V2__admin_audit_logs.sql idempotently creates it upon adoption.
-- IMMUTABILITY POLICY: Never edit V1 retroactively once deployed. All future
-- schema evolutions must be added as new versioned migrations (e.g. V3__...).
CREATE TABLE IF NOT EXISTS admin_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    entity_identifier VARCHAR(100),
    details TEXT NOT NULL,
    performed_by_id BIGINT REFERENCES users(id),
    performed_by_email VARCHAR(100) NOT NULL,
    performed_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_admin_audit_action ON admin_audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_admin_audit_entity ON admin_audit_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_admin_audit_performed_at ON admin_audit_logs(performed_at);
