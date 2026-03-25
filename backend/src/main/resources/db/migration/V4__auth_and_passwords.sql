-- ============================================
-- PHASE 1: USER AUTHENTICATION
-- V4 Migration: Add Email/Password Auth to Existing Users Table
-- ============================================
-- 
-- IMPORTANT: The users table already exists from V2 (created for Auth0).
-- This migration ADDS email/password auth columns to that existing table.
-- 
-- Strategy: Support BOTH Auth0 AND email/password simultaneously
-- - auth0_sub: Keep this (existing Auth0 users use this)
-- - email: Add this (new email/password users use this)
-- - password_hash: Add this (store hashed password)
-- - email_verified: Add this (soft gate for email verification)
-- 
-- This allows:
-- 1. Existing Auth0 users continue working
-- 2. New email/password users can register
-- 3. Future: OAuth providers alongside email/password

-- Add columns to existing users table for email/password authentication
ALTER TABLE users
ADD COLUMN IF NOT EXISTS email TEXT UNIQUE,
ADD COLUMN IF NOT EXISTS password_hash TEXT,
ADD COLUMN IF NOT EXISTS email_verified BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT now();

-- Create index on email for fast lookups during login
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Email verification codes: Soft gate to verify email ownership
-- 6-digit code, 24-hour expiry, one-time use
CREATE TABLE IF NOT EXISTS email_verification_codes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  code TEXT NOT NULL, -- 6 digits: "123456"
  created_at TIMESTAMPTZ DEFAULT now(),
  expires_at TIMESTAMPTZ NOT NULL, -- created_at + 24 hours
  verified_at TIMESTAMPTZ, -- NULL until code is used
  last_resent_at TIMESTAMPTZ -- For rate limiting: once per minute
);

CREATE INDEX IF NOT EXISTS idx_email_codes_user ON email_verification_codes(user_id);
CREATE INDEX IF NOT EXISTS idx_email_codes_expires ON email_verification_codes(expires_at);

-- Password reset tokens: Allow users to reset forgotten passwords
-- Token-based, 1-hour expiry, one-time use
CREATE TABLE IF NOT EXISTS password_reset_tokens (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token TEXT NOT NULL UNIQUE, -- Random token sent in email link
  created_at TIMESTAMPTZ DEFAULT now(),
  expires_at TIMESTAMPTZ NOT NULL, -- created_at + 1 hour
  used_at TIMESTAMPTZ -- NULL until password is reset
);

CREATE INDEX IF NOT EXISTS idx_pwd_reset_user ON password_reset_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_pwd_reset_token ON password_reset_tokens(token);

-- Comments for documentation
COMMENT ON TABLE users IS 'User accounts. Supports BOTH Auth0 (auth0_sub) AND email/password (email, password_hash) simultaneously. Auth0 users have null email/password_hash. Email/password users have null auth0_sub.';
COMMENT ON COLUMN users.auth0_sub IS 'Auth0 subject identifier - non-null for Auth0 users, null for email/password users';
COMMENT ON COLUMN users.email IS 'Email address - null for Auth0 users, non-null for email/password users';
COMMENT ON COLUMN users.password_hash IS 'BCrypt hashed password - null for Auth0 users, non-null for email/password users';
COMMENT ON COLUMN users.email_verified IS 'Only relevant for email/password users. True after user enters verification code.';

COMMENT ON TABLE email_verification_codes IS '6-digit codes sent to email, valid 24 hours, one-time use. Soft gate: allows app use but shows alert until verified.';
COMMENT ON COLUMN email_verification_codes.verified_at IS 'Timestamp when code was successfully used';
COMMENT ON COLUMN email_verification_codes.last_resent_at IS 'For rate limiting: only allow resend once per minute';

COMMENT ON TABLE password_reset_tokens IS 'One-time tokens for password reset, valid 1 hour. Used only by email/password users.';
COMMENT ON COLUMN password_reset_tokens.used_at IS 'Timestamp when token was successfully used to reset password';
