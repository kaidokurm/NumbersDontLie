-- V3__goals_and_ai_insights.sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Goals: separate table (1B)
CREATE TABLE IF NOT EXISTS goals (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL,
  goal_type TEXT NOT NULL, -- e.g. "weight_loss", "muscle_gain", "general_fitness"
  target_weight_kg NUMERIC(5,2) NULL,
  target_activity_days_per_week INT NULL,
  notes TEXT NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_goals_user_id ON goals(user_id);
CREATE INDEX IF NOT EXISTS idx_goals_user_active ON goals(user_id, is_active);

-- Optional: milestone tracking (explicitly referenced by testing checklist)
CREATE TABLE IF NOT EXISTS goal_milestones (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  goal_id UUID NOT NULL,
  milestone_type TEXT NOT NULL, -- "weight", "activity", "habits"
  milestone_value NUMERIC(10,4) NOT NULL,
  reached_at TIMESTAMPTZ NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_goal_milestones_goal_id ON goal_milestones(goal_id);

-- AI insight caching and history
CREATE TABLE IF NOT EXISTS ai_insights (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL,
  goal_id UUID NULL,
  input_hash TEXT NOT NULL,
  model TEXT NOT NULL,
  payload JSONB NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_ai_insights_user_created ON ai_insights(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_ai_insights_user_hash ON ai_insights(user_id, input_hash);
