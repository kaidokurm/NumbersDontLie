-- ============================================
-- PHASE 3: Multiple OAuth identities per user
-- V16 Migration: Add user_identities mapping table
-- ============================================

CREATE TABLE IF NOT EXISTS user_identities (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  provider TEXT NOT NULL,
  provider_sub TEXT NOT NULL,
  email_at_link_time TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (provider, provider_sub)
);

CREATE INDEX IF NOT EXISTS idx_user_identities_user_id ON user_identities(user_id);

-- Backfill from legacy users.auth0_sub so existing users keep working.
INSERT INTO user_identities (
  id,
  user_id,
  provider,
  provider_sub,
  email_at_link_time,
  created_at,
  updated_at
)
SELECT
  gen_random_uuid(),
  u.id,
  CASE
    WHEN POSITION('|' IN u.auth0_sub) > 0 THEN split_part(u.auth0_sub, '|', 1)
    ELSE 'auth0'
  END AS provider,
  CASE
    WHEN POSITION('|' IN u.auth0_sub) > 0 THEN split_part(u.auth0_sub, '|', 2)
    ELSE u.auth0_sub
  END AS provider_sub,
  u.email,
  COALESCE(u.created_at, now()),
  COALESCE(u.updated_at, now())
FROM users u
WHERE u.auth0_sub IS NOT NULL
ON CONFLICT (provider, provider_sub) DO NOTHING;
