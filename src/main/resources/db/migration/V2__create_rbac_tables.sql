-- Create groups table
CREATE TABLE groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create junction tables for groups
CREATE TABLE group_roles (
    group_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (group_id, role_id),
    CONSTRAINT fk_group_role_group FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE CASCADE,
    CONSTRAINT fk_group_role_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

CREATE TABLE user_groups (
    user_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, group_id),
    CONSTRAINT fk_user_group_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_group_group FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE CASCADE
);

-- Create user permission overrides
CREATE TABLE user_permission_overrides (
    user_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    granted BOOLEAN NOT NULL,
    PRIMARY KEY (user_id, permission_id),
    CONSTRAINT fk_user_permission_override_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_permission_override_permission FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_group_name ON groups (name);
CREATE INDEX idx_group_role_group_id ON group_roles (group_id);
CREATE INDEX idx_group_role_role_id ON group_roles (role_id);
CREATE INDEX idx_user_group_user_id ON user_groups (user_id);
CREATE INDEX idx_user_group_group_id ON user_groups (group_id);
CREATE INDEX idx_user_permission_override_user_id ON user_permission_overrides (user_id);
CREATE INDEX idx_user_permission_override_permission_id ON user_permission_overrides (permission_id);

-- Add role hierarchy table
CREATE TABLE role_hierarchy (
    parent_id BIGINT NOT NULL,
    child_id BIGINT NOT NULL,
    PRIMARY KEY (parent_id, child_id),
    CONSTRAINT fk_role_hierarchy_parent FOREIGN KEY (parent_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_hierarchy_child FOREIGN KEY (child_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT check_no_self_reference CHECK (parent_id != child_id)
);

CREATE INDEX idx_role_hierarchy_parent_id ON role_hierarchy (parent_id);
CREATE INDEX idx_role_hierarchy_child_id ON role_hierarchy (child_id);

-- Create audit log table
CREATE TABLE security_audit_log (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(100) NOT NULL,
    description TEXT,
    actor VARCHAR(100),
    ip_address VARCHAR(45),
    status VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_security_audit_log_action ON security_audit_log (action);
CREATE INDEX idx_security_audit_log_actor ON security_audit_log (actor);
CREATE INDEX idx_security_audit_log_timestamp ON security_audit_log (timestamp);
