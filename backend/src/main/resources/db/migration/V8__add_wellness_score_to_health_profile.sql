-- V8__add_wellness_score_to_health_profile.sql
-- Adds wellness score column to track overall health status

-- Add wellness score column (0-100 scale)
ALTER TABLE health_profiles ADD COLUMN IF NOT EXISTS wellness_score INTEGER DEFAULT 0;

-- Create index for querying by wellness score
CREATE INDEX IF NOT EXISTS idx_health_profiles_wellness_score ON health_profiles(wellness_score);
