-- Prevent duplicate weight entries for the same user at the same timestamp.
-- Soft-deleted entries are ignored.
CREATE UNIQUE INDEX IF NOT EXISTS uq_weight_entries_user_measured_at_active
  ON weight_entries(user_id, measured_at)
  WHERE deleted_at IS NULL;
