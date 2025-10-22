-- Create roles table
-- This table stores the available roles in the system for RBAC

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    modified_by VARCHAR(50)
);

-- Create index for role name lookups
CREATE UNIQUE INDEX idx_role_name ON roles(name);

-- Add comments for documentation
COMMENT ON TABLE roles IS 'Roles table for role-based access control (RBAC)';
COMMENT ON COLUMN roles.id IS 'Primary key - auto-incrementing ID';
COMMENT ON COLUMN roles.name IS 'Unique role name (e.g., ROLE_ADMIN, ROLE_USER)';
COMMENT ON COLUMN roles.description IS 'Human-readable description of the role';
