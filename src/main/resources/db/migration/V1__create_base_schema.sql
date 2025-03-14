-- Create basic security tables
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    system BOOLEAN NOT NULL DEFAULT FALSE,
    precedence INT NOT NULL DEFAULT 100,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    resource_type VARCHAR(50) NOT NULL,
    operation_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_user_username ON users (username);
CREATE INDEX idx_user_email ON users (email);
CREATE INDEX idx_role_name ON roles (name);
CREATE INDEX idx_permission_name ON permissions (name);
CREATE INDEX idx_permission_resource ON permissions (resource_type);
CREATE INDEX idx_role_permission_role_id ON role_permissions (role_id);
CREATE INDEX idx_role_permission_permission_id ON role_permissions (permission_id);
CREATE INDEX idx_user_role_user_id ON user_roles (user_id);
CREATE INDEX idx_user_role_role_id ON user_roles (role_id);

-- Insert basic roles
INSERT INTO roles (name, description, system, precedence) VALUES
('ADMIN', 'Administrator with full system access', true, 0),
('MANAGER', 'Pharmacy manager with access to pharmacy operations', true, 10),
('PHARMACIST', 'Pharmacist with access to prescriptions and inventory', true, 20),
('SALESMAN', 'Sales representative with limited access', true, 30),
('PROPRIETOR', 'Pharmacy owner with business oversight', true, 5),
('STUDENT', 'Student role for exam access', true, 50),
('INSTRUCTOR', 'Instructor role for managing exams', true, 15);

-- Insert basic permissions
INSERT INTO permissions (name, description, resource_type, operation_type) VALUES
-- User management permissions
('user:view', 'View user profiles', 'USER', 'READ'),
('user:create', 'Create new users', 'USER', 'WRITE'),
('user:edit', 'Edit user profiles', 'USER', 'WRITE'),
('user:delete', 'Delete users', 'USER', 'DELETE'),

-- Role management permissions
('role:view', 'View roles', 'ROLE', 'READ'),
('role:create', 'Create roles', 'ROLE', 'WRITE'),
('role:edit', 'Edit roles', 'ROLE', 'WRITE'),
('role:delete', 'Delete roles', 'ROLE', 'DELETE'),

-- Permission management
('permission:view', 'View permissions', 'PERMISSION', 'READ'),
('permission:create', 'Create permissions', 'PERMISSION', 'WRITE'),
('permission:edit', 'Edit permissions', 'PERMISSION', 'WRITE'),
('permission:delete', 'Delete permissions', 'PERMISSION', 'DELETE');

-- Assign permissions to roles
-- Admin has all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 
    (SELECT id FROM roles WHERE name = 'ADMIN'),
    id
FROM permissions;
