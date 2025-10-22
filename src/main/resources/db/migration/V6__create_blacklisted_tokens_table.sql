-- Create table for blacklisted JWT tokens
-- Replaces in-memory storage for better persistence

CREATE TABLE blacklisted_tokens (
    id BIGSERIAL PRIMARY KEY,
    jti VARCHAR(255) NOT NULL UNIQUE,
    expiry_time TIMESTAMP NOT NULL,
    blacklisted_at TIMESTAMP NOT NULL,
    username VARCHAR(100)
);

-- Index for fast JTI lookups (main use case)
CREATE INDEX idx_jti ON blacklisted_tokens(jti);

-- Index for cleanup operations (deleting expired tokens)
CREATE INDEX idx_expiry ON blacklisted_tokens(expiry_time);

-- Comment for documentation
COMMENT ON TABLE blacklisted_tokens IS 'Stores blacklisted JWT tokens to prevent reuse after logout';
COMMENT ON COLUMN blacklisted_tokens.jti IS 'JWT ID (jti claim) - unique identifier for the token';
COMMENT ON COLUMN blacklisted_tokens.expiry_time IS 'When the token expires (safe to delete after this)';
COMMENT ON COLUMN blacklisted_tokens.blacklisted_at IS 'When the token was blacklisted';
COMMENT ON COLUMN blacklisted_tokens.username IS 'Username associated with the token (for auditing)';
