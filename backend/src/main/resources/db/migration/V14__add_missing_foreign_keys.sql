-- V14__add_missing_foreign_keys.sql
-- Add missing user/goal foreign keys to enforce data integrity and simplify account deletion.

-- Cleanup orphaned rows before adding constraints.
DELETE FROM goal_progress gp
WHERE NOT EXISTS (SELECT 1 FROM users u WHERE u.id = gp.user_id)
   OR NOT EXISTS (SELECT 1 FROM goals g WHERE g.id = gp.goal_id);

DELETE FROM goal_milestones gm
WHERE NOT EXISTS (SELECT 1 FROM goals g WHERE g.id = gm.goal_id);

DELETE FROM goals g
WHERE NOT EXISTS (SELECT 1 FROM users u WHERE u.id = g.user_id);

DELETE FROM ai_insights ai
WHERE NOT EXISTS (SELECT 1 FROM users u WHERE u.id = ai.user_id);

UPDATE ai_insights ai
SET goal_id = NULL
WHERE goal_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM goals g WHERE g.id = ai.goal_id);

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_goals_user'
  ) THEN
    ALTER TABLE goals
      ADD CONSTRAINT fk_goals_user
      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_goal_milestones_goal'
  ) THEN
    ALTER TABLE goal_milestones
      ADD CONSTRAINT fk_goal_milestones_goal
      FOREIGN KEY (goal_id) REFERENCES goals(id) ON DELETE CASCADE;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_ai_insights_user'
  ) THEN
    ALTER TABLE ai_insights
      ADD CONSTRAINT fk_ai_insights_user
      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_ai_insights_goal'
  ) THEN
    ALTER TABLE ai_insights
      ADD CONSTRAINT fk_ai_insights_goal
      FOREIGN KEY (goal_id) REFERENCES goals(id) ON DELETE SET NULL;
  END IF;
END $$;
