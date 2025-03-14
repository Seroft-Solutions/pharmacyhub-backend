-- Insert exam feature
INSERT INTO features (name, description, code, active)
VALUES ('Exam Management', 'Features related to exam creation, management, and taking exams', 'exams', true);

-- Get the feature ID
SET @examFeatureId = LAST_INSERT_ID();

-- Add operations to the feature
INSERT INTO feature_operations (feature_id, operation) VALUES
(@examFeatureId, 'VIEW'),
(@examFeatureId, 'TAKE'),
(@examFeatureId, 'CREATE'),
(@examFeatureId, 'EDIT'),
(@examFeatureId, 'DELETE'),
(@examFeatureId, 'DUPLICATE'),
(@examFeatureId, 'MANAGE_QUESTIONS'),
(@examFeatureId, 'PUBLISH'),
(@examFeatureId, 'UNPUBLISH'),
(@examFeatureId, 'ASSIGN'),
(@examFeatureId, 'GRADE'),
(@examFeatureId, 'VIEW_RESULTS'),
(@examFeatureId, 'EXPORT_RESULTS'),
(@examFeatureId, 'VIEW_ANALYTICS');

-- Map existing permissions to feature operations
-- Get existing permission IDs
SET @viewPermissionId = (SELECT id FROM permissions WHERE name = 'exams:view');
SET @takePermissionId = (SELECT id FROM permissions WHERE name = 'exams:take');
SET @createPermissionId = (SELECT id FROM permissions WHERE name = 'exams:create');
SET @editPermissionId = (SELECT id FROM permissions WHERE name = 'exams:edit');
SET @deletePermissionId = (SELECT id FROM permissions WHERE name = 'exams:delete');
SET @duplicatePermissionId = (SELECT id FROM permissions WHERE name = 'exams:duplicate');
SET @manageQuestionsPermissionId = (SELECT id FROM permissions WHERE name = 'exams:manage-questions');
SET @publishPermissionId = (SELECT id FROM permissions WHERE name = 'exams:publish');
SET @unpublishPermissionId = (SELECT id FROM permissions WHERE name = 'exams:unpublish');
SET @assignPermissionId = (SELECT id FROM permissions WHERE name = 'exams:assign');
SET @gradePermissionId = (SELECT id FROM permissions WHERE name = 'exams:grade');
SET @viewResultsPermissionId = (SELECT id FROM permissions WHERE name = 'exams:view-results');
SET @exportResultsPermissionId = (SELECT id FROM permissions WHERE name = 'exams:export-results');
SET @viewAnalyticsPermissionId = (SELECT id FROM permissions WHERE name = 'exams:view-analytics');

-- Add permissions to feature
INSERT INTO feature_permissions (feature_id, permission_id)
SELECT @examFeatureId, id FROM permissions 
WHERE name IN (
    'exams:view', 'exams:take', 'exams:create', 'exams:edit', 'exams:delete',
    'exams:duplicate', 'exams:manage-questions', 'exams:publish', 'exams:unpublish',
    'exams:assign', 'exams:grade', 'exams:view-results', 'exams:export-results',
    'exams:view-analytics'
);
