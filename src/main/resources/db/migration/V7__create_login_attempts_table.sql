-- Create table for tracking login attempts and account lockouts
-- Replaces in-memory storage for better persistence

CREATE TABLE login_attempts (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    first_attempt_time TIMESTAMP NOT NULL,
    lock_until TIMESTAMP,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for username lookups (main use case)
CREATE INDEX idx_username ON login_attempts(username);

-- Index for cleanup operations (finding expired locks)
CREATE INDEX idx_lock_until ON login_attempts(lock_until);

-- Comment for documentation
COMMENT ON TABLE login_attempts IS 'Tracks failed login attempts and account lockout status';
COMMENT ON COLUMN login_attempts.username IS 'Username attempting to login';
COMMENT ON COLUMN login_attempts.attempt_count IS 'Number of failed login attempts';
COMMENT ON COLUMN login_attempts.first_attempt_time IS 'Timestamp of first failed attempt in current window';
COMMENT ON COLUMN login_attempts.lock_until IS 'Timestamp until which account is locked (NULL if not locked)';
COMMENT ON COLUMN login_attempts.last_updated IS 'Last time this record was updated';
