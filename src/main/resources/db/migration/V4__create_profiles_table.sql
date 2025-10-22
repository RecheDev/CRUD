-- Create profiles table
-- This table stores extended user information with a one-to-one relationship to users

CREATE TABLE profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    bio TEXT,
    avatar_url VARCHAR(500),
    phone_number VARCHAR(20),
    date_of_birth DATE,
    address VARCHAR(300),
    city VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    modified_by VARCHAR(50),
    CONSTRAINT fk_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for efficient queries
CREATE UNIQUE INDEX idx_profile_user_id ON profiles(user_id);
CREATE INDEX idx_profile_created_at ON profiles(created_at);

-- Add comments for documentation
COMMENT ON TABLE profiles IS 'Extended user profile information with one-to-one relationship to users';
COMMENT ON COLUMN profiles.id IS 'Primary key - same as user_id (shared primary key)';
COMMENT ON COLUMN profiles.user_id IS 'Foreign key and unique identifier linking to users table';
COMMENT ON COLUMN profiles.bio IS 'User biography or description';
COMMENT ON COLUMN profiles.avatar_url IS 'URL to user avatar image';
COMMENT ON COLUMN profiles.phone_number IS 'User contact phone number';
COMMENT ON COLUMN profiles.date_of_birth IS 'User date of birth';
COMMENT ON COLUMN profiles.address IS 'User street address';
COMMENT ON COLUMN profiles.city IS 'User city';
COMMENT ON COLUMN profiles.country IS 'User country';
COMMENT ON COLUMN profiles.postal_code IS 'User postal/zip code';
