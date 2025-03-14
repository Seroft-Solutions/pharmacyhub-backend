-- Add feature_operations table to support operation-level permissions
CREATE TABLE IF NOT EXISTS feature_operations (
    feature_id BIGINT NOT NULL,
    operation VARCHAR(255) NOT NULL,
    PRIMARY KEY (feature_id, operation),
    CONSTRAINT fk_feature_operations_feature FOREIGN KEY (feature_id) REFERENCES features (id)
);

-- Add common operations to existing features
-- For exam feature
INSERT INTO feature_operations (feature_id, operation)
SELECT id, 'READ' FROM features WHERE code = 'exam_management';

INSERT INTO feature_operations (feature_id, operation)
SELECT id, 'WRITE' FROM features WHERE code = 'exam_management';

INSERT INTO feature_operations (feature_id, operation)
SELECT id, 'DELETE' FROM features WHERE code = 'exam_management';

INSERT INTO feature_operations (feature_id, operation)
SELECT id, 'PUBLISH' FROM features WHERE code = 'exam_management';

INSERT INTO feature_operations (feature_id, operation)
SELECT id, 'TAKE' FROM features WHERE code = 'exam_management';

-- Create standard operation-specific permissions
INSERT INTO permissions (name, description, resource_type, operation_type)
VALUES 
('exam_management:READ', 'Permission to view exams', 'FEATURE', 'READ'),
('exam_management:WRITE', 'Permission to create and edit exams', 'FEATURE', 'WRITE'),
('exam_management:DELETE', 'Permission to delete exams', 'FEATURE', 'DELETE'),
('exam_management:PUBLISH', 'Permission to publish exams', 'FEATURE', 'MANAGE'),
('exam_management:TAKE', 'Permission to take exams', 'FEATURE', 'EXECUTE');

-- Assign these permissions to existing roles
-- For admin role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.name = 'ADMIN' AND p.name IN ('exam_management:READ', 'exam_management:WRITE', 'exam_management:DELETE', 'exam_management:PUBLISH');

-- For student role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.name = 'STUDENT' AND p.name IN ('exam_management:READ', 'exam_management:TAKE');

-- For instructor role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.name = 'INSTRUCTOR' AND p.name IN ('exam_management:READ', 'exam_management:WRITE', 'exam_management:PUBLISH');
