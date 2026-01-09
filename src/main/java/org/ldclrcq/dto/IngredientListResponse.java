package org.ldclrcq.dto;

import java.util.List;
import java.util.UUID;

public record IngredientListResponse(
        List<IngredientSummary> ingredients,
        String nextCursor,
        boolean hasMore
) {
    public record IngredientSummary(
            UUID id,
            String name,
            List<String> disambiguations,
            long recipeCount,
            List<Integer> availableMonths
    ) {}
}
