-- V10__add_soft_delete.sql
-- Add soft delete support to all resource tables

-- Add deleted_at column to users table
ALTER TABLE users ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE NULL;
CREATE INDEX idx_users_deleted_at ON users(deleted_at);

-- Add deleted_at column to health_profiles table
ALTER TABLE health_profiles ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE NULL;
CREATE INDEX idx_health_profiles_deleted_at ON health_profiles(deleted_at);

-- Add deleted_at column to goals table
ALTER TABLE goals ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE NULL;
CREATE INDEX idx_goals_deleted_at ON goals(deleted_at);

-- Add deleted_at column to weight_entries table
ALTER TABLE weight_entries ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE NULL;
CREATE INDEX idx_weight_entries_deleted_at ON weight_entries(deleted_at);

-- Add deleted_at column to goal_progress table
ALTER TABLE goal_progress ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE NULL;
CREATE INDEX idx_goal_progress_deleted_at ON goal_progress(deleted_at);

-- Add deleted_at column to ai_insights table
ALTER TABLE ai_insights ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE NULL;
CREATE INDEX idx_ai_insights_deleted_at ON ai_insights(deleted_at);

-- Note: audit_events is not soft-deleted (immutable audit trail)
-- Note: email_verification_codes and password_reset_tokens are not soft-deleted (temporary tokens)