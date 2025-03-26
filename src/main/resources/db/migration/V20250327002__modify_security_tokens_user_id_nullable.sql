-- Modify security_tokens table to make user_id column nullable
ALTER TABLE security_tokens ALTER COLUMN user_id DROP NOT NULL;

-- Add comment to explain the change
COMMENT ON COLUMN security_tokens.user_id IS 'User ID, can be null for pre-registration tokens that will be updated later';
