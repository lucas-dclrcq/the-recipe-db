package org.ldclrcq.dto;

import java.util.List;

public record OcrResult(List<ExtractedRecipe> recipes) {

    public record ExtractedRecipe(
            String recipeName,
            int pageNumber,
            String ingredient,
            double confidence
    ) {
        public boolean needsReview() {
            return confidence < 0.80;
        }
    }
}
