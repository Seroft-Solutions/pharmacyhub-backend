-- Create features table
CREATE TABLE features (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    code VARCHAR(50) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    parent_feature_id BIGINT,
    CONSTRAINT fk_parent_feature FOREIGN KEY (parent_feature_id) REFERENCES features (id) ON DELETE SET NULL
);

-- Create feature_permissions junction table
CREATE TABLE feature_permissions (
    feature_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (feature_id, permission_id),
    CONSTRAINT fk_feature_permission_feature FOREIGN KEY (feature_id) REFERENCES features (id) ON DELETE CASCADE,
    CONSTRAINT fk_feature_permission_permission FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE
);

-- Create index for faster lookups
CREATE INDEX idx_feature_code ON features (code);
CREATE INDEX idx_feature_permission_feature_id ON feature_permissions (feature_id);
CREATE INDEX idx_feature_permission_permission_id ON feature_permissions (permission_id);

-- Insert basic features
INSERT INTO features (name, description, code, active) VALUES
('Dashboard', 'Main dashboard access', 'dashboard', true),
('User Management', 'User management features', 'user_management', true),
('Exam Management', 'Exam management features', 'exam_management', true),
('Paper Management', 'Paper management features', 'paper_management', true);

-- Insert child features
INSERT INTO features (name, description, code, active, parent_feature_id) VALUES
('User View', 'View user details', 'user_view', true, (SELECT id FROM features WHERE code = 'user_management')),
('User Create', 'Create new users', 'user_create', true, (SELECT id FROM features WHERE code = 'user_management')),
('User Edit', 'Edit existing users', 'user_edit', true, (SELECT id FROM features WHERE code = 'user_management')),
('User Delete', 'Delete users', 'user_delete', true, (SELECT id FROM features WHERE code = 'user_management')),

('Exam View', 'View exams', 'exam_view', true, (SELECT id FROM features WHERE code = 'exam_management')),
('Exam Create', 'Create new exams', 'exam_create', true, (SELECT id FROM features WHERE code = 'exam_management')),
('Exam Edit', 'Edit existing exams', 'exam_edit', true, (SELECT id FROM features WHERE code = 'exam_management')),
('Exam Delete', 'Delete exams', 'exam_delete', true, (SELECT id FROM features WHERE code = 'exam_management')),
('Exam Take', 'Take exams', 'exam_take', true, (SELECT id FROM features WHERE code = 'exam_management')),
('Exam Grade', 'Grade exams', 'exam_grade', true, (SELECT id FROM features WHERE code = 'exam_management')),

('Practice Papers', 'Manage practice papers', 'practice_papers', true, (SELECT id FROM features WHERE code = 'paper_management')),
('Model Papers', 'Manage model papers', 'model_papers', true, (SELECT id FROM features WHERE code = 'paper_management')),
('Past Papers', 'Manage past papers', 'past_papers', true, (SELECT id FROM features WHERE code = 'paper_management')),
('Subject Papers', 'Manage subject papers', 'subject_papers', true, (SELECT id FROM features WHERE code = 'paper_management'));
