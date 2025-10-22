-- Create refresh_tokens table for JWT refresh token management
-- Supports token rotation, multi-device login, and automatic cleanup

CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(36) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_from_ip VARCHAR(45),
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    modified_by VARCHAR(50),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for performance optimization
CREATE UNIQUE INDEX idx_refresh_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_expiry ON refresh_tokens(expiry_date);
CREATE INDEX idx_refresh_revoked ON refresh_tokens(revoked, revoked_at);

-- Comments for documentation
COMMENT ON TABLE refresh_tokens IS 'Stores refresh tokens for JWT authentication with token rotation support';
COMMENT ON COLUMN refresh_tokens.token IS 'UUID-based refresh token string';
COMMENT ON COLUMN refresh_tokens.user_id IS 'Foreign key to users table';
COMMENT ON COLUMN refresh_tokens.expiry_date IS 'When the refresh token expires (default 7 days)';
COMMENT ON COLUMN refresh_tokens.created_at IS 'When the refresh token was created';
COMMENT ON COLUMN refresh_tokens.created_from_ip IS 'IP address where the token was created (security audit)';
COMMENT ON COLUMN refresh_tokens.revoked IS 'Whether the token has been manually revoked (logout, token rotation)';
COMMENT ON COLUMN refresh_tokens.revoked_at IS 'When the token was revoked';
