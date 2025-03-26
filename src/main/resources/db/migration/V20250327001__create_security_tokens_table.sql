-- Add security tokens table for persistent token storage
CREATE TABLE IF NOT EXISTS security_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(512) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    purpose VARCHAR(100) NOT NULL,
    expiration_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Add indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_token ON security_tokens(token);
CREATE INDEX IF NOT EXISTS idx_user_id ON security_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_expiration_time ON security_tokens(expiration_time);
CREATE INDEX IF NOT EXISTS idx_user_purpose ON security_tokens(user_id, purpose);

-- Add comment to table
COMMENT ON TABLE security_tokens IS 'Stores security tokens for email verification, password reset, etc.';
