-- Create users table
-- This table stores the main user information including authentication credentials

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    modified_by VARCHAR(50)
);

-- Create indexes for better query performance
CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_enabled ON users(enabled);
CREATE INDEX idx_user_created_at ON users(created_at);

-- Add comments for documentation
COMMENT ON TABLE users IS 'Main users table storing authentication and account information';
COMMENT ON COLUMN users.id IS 'Primary key - UUID for better security and distribution';
COMMENT ON COLUMN users.username IS 'Unique username for login';
COMMENT ON COLUMN users.email IS 'Unique email address';
COMMENT ON COLUMN users.password IS 'BCrypt hashed password';
COMMENT ON COLUMN users.enabled IS 'Account activation status';
COMMENT ON COLUMN users.account_non_locked IS 'Account lock status';
COMMENT ON COLUMN users.account_non_expired IS 'Account expiration status';
COMMENT ON COLUMN users.credentials_non_expired IS 'Credentials expiration status';
