-- V18__add_goal_target_date.sql
-- Adds explicit target date to goals for accurate pace/proximity calculations.

ALTER TABLE goals
    ADD COLUMN IF NOT EXISTS target_date DATE;

-- Backfill existing rows with 90-day default horizon from creation date.
UPDATE goals
SET target_date = (created_at::date + INTERVAL '90 days')::date
WHERE target_date IS NULL;

CREATE INDEX IF NOT EXISTS idx_goals_user_target_date ON goals(user_id, target_date);
