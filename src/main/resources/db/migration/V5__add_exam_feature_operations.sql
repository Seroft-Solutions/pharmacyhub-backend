-- This migration extends exam features with additional operations

-- Get the exam feature ID
SET @examFeatureId = (SELECT id FROM features WHERE code = 'exam_management');

-- Add additional operations to the feature
INSERT INTO feature_operations (feature_id, operation) VALUES
(@examFeatureId, 'VIEW'),
(@examFeatureId, 'CREATE'),
(@examFeatureId, 'EDIT'),
(@examFeatureId, 'DUPLICATE'),
(@examFeatureId, 'MANAGE_QUESTIONS'),
(@examFeatureId, 'UNPUBLISH'),
(@examFeatureId, 'ASSIGN'),
(@examFeatureId, 'GRADE'),
(@examFeatureId, 'VIEW_RESULTS'),
(@examFeatureId, 'EXPORT_RESULTS'),
(@examFeatureId, 'VIEW_ANALYTICS');

-- Create more detailed permissions for the exam feature
INSERT INTO permissions (name, description, resource_type, operation_type)
VALUES 
('exam_management:duplicate', 'Permission to duplicate exams', 'FEATURE', 'WRITE'),
('exam_management:manage_questions', 'Permission to manage exam questions', 'FEATURE', 'WRITE'),
('exam_management:unpublish', 'Permission to unpublish exams', 'FEATURE', 'MANAGE'),
('exam_management:assign', 'Permission to assign exams to students', 'FEATURE', 'MANAGE'),
('exam_management:grade', 'Permission to grade exam submissions', 'FEATURE', 'WRITE'),
('exam_management:view_results', 'Permission to view exam results', 'FEATURE', 'READ'),
('exam_management:export_results', 'Permission to export exam results', 'FEATURE', 'READ'),
('exam_management:view_analytics', 'Permission to view exam analytics', 'FEATURE', 'READ');

-- Add new permissions to feature
INSERT INTO feature_permissions (feature_id, permission_id)
SELECT @examFeatureId, id FROM permissions 
WHERE name IN (
    'exam_management:duplicate', 'exam_management:manage_questions', 'exam_management:unpublish',
    'exam_management:assign', 'exam_management:grade', 'exam_management:view_results', 
    'exam_management:export_results', 'exam_management:view_analytics'
);
