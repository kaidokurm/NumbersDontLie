-- V6__extend_health_profile_schema.sql
-- Extends health profile with dietary preferences, restrictions, and fitness assessment data

-- Add columns for dietary preferences and restrictions
ALTER TABLE health_profiles ADD COLUMN IF NOT EXISTS dietary_preferences TEXT[] DEFAULT ARRAY[]::TEXT[];
ALTER TABLE health_profiles ADD COLUMN IF NOT EXISTS dietary_restrictions TEXT[] DEFAULT ARRAY[]::TEXT[];

-- Add JSON column for fitness assessment data
-- Stores: current_activity_frequency, exercise_types, avg_session_duration, fitness_level, 
--         preferred_environment, exercise_time, endurance_level, strength_level
ALTER TABLE health_profiles ADD COLUMN IF NOT EXISTS fitness_assessment JSONB DEFAULT '{}'::JSONB;

-- Add column to track if fitness assessment has been completed
ALTER TABLE health_profiles ADD COLUMN IF NOT EXISTS fitness_assessment_completed BOOLEAN DEFAULT FALSE;

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_health_profiles_dietary ON health_profiles USING GIN(dietary_preferences, dietary_restrictions);
