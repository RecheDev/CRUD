-- Insert default roles
-- These are the standard roles available in the system

INSERT INTO roles (name, description, created_at, created_by)
VALUES
    ('ROLE_USER', 'Standard user with basic permissions', CURRENT_TIMESTAMP, 'system'),
    ('ROLE_ADMIN', 'Administrator with full system access', CURRENT_TIMESTAMP, 'system'),
    ('ROLE_MODERATOR', 'Moderator with elevated permissions', CURRENT_TIMESTAMP, 'system');

-- Add comment
COMMENT ON TABLE roles IS 'Default roles have been inserted: ROLE_USER, ROLE_ADMIN, ROLE_MODERATOR';
