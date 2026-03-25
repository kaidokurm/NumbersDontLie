CREATE TABLE IF NOT EXISTS activity_checkins (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    activity_type VARCHAR(64) NOT NULL,
    duration_minutes INTEGER,
    intensity VARCHAR(24),
    note TEXT,
    checkin_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_activity_checkins_user_checkin_at
    ON activity_checkins(user_id, checkin_at DESC)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_activity_checkins_user_checkin_at_active
    ON activity_checkins(user_id, checkin_at)
    WHERE deleted_at IS NULL;
