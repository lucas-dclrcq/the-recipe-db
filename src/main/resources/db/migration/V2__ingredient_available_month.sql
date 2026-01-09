-- Migration to add ingredient_available_month element collection table
CREATE TABLE IF NOT EXISTS ingredient_available_month (
    ingredient_id UUID NOT NULL,
    month INTEGER NOT NULL,
    CONSTRAINT fk_iam_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredient(id) ON DELETE CASCADE,
    CONSTRAINT ck_iam_month CHECK (month >= 1 AND month <= 12)
);

-- Optional: prevent duplicates
CREATE UNIQUE INDEX IF NOT EXISTS ux_iam_ingredient_month ON ingredient_available_month(ingredient_id, month);
