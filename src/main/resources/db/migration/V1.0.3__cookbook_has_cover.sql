-- Add has_cover boolean column to avoid loading LOB just to check existence
ALTER TABLE cookbook
    ADD COLUMN IF NOT EXISTS has_cover BOOLEAN NOT NULL DEFAULT FALSE;

-- Update existing rows that have cover data
UPDATE cookbook SET has_cover = TRUE WHERE cover_data IS NOT NULL;
