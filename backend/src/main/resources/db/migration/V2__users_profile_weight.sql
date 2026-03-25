-- Users keyed by Auth0 subject (sub), e.g. "google-oauth2|..."
CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  auth0_sub TEXT NOT NULL UNIQUE,
  email TEXT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- One profile per user
CREATE TABLE IF NOT EXISTS health_profiles (
  user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  birth_year INT NULL,
  gender TEXT NULL,
  height_cm INT NOT NULL,
  baseline_activity_level TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Weight history (supports chart + trends)
CREATE TABLE IF NOT EXISTS weight_entries (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  measured_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  weight_kg NUMERIC(5,2) NOT NULL CHECK (weight_kg > 0),
  note TEXT NULL
);

CREATE INDEX IF NOT EXISTS idx_weight_entries_user_time ON weight_entries(user_id, measured_at DESC);
