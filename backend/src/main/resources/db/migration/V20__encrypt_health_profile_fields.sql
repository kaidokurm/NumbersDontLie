-- Convert health profile fields to encrypted-at-rest text storage.
-- Existing plaintext values are converted to JSON string form; JPA converters will encrypt on next write.
--
-- V6 created a GIN index on dietary_preferences/dietary_restrictions when they were TEXT[].
-- After converting to encrypted TEXT, that index is no longer valid/usable, so drop it first.

DROP INDEX IF EXISTS idx_health_profiles_dietary;

ALTER TABLE health_profiles
    ALTER COLUMN dietary_preferences TYPE TEXT
    USING CASE
        WHEN dietary_preferences IS NULL THEN NULL
        ELSE array_to_json(dietary_preferences)::text
    END;

ALTER TABLE health_profiles
    ALTER COLUMN dietary_restrictions TYPE TEXT
    USING CASE
        WHEN dietary_restrictions IS NULL THEN NULL
        ELSE array_to_json(dietary_restrictions)::text
    END;

ALTER TABLE health_profiles
    ALTER COLUMN fitness_assessment TYPE TEXT
    USING CASE
        WHEN fitness_assessment IS NULL THEN NULL
        ELSE fitness_assessment::text
    END;
