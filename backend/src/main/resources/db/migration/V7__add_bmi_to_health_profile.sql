-- V7__add_bmi_to_health_profile.sql
-- Adds BMI calculation and classification to health profile

-- Add columns for BMI value and classification
ALTER TABLE health_profiles ADD COLUMN IF NOT EXISTS bmi_value DECIMAL(5,2);
ALTER TABLE health_profiles ADD COLUMN IF NOT EXISTS bmi_classification VARCHAR(20);

-- BMI Classifications:
-- underweight: < 18.5
-- normal: 18.5 - 24.9
-- overweight: 25 - 29.9
-- obese: >= 30

-- Create index for querying by BMI classification
CREATE INDEX IF NOT EXISTS idx_health_profiles_bmi_class ON health_profiles(bmi_classification);
