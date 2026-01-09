-- Add OCR status tracking to cookbook
ALTER TABLE cookbook ADD COLUMN ocr_status VARCHAR(50) NOT NULL DEFAULT 'NONE';
ALTER TABLE cookbook ADD COLUMN ocr_error_message TEXT;

-- Create table to store OCR results before confirmation
CREATE TABLE ocr_result (
    id UUID PRIMARY KEY,
    cookbook_id UUID NOT NULL,
    ingredient VARCHAR(255) NOT NULL,
    recipe_name VARCHAR(255) NOT NULL,
    page_number INTEGER NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    needs_review BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_ocr_result_cookbook FOREIGN KEY (cookbook_id) REFERENCES cookbook(id) ON DELETE CASCADE
);

CREATE INDEX idx_ocr_result_cookbook_id ON ocr_result(cookbook_id);
