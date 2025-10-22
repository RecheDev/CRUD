-- Create table for rate limiting data
-- Replaces in-memory storage for better persistence

CREATE TABLE rate_limit_entries (
    id BIGSERIAL PRIMARY KEY,
    client_key VARCHAR(100) NOT NULL UNIQUE,
    tokens INTEGER NOT NULL DEFAULT 60,
    last_refill_time TIMESTAMP NOT NULL,
    hourly_tokens INTEGER NOT NULL DEFAULT 1000,
    hourly_refill_time TIMESTAMP NOT NULL,
    last_access_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for client key lookups (main use case)
CREATE INDEX idx_client_key ON rate_limit_entries(client_key);

-- Index for cleanup operations (finding stale entries)
CREATE INDEX idx_last_refill ON rate_limit_entries(last_refill_time);

-- Comment for documentation
COMMENT ON TABLE rate_limit_entries IS 'Stores rate limiting data per client IP address';
COMMENT ON COLUMN rate_limit_entries.client_key IS 'Client identifier (typically IP address)';
COMMENT ON COLUMN rate_limit_entries.tokens IS 'Available tokens for per-minute rate limit (max 60)';
COMMENT ON COLUMN rate_limit_entries.last_refill_time IS 'Last time per-minute tokens were refilled';
COMMENT ON COLUMN rate_limit_entries.hourly_tokens IS 'Available tokens for per-hour rate limit (max 1000)';
COMMENT ON COLUMN rate_limit_entries.hourly_refill_time IS 'Last time hourly tokens were refilled';
COMMENT ON COLUMN rate_limit_entries.last_access_time IS 'Last time this entry was accessed';
