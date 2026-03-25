-- V9__add_goal_progress_tracking.sql
-- Adds goal progress tracking to monitor progress towards goals

-- Create goal_progress table
CREATE TABLE IF NOT EXISTS goal_progress (
    id UUID PRIMARY KEY,
    goal_id UUID NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    -- Progress metrics
    current_value DECIMAL(10,2),           -- e.g., current weight or activity days
    progress_percentage INTEGER DEFAULT 0, -- 0-100, calculated as (current_value / target_value) * 100
    
    -- Status tracking
    is_on_track BOOLEAN DEFAULT true,      -- whether user is on pace to reach goal by target date
    days_remaining INTEGER,                -- days until target_date
    
    -- Milestones (e.g., every 5% progress)
    milestones_completed INTEGER DEFAULT 0, -- count of milestones reached
    milestone_details JSONB,               -- stores milestone history: [{"percentage": 25, "completed_at": "..."}]
    
    -- Historical tracking
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for querying
CREATE INDEX IF NOT EXISTS idx_goal_progress_goal_id ON goal_progress(goal_id);
CREATE INDEX IF NOT EXISTS idx_goal_progress_user_id ON goal_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_goal_progress_recorded_at ON goal_progress(recorded_at DESC);
CREATE INDEX IF NOT EXISTS idx_goal_progress_is_on_track ON goal_progress(is_on_track);
