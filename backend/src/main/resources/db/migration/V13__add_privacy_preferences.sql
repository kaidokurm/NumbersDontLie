-- V13__add_privacy_preferences.sql
-- Stores user consent and privacy preferences

CREATE TABLE IF NOT EXISTS privacy_preferences (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    data_usage_consent BOOLEAN NOT NULL DEFAULT FALSE,
    consent_given_at TIMESTAMPTZ NULL,
    allow_anonymized_analytics BOOLEAN NOT NULL DEFAULT FALSE,
    public_profile_visible BOOLEAN NOT NULL DEFAULT FALSE,
    email_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
