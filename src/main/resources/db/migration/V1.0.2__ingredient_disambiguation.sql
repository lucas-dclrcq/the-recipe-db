-- Add ingredient disambiguation table for storing ingredient aliases
CREATE TABLE ingredient_disambiguation (
    id UUID NOT NULL PRIMARY KEY,
    ingredient_id UUID NOT NULL REFERENCES ingredient(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_disambiguation_name UNIQUE(name)
);

CREATE INDEX idx_disambiguation_ingredient ON ingredient_disambiguation(ingredient_id);
CREATE INDEX idx_disambiguation_name ON ingredient_disambiguation(name);
